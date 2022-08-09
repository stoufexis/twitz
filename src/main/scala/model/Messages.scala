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

  val parse: String => Either[String, Incoming] = {
    case PRIVMSG_REGEX(PrivmsgTags(t), FullUser(user), Channel(ch), Message(msg)) =>
      Right(PRIVMSG(t, user, ch, msg))

    case WHISPER_REGEX(WhisperTags(t), BodyUser(bu), PlainUser(pu), Message(msg)) =>
      Right(WHISPER(t, bu, pu, msg))

    case GLOBALUSERSTATE_REGEX(GlobalUserStateTags(t))                  => Right(GLOBALUSERSTATE(t))
    case HOSTTARGET_REGEX(Channel(ch), BodyChannel(bch), Viewers(v))    => Right(HOSTTARGET(ch, bch, v))
    case PING_REGEX(body)                                               => Right(PING(body))
    case ROOMSTATE_REGEX(RoomStateTags(t), Channel(ch))                 => Right(ROOMSTATE(t, ch))
    case CLEARMSG_REGEX(ClearMsgTags(t), Channel(ch), Message(msg))     => Right(CLEARMSG(t, ch, msg))
    case CLEARCHAT_REGEX(ClearChatTags(t), Channel(ch))                 => Right(CLEARCHAT(t, ch, None))
    case CLEARCHAT_REGEX(ClearChatTags(t), Channel(ch), BodyUser(u))    => Right(CLEARCHAT(t, ch, Some(u)))
    case NOTICE_REGEX(NoticeTags(t), Channel(ch), Message(msg))         => Right(NOTICE(t, ch, msg))
    case USERNOTICE_REGEX(UserNoticeTags(t), Channel(ch))               => Right(USERNOTICE(t, ch, None))
    case USERNOTICE_REGEX(UserNoticeTags(t), Channel(ch), Message(msg)) => Right(USERNOTICE(t, ch, Some(msg)))
    case USERSTATE_REGEX(UserStateTags(u), Channel(ch))                 => Right(USERSTATE(u, ch))
    case JOIN_REGEX(FullUser(u), Channel(ch))                           => Right(JOIN(u, ch))
    case PART_REGEX(FullUser(u), Channel(ch))                           => Right(PART(u, ch))
    case in                                                             => Left(in)
  }

object Outgoing:
  val toFrame: Outgoing => WebSocketFrame = {
    case Outgoing.PRIVMSG(Some(reply), channel, body) => WebSocketFrame.text(show"$reply PRIVMSG $channel $body")
    case Outgoing.PRIVMSG(None, channel, body)        => WebSocketFrame.text(show"PRIVMSG $channel $body")
    case Outgoing.PONG(body)                          => WebSocketFrame.text(show"PONG $body")
    case Outgoing.NICK(channel)                       => WebSocketFrame.text(show"NICK $channel")
    case Outgoing.JOIN(channel)                       => WebSocketFrame.text(show"JOIN $channel")
  }

object Auth:
  val toFrame: Auth => WebSocketFrame = {
    case Auth.CAP_REQ(caps) => WebSocketFrame.text(show"CAP REQ $caps")
    case Auth.PASS(token)   => WebSocketFrame.text(show"PASS oauth:$token")
  }
