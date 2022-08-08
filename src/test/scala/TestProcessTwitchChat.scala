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
import sttp.client3.{Request, *}
import sttp.model.*
import sttp.monad.MonadError
import sttp.ws.*

import service.http_client.*
import service.process_twitch_chat.TwitchChat
import service.read_access_info.ReadAccessInfo

import model.*
import model.AccessInfo.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

val info = AccessInfo(
  channels = JoinChannel("damapatas123"),
  accessToken = AccessToken("j2xl92k09tlf80mjkgrmju1vfvlju4"),
  refreshToken = RefreshToken("ozdcm0kd44vdi7uc26ptbqk1rnnl00ifr1488w54qx8dwy405w"),
  clientId = "4ernutpawy9xrie0cm7lvz9djzobof",
  clientSecret = "2fut789ifewi46wdzssg8ver6r2xbp"
)

val runChat =
  TwitchChat.process {
    case Incoming.PRIVMSG(tags, FullUser(from), channel, inMessage) =>
      tags.getBits match
        case Some(value) =>
          val message = Message(show"Thank you $from for $value bits")
          ZStream(Outgoing.PRIVMSG(tags.getId, channel, message))

        case None =>
          val message = inMessage match
            case Message("!hey") => ZStream("ho")
            case _               => ZStream.empty
          message
            .map(msg => Outgoing.PRIVMSG(tags.getId, channel, Message(msg)))

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
        ":lovingt3s!lovingt3s@lovingt3s.tmi.twitch.tv PRIVMSG #bar :!ping"
    )
  )

val expected: List[WebSocketFrame] =
  List(
    "CAP REQ :twitch.tv/membership twitch.tv/tags twitch.tv/commands",
    "PASS oauth:j2xl92k09tlf80mjkgrmju1vfvlju4",
    "NICK #damapatas123",
    "JOIN #damapatas123",
    "@reply-parent-msg-id=b34ccfc7-4977-403a-8a94-33c6bac34fb8 PRIVMSG #ronni :Thank you ronni for 100 bits",
    "@reply-parent-msg-id=885196de-cb67-427a-baa8-82f9b0fcd05f PRIVMSG #bar :ho"
  ).map(WebSocketFrame.text)

val mockReq: [T] => Request[RequestResult[T], Any] => Task[Response[RequestResult[T]]] =
  [T] => (r: Request[RequestResult[T], Any]) => ???

object Tests extends ZIOSpecDefault:
  val spec = suite("tests") {
    test("responds to bits") {
      for
        ref <- Ref.make[List[WebSocketFrame]](Nil)
        env = ZLayer.make[TwitchChat](
          TwitchChat.layer,
          ReadAccessInfo.mockLayer(info),
          HttpClient.mockLayer(input, ref, mockReq))
        _      <- runChat.provide(env)
        output <- ref.get
      yield assert(output)(Assertion.equalTo(expected))
    }
  }
