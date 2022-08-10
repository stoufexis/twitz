import zio.*
import zio.stream.*
import zio.test.*
import zio.{Task, *}

import cats.Show
import cats.syntax.all.*

import sttp.capabilities.*
import sttp.capabilities.WebSockets
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.testing.*
import sttp.client3.*
import sttp.model.*
import sttp.monad.MonadError
import sttp.ws.*

import service.http_client.*
import service.process_twitch_chat.TwitchChat
import service.authentication_store.AuthenticationStore

import model.*
import model.Credentials.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import type_classes.Unwrap.unwrap
import type_classes.instances.unwrap.given

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

val info = Credentials(
  channels = JoinChannel("channel123"),
  accessToken = AccessToken("abcdefghijklmnopqrstuvwxyz1234567890"),
  refreshToken = RefreshToken("1234567890abcdefghijklmnopqrstuvwxyz"),
  clientId = "123123123abcabcabcabcabc",
  clientSecret = "abcabcabcabcabcabcabcabc123123"
)

def streamOutgoing(replyTo: Option[MessageId], channel: Channel, message: String): UStream[Outgoing.PRIVMSG] =
  ZStream(Outgoing.PRIVMSG(replyTo, channel, Message(message)))

val processChat: ZStream[Any, Throwable, Incoming] => ZStream[Any, Throwable, Outgoing] =
  _.flatMap {
    case Incoming.PRIVMSG(Bits(bits, tags), from, channel, _) =>
      val message = s"Thank you ${from.unwrap} for $bits bits"
      streamOutgoing(tags.getId, channel, message)

    case Incoming.PRIVMSG(tags, _, channel, inMessage) =>
      for
        msg <- inMessage.unwrap match
          case "!hey" => ZStream("ho")
          case _      => ZStream.empty
      yield Outgoing.PRIVMSG(tags.getId, channel, Message(msg))

    case Incoming.USERNOTICE(Type(UserNoticeType.SUB | UserNoticeType.RESUB, Login(name, _)), channel, _) =>
      val message = s"Thank you ${name.unwrap} for subscribing!"
      streamOutgoing(None, channel, message)

    case _ => ZStream.empty
  }

