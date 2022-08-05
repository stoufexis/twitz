package service.process_twitch_chat

import zio.*
import zio.stream.*

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*

import service.read_access_info.ReadAccessInfo

import model.*

import common.*

import service.process_twitch_chat.impl.*

type ProcessIncoming = Incoming => Stream[Throwable, Outgoing]

trait TwitchChat:
  def process(f: ProcessIncoming): Task[Response[Unit]]

object TwitchChat:
  def process(f: ProcessIncoming): RIO[TwitchChat, Response[Unit]] =
    ZIO.serviceWithZIO(_.process(f))

  val layer: URLayer[SttpBackend[Task, WebSockets & ZioStreams] & ReadAccessInfo, TwitchChat] =
    ZLayer.fromFunctionEnvironment { env =>
      new TwitchChat:
        def process(f: ProcessIncoming) = makeChatter(f).provideEnvironment(env)
    }
