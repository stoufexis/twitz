package main

import zio.*
import zio.stream.*

import cats.syntax.all.*
import cats.{SemigroupK, Show}

import sttp.capabilities.WebSockets
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend

import scala.collection.immutable.HashMap

import service.http_client.HttpClient
import service.local_storage.LocalStorage
import service.process_twitch_chat.TwitchChat
import service.read_access_info.ReadAccessInfo

import model.*
import model.AuxTypes.*
import model.Tags.*

import common.*

import type_classes.Unwrap.unwrap
import type_classes.instances.unwrap.given

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

  val runChat: RIO[TwitchChat, Response[Either[String, Unit]]] =
    TwitchChat.process {
      _.flatMap {
        case Incoming.PRIVMSG(Bits(bits, tags), from, channel, _) =>
          ZStream(
            Outgoing.PRIVMSG(
              tags.getId,
              channel,
              Message(s"Thank you ${from.unwrap} for $bits bits")))

        case Incoming.PRIVMSG(tags, _, channel, inMessage) =>
          for
            msg <- inMessage match
              case Message("!hey") => ZStream("ho")
              case _               => ZStream.empty
          yield Outgoing.PRIVMSG(tags.getId, channel, Message(msg))

        case Incoming.USERNOTICE(Type(UserNoticeType.SUB | UserNoticeType.RESUB, Login(name, _)), channel, _) =>
          ZStream(
            Outgoing.PRIVMSG(
              None,
              channel,
              Message(show"Thank you $name for subscribing!")))

        case Incoming.PING(body) => ZStream(Outgoing.PONG(body))

        case Incoming.USERNOTICE(tags, channel, message)           => ZStream.empty
        case Incoming.ROOMSTATE(tags, channel)                     => ZStream.empty
        case Incoming.GLOBALUSERSTATE(tags)                        => ZStream.empty
        case Incoming.CLEARMSG(tags, channel, message)             => ZStream.empty
        case Incoming.CLEARCHAT(tags, channel, user)               => ZStream.empty
        case Incoming.HOSTTARGET(hostingChannel, channel, viewers) => ZStream.empty
        case Incoming.NOTICE(tags, channel, message)               => ZStream.empty
        case Incoming.USERSTATE(tags, channel)                     => ZStream.empty
        case Incoming.WHISPER(tags, to, from, message)             => ZStream.empty
        case Incoming.JOIN(from, message)                          => ZStream.empty
        case Incoming.PART(from, message)                          => ZStream.empty
      }
    }

  val environment =
    ZLayer.make[TwitchChat](
      TwitchChat.layer,
      HttpClient.layer,
      ReadAccessInfo.layer(info),
      LocalStorage.layer(Path.of(".")),
      Scope.default,
      HttpClientZioBackend.layer())

  val run = runChat.provide(environment)
