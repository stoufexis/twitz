package model

import cats.syntax.all.*

import sttp.ws.WebSocketFrame

import model.AuxTypes.*
import model.Tags.*

import type_classes.instances.show.given

enum Incoming:
  case PING(body: String)
  case PRIVMSG(tags: PrivmsgTags, from: FullUser, channel: Channel, message: Message)
  case ROOMSTATE(tags: RoomStateTags, channel: Channel)
  case GLOBALUSERSTATE(tags: GlobalUserStateTags)
  case CLEARMSG(tags: ClearMsgTags, channel: Channel, message: Message)
  case CLEARCHAT(tags: ClearChatTags, channel: Channel, user: Option[BodyUser])
  case HOSTTARGET(hostingChannel: Channel, channel: BodyChannel, viewers: Viewers)
  case NOTICE(tags: NoticeTags, channel: Channel, message: Message)
  case USERNOTICE(tags: UserNoticeTags, channel: Channel, message: Option[Message])
  case USERSTATE(tags: UserStateTags, channel: Channel)
  case WHISPER(tags: WhisperTags, to: BodyUser, from: PlainUser, message: Message)
  case JOIN(from: FullUser, message: Channel)
  case PART(from: FullUser, message: Channel)

enum Outgoing:
  case PRIVMSG(replyTo: Option[MessageId], channel: Channel, body: Message)
  case PONG(body: String)
  case NICK(channel: JoinChannels)
  case JOIN(channel: JoinChannels)

enum Auth:
  case CAP_REQ(caps: Capabilities)
  case PASS(token: AccessToken)

object Incoming:
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

  def parse: String => Either[String, Incoming] =
    case GLOBALUSERSTATE_REGEX(GlobalUserStateTags(t)) =>
      Right(GLOBALUSERSTATE(GlobalUserStateTags(t)))

    case HOSTTARGET_REGEX(Channel(ch), BodyChannel(bch), Viewers(v)) =>
      Right(HOSTTARGET(Channel(ch), BodyChannel(bch), Viewers(v)))

    case PING_REGEX(body) =>
      Right(PING(body))

    case PRIVMSG_REGEX(PrivmsgTags(t), FullUser(user), Channel(ch), Message(msg)) =>
      Right(PRIVMSG(PrivmsgTags(t), FullUser(user), Channel(ch), Message(msg)))

    case ROOMSTATE_REGEX(RoomStateTags(t), Channel(ch)) =>
      Right(ROOMSTATE(RoomStateTags(t), Channel(ch)))

    case CLEARMSG_REGEX(ClearMsgTags(t), Channel(ch), Message(msg)) =>
      Right(CLEARMSG(ClearMsgTags(t), Channel(ch), Message(msg)))

    case CLEARCHAT_REGEX(ClearChatTags(t), Channel(ch)) =>
      Right(CLEARCHAT(ClearChatTags(t), Channel(ch), None))

    case CLEARCHAT_REGEX(ClearChatTags(t), Channel(ch), BodyUser(u)) =>
      Right(CLEARCHAT(ClearChatTags(t), Channel(ch), Some(BodyUser(u))))

    case NOTICE_REGEX(NoticeTags(t), Channel(ch), Message(msg)) =>
      Right(NOTICE(NoticeTags(t), Channel(ch), Message(msg)))

    case USERNOTICE_REGEX(UserNoticeTags(t), Channel(ch)) =>
      Right(USERNOTICE(UserNoticeTags(t), Channel(ch), None))

    case USERNOTICE_REGEX(UserNoticeTags(t), Channel(ch), Message(msg)) =>
      Right(USERNOTICE(UserNoticeTags(t), Channel(ch), Some(Message(msg))))

    case USERSTATE_REGEX(UserStateTags(u), Channel(ch)) =>
      Right(USERSTATE(UserStateTags(u), Channel(ch)))

    case WHISPER_REGEX(WhisperTags(t), BodyUser(bu), PlainUser(pu), Message(msg)) =>
      Right(WHISPER(WhisperTags(t), BodyUser(bu), PlainUser(pu), Message(msg)))

    case JOIN_REGEX(FullUser(u), Channel(ch)) =>
      Right(JOIN(FullUser(u), Channel(ch)))

    case PART_REGEX(FullUser(u), Channel(ch)) =>
      Right(PART(FullUser(u), Channel(ch)))

    case in => Left(in)

object Outgoing:
  def toFrame: Outgoing => WebSocketFrame =
    case Outgoing.PRIVMSG(replyTo, channel, body) => WebSocketFrame.text(show"$replyTo PRIVMSG $channel $body")
    case Outgoing.PONG(body)                      => WebSocketFrame.text(show"PONG $body")
    case Outgoing.NICK(channel)                   => WebSocketFrame.text(show"NICK $channel")
    case Outgoing.JOIN(channel)                   => WebSocketFrame.text(show"JOIN $channel")

object Auth:
  def toFrame: Auth => WebSocketFrame =
    case Auth.CAP_REQ(caps) => WebSocketFrame.text(show"CAP REQ $caps")
    case Auth.PASS(token)   => WebSocketFrame.text(show"PASS oauth:$token")
