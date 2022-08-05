package main

import zio.*
import zio.stream.*

import cats.Show
import cats.syntax.all.*

import sttp.capabilities.WebSockets
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend

import service.read_access_info.ReadAccessInfo
import service.process_twitch_chat.TwitchChat

import model.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import type_classes.instances.show.given

import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

object Main extends ZIOAppDefault:
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

  val environment =
    ZLayer.make[TwitchChat](
      TwitchChat.layer,
      HttpClientZioBackend.layer(),
      ReadAccessInfo.makeLayer(info, path),
      Scope.default)

  val run = runChat.provide(environment)
