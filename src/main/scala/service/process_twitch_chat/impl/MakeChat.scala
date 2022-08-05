package service.process_twitch_chat.impl

import io.circe.*
import zio.*
import zio.stream.*

import cats.syntax.all.catsSyntaxSemigroup

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.ws.WebSocketFrame
import sttp.ws.WebSocketFrame.*

import service.http_client.HttpClient
import service.process_twitch_chat.*
import service.read_access_info.ReadAccessInfo

import model.*
import model.Auth.*
import model.AuxTypes.{Capabilities, Channel, JoinChannels}
import model.Outgoing.*

import common.*

import type_classes.Read.read
import type_classes.instances.parse.given

val authStream: ZStream[ReadAccessInfo, Throwable, WebSocketFrame] =
  for
    info <- ZStream.fromZIO(ReadAccessInfo.get)

    cap  = CAP_REQ(Capabilities.membership |+| Capabilities.tags |+| Capabilities.commands)
    pass = PASS(info.accessToken)
    nick = NICK(info.channels)
    join = JOIN(info.channels)

    frames <- ZStream(cap, pass, nick, join).map {
      case auth: Auth         => toFrame(auth)
      case outgoing: Outgoing => toFrame(outgoing)
    }
  yield frames

type Pipe[A, B] = ZStream[Any, Throwable, A] => ZStream[Any, Throwable, B]

def processFrames(f: ProcessIncoming): Pipe[WebSocketFrame, WebSocketFrame] =
  _.flatMap {
    case Text(payload, _, _) =>
      for
        incoming <- ZStream
          .fromIterable(payload.split("\r\n"))
          .map(read[Incoming])

        outgoing <- incoming match
          case Left(value)  => printIgnore(value)
          case Right(value) => f(value)
      yield toFrame(outgoing)

    case Ping(payload) => ZStream(Pong(payload))
    case other         => printIgnore(other)
  }

def makeChatter(f: ProcessIncoming): RIO[HttpClient & ReadAccessInfo, Response[Either[String, Unit]]] =
  val uri = uri"ws://irc-ws.chat.twitch.tv:80"
  for
    info <- ZIO.environment[ReadAccessInfo]
    auth = authStream.provideEnvironment(info)
    response <- HttpClient.websocket(uri, auth ++ _.viaFunction(processFrames(f)))
  yield response
