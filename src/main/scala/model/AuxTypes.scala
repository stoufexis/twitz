package model

import io.circe.Decoder.decodeString
import io.circe.Decoder.{Result, decoderInstances}
import io.circe.{Decoder, HCursor}

import cats.{Semigroup, Show}

import scala.annotation.targetName

import common.*

import type_classes.Unwrap

object AuxTypes:
  opaque type Channel      = String
  opaque type BodyChannel  = String
  opaque type Message      = String
  opaque type FullUser     = String
  opaque type PlainUser    = String
  opaque type BodyUser     = String
  opaque type JoinChannels = List[String]
  opaque type MessageId    = String
  opaque type AccessToken  = String
  opaque type RefreshToken = String
  opaque type Viewers      = Int
  opaque type Capabilities = List[String]

  object Channel:
    def apply(string: String): Channel            = string
    def unapply(channel: String): Option[Channel] = matchOption("#([A-Za-z\\d]+)".r)(channel)

  object BodyChannel:
    def apply(string: String): BodyChannel            = string
    def unapply(channel: String): Option[BodyChannel] = matchOption(":(([A-Za-z\\d]+)|-)".r)(channel)

  object Message:
    def apply(string: String): Message           = string
    def unapply(string: String): Option[Message] = matchOption(":(.+$)".r)(string)

  object FullUser:
    def apply(string: String): FullUser           = string
    def unapply(string: String): Option[FullUser] =
      matchOption(":([A-Za-z\\d]+)!\\1@\\1.tmi.twitch.tv".r)(string)

  object PlainUser:
    def apply(string: String): PlainUser           = string
    def unapply(string: String): Option[PlainUser] = matchOption("([A-Za-z\\d]+)".r)(string)

  object BodyUser:
    def apply(string: String): BodyUser           = string
    def unapply(string: String): Option[BodyUser] = matchOption(":([A-Za-z\\d]+)".r)(string)

  object Capabilities:
    val membership: Capabilities = List("twitch.tv/membership")
    val tags: Capabilities       = List("twitch.tv/tags")
    val commands: Capabilities   = List("twitch.tv/commands")

  object JoinChannel:
    def apply(str: String*): JoinChannels = str.toList

  object MessageId:
    def apply(str: String): MessageId = str

  object AccessToken:
    def apply(str: String): AccessToken              = str
    def unapply(string: String): Option[AccessToken] = Some(string)

  object RefreshToken:
    def apply(str: String): RefreshToken              = str
    def unapply(string: String): Option[RefreshToken] = Some(string)

  object Viewers:
    def apply(i: Int): Viewers                   = i
    def unapply(string: String): Option[Viewers] = string.toIntOption

  val unwrapChannel: Unwrap[Channel, String]                 = Unwrap.make(identity, identity)
  val unwrapBodyChannel: Unwrap[BodyChannel, String]         = Unwrap.make(identity, identity)
  val unwrapMessage: Unwrap[Message, String]                 = Unwrap.make(identity, identity)
  val unwrapFullUser: Unwrap[FullUser, String]               = Unwrap.make(identity, identity)
  val unwrapPlainUser: Unwrap[PlainUser, String]             = Unwrap.make(identity, identity)
  val unwrapBodyUser: Unwrap[BodyUser, String]               = Unwrap.make(identity, identity)
  val unwrapJoinChannels: Unwrap[JoinChannels, List[String]] = Unwrap.make(identity, identity)
  val unwrapMessageId: Unwrap[MessageId, String]             = Unwrap.make(identity, identity)
  val unwrapAccessToken: Unwrap[AccessToken, String]         = Unwrap.make(identity, identity)
  val unwrapRefreshToken: Unwrap[RefreshToken, String]       = Unwrap.make(identity, identity)
  val unwrapViewers: Unwrap[Viewers, Int]                    = Unwrap.make(identity, identity)

  val showChannelContent: Show[Channel]           = "#" + _
  val showBodyChannelContent: Show[BodyChannel]   = ":" + _
  val showMessageContent: Show[Message]           = ":" + _
  val showMessageIdContent: Show[MessageId]       = "@reply-parent-msg-id=" + _
  val showJoinChannelsContent: Show[JoinChannels] = "#" + _.map(Channel(_)).reduce(_ + "," + _)
  val showFullUserContent: Show[FullUser]         = string => s":$string!$string@$string.tmi.twitch.tv"
  val showPlainUserContent: Show[PlainUser]       = ":" + _
  val showBodyUserContent: Show[BodyUser]         = identity(_)
  val showCapabilities: Show[Capabilities]        = ":" + _.reduce(_ + " " + _)
  val showAccessToken: Show[AccessToken]          = identity(_)
  val showRefreshToken: Show[RefreshToken]        = identity(_)

  val accessTokenDecoder: Decoder[AccessToken]   = decodeString
  val refreshTokenDecoder: Decoder[RefreshToken] = decodeString

  val semigroupCapabilities: Semigroup[Capabilities] = _.concat(_)
  val semigroupJoinChannels: Semigroup[JoinChannels] = _.concat(_)
