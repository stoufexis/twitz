package model

import io.circe.Decoder.decodeString
import io.circe.Decoder.{Result, decoderInstances}
import io.circe.{Decoder, HCursor}

import cats.{Semigroup, Show}

import common.*

import type_classes.instances.parse.given
import type_classes.{Read, Unwrap}

object AuxTypes:
  opaque type Channel      = String
  opaque type BodyChannel  = String
  opaque type Message      = String
  opaque type FullUser     = String
  opaque type PlainUser    = String
  opaque type BodyUser     = String
  opaque type Capabilities = List[String]
  opaque type JoinChannels = List[String]
  opaque type MessageId    = String
  opaque type AccessToken  = String
  opaque type RefreshToken = String

  object Capabilities:
    val membership: Capabilities = List("twitch.tv/membership")
    val tags: Capabilities       = List("twitch.tv/tags")
    val commands: Capabilities   = List("twitch.tv/commands")

  object JoinChannel:
    def apply(str: String*): JoinChannels = str.toList

  object Message:
    def apply(str: String): Message = str

  object MessageId:
    def apply(str: String): MessageId = str

  object AccessToken:
    def apply(str: String): AccessToken = str

  object RefreshToken:
    def apply(str: String): RefreshToken = str

  val readChannel: Read[Channel]         = matchEither("#([A-Za-z\\d]+)".r, identity)(_)
  val readBodyChannel: Read[BodyChannel] = matchEither(":(([A-Za-z\\d]+)|-)".r, identity)(_)
  val readMessage: Read[Message]         = matchEither(":(.+$)".r, identity)(_)
  val readFullUser: Read[FullUser]       = matchEither(":([A-Za-z\\d]+)!\\1@\\1.tmi.twitch.tv".r, identity)(_)
  val readPlainUser: Read[PlainUser]     = matchEither("([A-Za-z\\d]+)".r, identity)(_)
  val readBodyUser: Read[BodyUser]       = matchEither(":([A-Za-z\\d]+)".r, identity)(_)

  val showChannel: Show[Channel]           = "#" + _
  val showBodyChannel: Show[BodyChannel]   = ":" + _
  val showMessage: Show[Message]           = ":" + _
  val showFullUser: Show[FullUser]         = us => s":$us!$us@$us.tmi.twitch.tv"
  val showPlainUser: Show[PlainUser]       = identity(_)
  val showBodyUser: Show[BodyUser]         = ":" + _
  val showMessageId: Show[MessageId]       = "@reply-parent-msg-id=" + _
  val showAccessToken: Show[AccessToken]   = identity(_)
  val showRefreshToken: Show[RefreshToken] = identity(_)
  val showCapabilities: Show[Capabilities] = ":" + _.reduce(_ + " " + _)
  val showJoinChannels: Show[JoinChannels] = _.map(s => s"#$s").reduce(_ + "," + _)

  val showChannelContent: Show[Channel]         = identity(_)
  val showBodyChannelContent: Show[BodyChannel] = identity(_)
  val showMessageContent: Show[Message]         = identity(_)
  val showFullUserContent: Show[FullUser]       = identity(_)
  val showPlainUserContent: Show[PlainUser]     = identity(_)
  val showBodyUserContent: Show[BodyUser]       = identity(_)

  val unwrapChannel: Unwrap[Channel, String]                 = identity(_)
  val unwrapBodyChannel: Unwrap[BodyChannel, String]         = identity(_)
  val unwrapMessage: Unwrap[Message, String]                 = identity(_)
  val unwrapFullUser: Unwrap[FullUser, String]               = identity(_)
  val unwrapPlainUser: Unwrap[PlainUser, String]             = identity(_)
  val unwrapBodyUser: Unwrap[BodyUser, String]               = identity(_)
  val unwrapAccessToken: Unwrap[AccessToken, String]         = identity(_)
  val unwrapRefreshToken: Unwrap[RefreshToken, String]       = identity(_)
  val unwrapCapabilities: Unwrap[Capabilities, List[String]] = identity(_)
  val unwrapJoinChannels: Unwrap[JoinChannels, List[String]] = identity(_)

  val accessTokenDecoder: Decoder[AccessToken]   = decodeString
  val refreshTokenDecoder: Decoder[RefreshToken] = decodeString

  val semigroupCapabilities: Semigroup[Capabilities] = _.concat(_)
  val semigroupJoinChannels: Semigroup[JoinChannels] = _.concat(_)
