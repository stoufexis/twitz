package service.process_twitch_chat

import zio.*
import zio.stream.*

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*

import service.http_client.HttpClient
import service.process_twitch_chat.impl.*
import service.read_access_info.ReadAccessInfo

import model.*

import common.*

type ProcessIncoming = Incoming => Stream[Throwable, Outgoing]

trait TwitchChat:
  def process(f: ProcessIncoming): Task[Response[Either[String, Unit]]]

object TwitchChat:
  def process(f: ProcessIncoming): RIO[TwitchChat, Response[Either[String, Unit]]] =
    ZIO.serviceWithZIO(_.process(f))

  val layer: URLayer[HttpClient & ReadAccessInfo, TwitchChat] =
    ZLayer.fromFunctionEnvironment { env =>
      new:
        def process(f: ProcessIncoming) = makeChatter(f).provideEnvironment(env)
    }