val input: List[WebSocketFrame] =
  List(
    WebSocketFrame.text(
      "@badge-info=;badges=staff/1,bits/1000;bits=100;color=;display-name=ronni;emotes=" +
        ";id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=12345678;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=12345678;user-type=staff " +
        ":ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #ronni :cheer100"),
    WebSocketFrame.text(
      "@badge-info=;badges=broadcaster/1;client-nonce=459e3142897c7a22b7d275178f2259e0;color=#0000FF;display-name=lovingt3s;" +
        "emote-only=1;emotes=62835:0-10;first-msg=0;flags=;id=885196de-cb67-427a-baa8-82f9b0fcd05f;mod=0;room-id=713936733;subscriber=0;tmi-sent-ts=1643904084794;turbo=0;user-id=713936733;user-type= " +
        ":lovingt3s!lovingt3s@lovingt3s.tmi.twitch.tv PRIVMSG #bar :!hey"),
    WebSocketFrame.text(
      "@badge-info=;badges=broadcaster/1;client-nonce=459e3142897c7a22b7d275178f2259e0;color=#0000FF;display-name=lovingt3s;" +
        "emote-only=1;emotes=62835:0-10;first-msg=0;flags=;id=885196de-cb67-427a-baa8-82f9b0fcd05f;mod=0;room-id=713936733;subscriber=0;tmi-sent-ts=1643904084794;turbo=0;user-id=713936733;user-type= " +
        ":lovingt3s!lovingt3s@lovingt3s.tmi.twitch.tv PRIVMSG #bar :!ping"),
    WebSocketFrame.text(
      "@badge-info=;badges=staff/1,broadcaster/1,turbo/1;color=#008000;display-name=ronni;emotes=;id=db25007f-7a18-43eb-9379-80131e44d633;login=ronni;mod=0;msg-id=resub;msg-param-cumulative-months=6;" +
        "msg-param-streak-months=2;msg-param-should-share-streak=1;msg-param-sub-plan=Prime;msg-param-sub-plan-name=Prime;room-id=12345678;subscriber=1;system-msg=ronni\\shas\\ssubscribed\\sfor\\s6\\smonths!;tmi-sent-ts=1507246572675;" +
        "turbo=1;user-id=87654321;user-type=staff :tmi.twitch.tv USERNOTICE #dallas :Great stream -- keep it up!"),
    WebSocketFrame.text(
      "@badge-info=;badges=staff/1,broadcaster/1,turbo/1;color=#008000;display-name=ronni;emotes=;id=db25007f-7a18-43eb-9379-80131e44d633;login=ronni;mod=0;msg-id=sub;msg-param-cumulative-months=6;" +
        "msg-param-streak-months=2;msg-param-should-share-streak=1;msg-param-sub-plan=Prime;msg-param-sub-plan-name=Prime;room-id=12345678;subscriber=1;system-msg=ronni\\shas\\ssubscribed\\sfor\\s6\\smonths!;tmi-sent-ts=1507246572675;" +
        "turbo=1;user-id=87654321;user-type=staff :tmi.twitch.tv USERNOTICE #dallas :Great stream -- keep it up!"),
    WebSocketFrame.text(
      "@badge-info=;badges=staff/1,broadcaster/1,turbo/1;color=#008000;display-name=ronni;emotes=;id=db25007f-7a18-43eb-9379-80131e44d633;login=ronni;mod=0;msg-id=subgift;msg-param-cumulative-months=6;" +
        "msg-param-streak-months=2;msg-param-should-share-streak=1;msg-param-sub-plan=Prime;msg-param-sub-plan-name=Prime;room-id=12345678;subscriber=1;system-msg=ronni\\shas\\ssubscribed\\sfor\\s6\\smonths!;tmi-sent-ts=1507246572675;" +
        "turbo=1;user-id=87654321;user-type=staff :tmi.twitch.tv USERNOTICE #dallas :Great stream -- keep it up!")
  )

val expected: List[WebSocketFrame] =
  List(
    "CAP REQ :twitch.tv/membership twitch.tv/tags twitch.tv/commands",
    "PASS oauth:j2xl92k09tlf80mjkgrmju1vfvlju4",
    "NICK #damapatas123",
    "JOIN #damapatas123",
    "@reply-parent-msg-id=b34ccfc7-4977-403a-8a94-33c6bac34fb8 PRIVMSG #ronni :Thank you ronni for 100 bits",
    "@reply-parent-msg-id=885196de-cb67-427a-baa8-82f9b0fcd05f PRIVMSG #bar :ho",
    "PRIVMSG #dallas :Thank you ronni for subscribing!",
    "PRIVMSG #dallas :Thank you ronni for subscribing!"
  ).map(WebSocketFrame.text)

val mockReq: [T] => Request[RequestResult[T], Any] => Task[Response[RequestResult[T]]] =
  [T] => (r: Request[RequestResult[T], Any]) => ??? // Shouldn't be called

object Tests extends ZIOSpecDefault:

  val mockEnv: Ref[List[WebSocketFrame]] => ULayer[TwitchChat] =
    ref =>
      ZLayer.make[TwitchChat](
        TwitchChat.layer,
        AuthenticationStore.mockLayer(info),
        HttpClient.mockLayer(input, ref, mockReq))

  val spec = suite("tests") {
    test("responds to bits") {
      for
        ref    <- Ref.make[List[WebSocketFrame]](Nil)
        _      <- TwitchChat.process(processChat).provide(mockEnv(ref))
        output <- ref.get
      yield assert(output)(Assertion.equalTo(expected))
    }
  }
