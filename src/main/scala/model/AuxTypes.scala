package model

import io.circe.Decoder.decodeString
import io.circe.Decoder.{Result, decoderInstances}
import io.circe.{Decoder, HCursor}

import cats.{Semigroup, Show}

import scala.annotation.targetName

import common.*

object AuxTypes:
  opaque type Channel      = String
  opaque type BodyChannel  = String
  opaque type Message      = String
  opaque type FullUser     = String
  opaque type PlainUser    = String
  opaque type BodyUser     = String
  opaque type JoinChannels = String
  opaque type MessageId    = String
  opaque type AccessToken  = String
  opaque type RefreshToken = String
  opaque type Viewers      = String
  opaque type Capabilities = List[String]

  object Channel:
    def apply(string: String): Channel           = s"#$string"
    def unapply(channel: String): Option[String] = matchOption("#([A-Za-z\\d]+)".r)(channel)

  object BodyChannel:
    def apply(string: String): BodyChannel       = s":$string"
    def unapply(channel: String): Option[String] = matchOption(":(([A-Za-z\\d]+)|-)".r)(channel)

  object Message:
    def apply(string: String): Message          = s":$string"
    def unapply(string: String): Option[String] = matchOption(":(.+$)".r)(string)

  object FullUser:
    def apply(string: String): FullUser         = s":$string!$string@$string.tmi.twitch.tv"
    def unapply(string: String): Option[String] =
      matchOption(":([A-Za-z\\d]+)!\\1@\\1.tmi.twitch.tv".r)(string)

  object PlainUser:
    def apply(string: String): PlainUser        = string
    def unapply(string: String): Option[String] = matchOption("([A-Za-z\\d]+)".r)(string)

  object BodyUser:
    def apply(string: String): BodyUser         = s":$string"
    def unapply(string: String): Option[String] = matchOption(":([A-Za-z\\d]+)".r)(string)

  object Capabilities:
    val membership: Capabilities = List("twitch.tv/membership")
    val tags: Capabilities       = List("twitch.tv/tags")
    val commands: Capabilities   = List("twitch.tv/commands")

  object JoinChannel:
    def apply(str: String*): JoinChannels = str.map(s => s"#$s").reduce(_ + "," + _)

  object MessageId:
    def apply(str: String): MessageId = s"@reply-parent-msg-id=$str"

  object AccessToken:
    def apply(str: String): AccessToken         = str
    def unapply(string: String): Option[String] = Some(string)

  object RefreshToken:
    def apply(str: String): RefreshToken        = str
    def unapply(string: String): Option[String] = Some(string)

  object Viewers:
    def apply(i: Int): Viewers               = Show[Int].show(i)
    def unapply(string: String): Option[Int] = string.toIntOption

  val showChannelContent: Show[Channel]           = identity(_)
  val showBodyChannelContent: Show[BodyChannel]   = identity(_)
  val showMessageContent: Show[Message]           = identity(_)
  val showMessageIdContent: Show[MessageId]       = identity(_)
  val showJoinChannelsContent: Show[JoinChannels] = identity(_)
  val showFullUserContent: Show[FullUser]         = identity(_)
  val showPlainUserContent: Show[PlainUser]       = identity(_)
  val showBodyUserContent: Show[BodyUser]         = identity(_)
  val showCapabilities: Show[Capabilities]        = ":" + _.reduce(_ + " " + _)
  val showAccessToken: Show[AccessToken]          = identity(_)
  val showRefreshToken: Show[RefreshToken]        = identity(_)

  val accessTokenDecoder: Decoder[AccessToken]   = decodeString
  val refreshTokenDecoder: Decoder[RefreshToken] = decodeString

  val semigroupCapabilities: Semigroup[Capabilities] = _.concat(_)
  val semigroupJoinChannels: Semigroup[JoinChannels] = _.concat(_)
