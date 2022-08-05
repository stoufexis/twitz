package type_classes.instances.parse

import model.AuxTypes.*
import model.Incoming
import model.Incoming.*
import model.Tags.*

import type_classes.Read
import type_classes.Read.readProduct

private val GLOBALUSERSTATE_REGEX = "([^ ]+) :tmi.twitch.tv GLOBALUSERSTATE".r
private val HOSTTARGET_REGEX      = ":tmi.twitch.tv HOSTTARGET ([^ ]+) ([^ ]+) ([^ ]+)".r
private val PING_REGEX            = "PING :tmi.twitch.tv".r
private val PRIVMSG_REGEX         = "([^ ]+) ([^ ]+) PRIVMSG ([^ ]+) (.+)".r
private val ROOMSTATE_REGEX       = "([^ ]+) :tmi.twitch.tv ROOMSTATE ([^ ]+)".r
private val CLEARMSG_REGEX        = "([^ ]+) :tmi.twitch.tv CLEARMSG ([^ ]+) (.+)".r
private val CLEARCHAT_REGEX       = "([^ ]+) :tmi.twitch.tv CLEARCHAT ([^ ]+) ([^ ]+)?".r
private val NOTICE_REGEX          = "([^ ]+) :tmi.twitch.tv NOTICE ([^ ]+) (.+)".r
private val USERNOTICE_REGEX      = "([^ ]+) :tmi.twitch.tv USERNOTICE ([^ ]+) (.+)?".r
private val USERSTATE_REGEX       = "([^ ]+) :tmi.twitch.tv USERSTATE ([^ ]+)".r
private val WHISPER_REGEX         = "([^ ]+) :tmi.twitch.tv WHISPER ([^ ]+) (.+)".r
private val JOIN_REGEX            = "([^ ]+) JOIN ([^ ]+)".r
private val PART_REGEX            = "([^ ]+) PART ([^ ]+)".r

given Read[Incoming] =
  case GLOBALUSERSTATE_REGEX(tags) =>
    Read[GlobalUserStateTags].read(tags).map(GLOBALUSERSTATE.apply)

  case HOSTTARGET_REGEX(hChannel, channel, viewers) =>
    readProduct[Channel, BodyChannel, Int, HOSTTARGET](hChannel, channel, viewers)

  case PING_REGEX(body) =>
    Right(PING(body))

  case PRIVMSG_REGEX(tags, from, channel, message) =>
    readProduct[PrivmsgTags, FullUser, Channel, Message, PRIVMSG](tags, from, channel, message)

  case ROOMSTATE_REGEX(tags, channel) =>
    readProduct[RoomStateTags, Channel, ROOMSTATE](tags, channel)

  case CLEARMSG_REGEX(tags, channel, message) =>
    readProduct[ClearMsgTags, Channel, Message, CLEARMSG](tags, channel, message)

  case CLEARCHAT_REGEX(tags, channel) =>
    readProduct[ClearChatTags, Channel, Option[BodyUser], CLEARCHAT](tags, channel, "")

  case CLEARCHAT_REGEX(tags, channel, user) =>
    readProduct[ClearChatTags, Channel, Option[BodyUser], CLEARCHAT](tags, channel, user)

  case NOTICE_REGEX(tags, channel, message) =>
    readProduct[NoticeTags, Channel, Message, NOTICE](tags, channel, message)

  case USERNOTICE_REGEX(tags, channel) =>
    readProduct[UserNoticeTags, Channel, Option[Message], USERNOTICE](tags, channel, "")

  case USERNOTICE_REGEX(tags, channel, message) =>
    readProduct[UserNoticeTags, Channel, Option[Message], USERNOTICE](tags, channel, message)

  case USERSTATE_REGEX(tags, channel) =>
    readProduct[UserStateTags, Channel, USERSTATE](tags, channel)

  case WHISPER_REGEX(tags, to, from, message) =>
    readProduct[WhisperTags, BodyUser, PlainUser, Message, WHISPER](tags, to, from, message)

  case JOIN_REGEX(from, channel) =>
    readProduct[FullUser, Channel, JOIN](from, channel)

  case PART_REGEX(from, channel) =>
    readProduct[FullUser, Channel, PART](from, channel)

  case in => Left(in)

//@main def main() =
//  val a =
//    Read[Incoming].read("@badge-info=;badges=broadcaster/1;client-nonce=06f5349227ca3d8b5ccba54d798d0ead;color=;display-name=damapatas123;emotes=;first-msg=0;flags=;id=16c321e7-5828-4cbe-9b9f-08b653a38c1a;mod=0;returning-chatter=0;room-id=602674108;subscriber=0;tmi-sent-ts=1659370433410;turbo=0;user-id=602674108;user-type= :damapatas123!damapatas123@damapatas123.tmi.twitch.tv PRIVMSG #damapatas123 :!Hey")
//  println(a)
