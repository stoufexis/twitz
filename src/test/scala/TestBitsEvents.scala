import zio.*
import zio.stream.*

import cats.Show
import cats.syntax.all.*

import sttp.capabilities.*
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.*
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.testing.*
import sttp.model.*
import sttp.monad.MonadError
import sttp.ws.*

import service.process_twitch_chat.TwitchChat
import service.read_access_info.ReadAccessInfo

import model.*
import model.AccessInfo.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import type_classes.instances.show.given

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

val ws = new WebSocket[Task]:
  override def receive(): Task[WebSocketFrame] =
    println("AAAA")
    ZIO.succeed(WebSocketFrame.text("ASDASDASD"))

  override def send(f: WebSocketFrame, isContinuation: Boolean): Task[Unit] =
    println("aaaa")
    Console.printLine(f)

  override def monad: MonadError[Task] = HttpClientZioBackend.stub.responseMonad
  override val upgradeHeaders: Headers = Headers(Nil)

  override def isOpen(): Task[Boolean] = ZIO.succeed(true)

val info = AccessInfo(
  channels = JoinChannel("damapatas123"),
  accessToken = AccessToken("j2xl92k09tlf80mjkgrmju1vfvlju4"),
  refreshToken = RefreshToken("ozdcm0kd44vdi7uc26ptbqk1rnnl00ifr1488w54qx8dwy405w"),
  clientId = "4ernutpawy9xrie0cm7lvz9djzobof",
  clientSecret = "2fut789ifewi46wdzssg8ver6r2xbp"
)

val path: Path = Paths.get("tokens.txt")

val runChat: RIO[TwitchChat, Response[Unit]] =
  TwitchChat.process {
    case Incoming.PRIVMSG(tags, from, channel, message) =>
      val bits =
        for
          bits <- tags.getBits
          message = Message(show"Thank you $from for $bits bits")
        yield Outgoing.PRIVMSG(None, channel, message)

      val command =
        message.show match
          case "!Hey" => Some(Outgoing.PRIVMSG(tags.getId, channel, Message("ho")))
          case _      => None

      ZStream fromOption bits <+> command

    case Incoming.PING(body)                                   => ZStream.empty
    case Incoming.ROOMSTATE(tags, channel)                     => ZStream.empty
    case Incoming.GLOBALUSERSTATE(tags)                        => ZStream.empty
    case Incoming.CLEARMSG(tags, channel, message)             => ZStream.empty
    case Incoming.CLEARCHAT(tags, channel, user)               => ZStream.empty
    case Incoming.HOSTTARGET(hostingChannel, channel, viewers) => ZStream.empty
    case Incoming.NOTICE(tags, channel, message)               => ZStream.empty
    case Incoming.USERNOTICE(tags, channel, message)           => ZStream.empty
    case Incoming.USERSTATE(tags, channel)                     => ZStream.empty
    case Incoming.WHISPER(tags, to, from, message)             => ZStream.empty
    case Incoming.JOIN(from, message)                          => ZStream.empty
    case Incoming.PART(from, message)                          => ZStream.empty
  }

val backend: SttpBackendStub[Task, WebSockets & ZioStreams] =
  HttpClientZioBackend
    .stub
    .whenAnyRequest
    .thenRespond(ws)

val environment =
  ZLayer.make[TwitchChat](
    TwitchChat.layer,
    ZLayer.succeed(backend),
    ReadAccessInfo.makeNonFSLayer(info))

object TestMain extends ZIOAppDefault:
  val run = runChat.provide(environment)

//val a =
//  SttpBackendStub.synchronous
//    .whenAnyRequest
//    .thenRespond(b)
