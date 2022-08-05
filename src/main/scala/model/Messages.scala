package model

import model.AuxTypes.*
import model.Tags.*

enum Incoming:
  case PING(body: String)
  case PRIVMSG(tags: PrivmsgTags, from: FullUser, channel: Channel, message: Message)
  case ROOMSTATE(tags: RoomStateTags, channel: Channel)
  case GLOBALUSERSTATE(tags: GlobalUserStateTags)
  case CLEARMSG(tags: ClearMsgTags, channel: Channel, message: Message)
  case CLEARCHAT(tags: ClearChatTags, channel: Channel, user: Option[BodyUser])
  case HOSTTARGET(hostingChannel: Channel, channel: BodyChannel, viewers: Int)
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
