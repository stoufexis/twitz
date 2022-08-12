import zio.*
import zio.stream.*

import cats.syntax.all.*
import cats.{SemigroupK, Show}

import sttp.capabilities.WebSockets
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.ws.WebSocketFrame

import scala.collection.immutable.HashMap

import service.authentication_store.AuthenticationStore
import service.http_client.HttpClient
import service.http_client.HttpClient.*
import service.local_storage.LocalStorage
import service.process_twitch_chat.TwitchChat

import model.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import type_classes.Unwrap.unwrap
import type_classes.instances.unwrap.given

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

val info = Credentials(
  channels = JoinChannel("channel1", "channel2"),
  accessToken = AccessToken("accesstoken"),
  refreshToken = RefreshToken("refreshtoken"),
  clientId = "clientid",
  clientSecret = "clientsecret"
)

val runChat: RIO[TwitchChat, Response[Either[String, Unit]]] =
  TwitchChat.process {
    _.flatMap {
      case Incoming.PRIVMSG(Bits(bits, tags), from, channel, _) =>
        ZStream(Message(s"Thank you ${from.unwrap} for $bits bits"))
          .map(Outgoing.PRIVMSG(tags.getId, channel, _))

      case Incoming.PRIVMSG(tags, _, channel, inMessage) =>
        for
          msg <- inMessage.unwrap match
            case "!hey" => ZStream("ho")
            case _      => ZStream.empty
        yield Outgoing.PRIVMSG(tags.getId, channel, Message(msg))

      case Incoming.USERNOTICE(Type(UserNoticeType.SUB | UserNoticeType.RESUB, Login(name, _)), channel, _) =>
        ZStream(Message(s"Thank you ${name.unwrap} for subscribing!"))
          .map(Outgoing.PRIVMSG(None, channel, _))

      case Incoming.PING(body) => ZStream(Outgoing.PONG(body))
      case _                   => ZStream.empty
    }
  }

val environment =
  ZLayer.make[TwitchChat](
    TwitchChat.layer,
    HttpClient.layer,
    AuthenticationStore.layer(info),
    LocalStorage.layer(Paths.get("./tmp/")),
    Scope.default,
    HttpClientZioBackend.layer())

object Main extends ZIOAppDefault:
  val run = runChat.provide(environment)
