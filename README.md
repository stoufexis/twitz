## TwitZ

TwitZ is a Scala 3, ZIO 2 based twitch library which interfaces with the Twitch IRC API. It can be used to create
bots/chatbots which respond to events emitted by twitch. This library should mirror the Twitch IRC API specification as
documented here: https://dev.twitch.tv/docs/irc, and its understanding is assumed through the rest of this README.

### Dependencies

This project depends on `sttp - 3.x.x` and `zio - 2.x.x`

### Authentication

Authentication is handled by the `AuthenticationStore` service. To create an `AuthenticationStore` layer, the following
are required:

* An instance of the `Credentials` case class:

```scala
case class Credentials(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
    clientId: String,
    clientSecret: String,
    channels: JoinChannels)
```

The access token, refresh token, client id and client secret can be obtained by
following: https://dev.twitch.tv/docs/authentication.
This information will be used to initially authenticate with twitch, afterwards, the tokens will be automatically
refreshed whenever they expire.
The `channels` argument refer to the channels that are going to be observed for events.

* A layer of the `LocalStorage` service.

```scala
LocalStorage.layer(Paths.get("<BASE_DIRECTORY>"))
```

The `LocalStorage` service is responsible for saving refresh tokens to the local file system so they wont be lost on
shutdown/restart. It requires a base path and a layer of ZIO `Scope`.

* A layer of the `HttpClient` service

```scala
HttpClient.layer
```

The HttpClient requires a layer of `HttpClientZioBackend`

#### Example of creating an `AuthenticationStore`

```scala
import zio.{Scope, ZLayer}

import sttp.client3.httpclient.zio.HttpClientZioBackend

import service.authentication_store.AuthenticationStore
import service.http_client.HttpClient
import service.local_storage.LocalStorage

import model.AuxTypes.{AccessToken, JoinChannel, RefreshToken}
import model.Credentials

import java.nio.file.Paths

val info = Credentials(
  channels = JoinChannel("channel123", "channel321"),
  accessToken = AccessToken("abcdefghijklmnopqrstuvwxyz1234567890"),
  refreshToken = RefreshToken("1234567890abcdefghijklmnopqrstuvwxyz"),
  clientId = "123123123abcabcabcabcabc",
  clientSecret = "abcabcabcabcabcabcabcabc123123"
)

val auth =
  ZLayer.make[AuthenticationStore](
    AuthenticationStore.layer(info),
    HttpClient.layer,
    LocalStorage.layer(Paths.get("./tmp/")),
    Scope.default,
    HttpClientZioBackend.layer())

```

### Processing Events

The `TwitchChat` service expects a function from a `Stream` of `Incoming` events to a `Stream` of `Outgoing` events.

```scala
trait TwitchChat:
  def process(f: Stream[Throwable, Incoming] => Stream[Throwable, Outgoing]): Task[Response[Either[String, Unit]]]
```

A layer of `HttpClient` and `AuthenticationStore` are required to crreate a layer of `TwitchChat`.

```scala
  ZLayer.make[TwitchChat](
  TwitchChat.layer,
  HttpClient.layer,
  AuthenticationStore.layer(info),
  LocalStorage.layer(Paths.get("./tmp/")),
  Scope.default,
  HttpClientZioBackend.layer())
```

#### Incoming Events
Incoming events model events sent by the Twitch IRC server. *(see: https://dev.twitch.tv/docs/irc for reference)*

```scala
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
```

#### Outgoing Events
Outgoing events model events that can be sent to the Twitch IRC server. *(see: https://dev.twitch.tv/docs/irc for reference)*

```scala
enum Outgoing:
  case PRIVMSG(replyTo: Option[MessageId], channel: Channel, body: Message)
  case PONG(body: String)
  case NICK(channel: JoinChannels)
  case JOIN(channel: JoinChannels)
```

#### Notes
* Multiple outgoing events can be emitted as response to an incoming event by returning a stream of more than one element.
* Any effect can be evaluated while processing.
* No outgoing events can be emitted as response to an incoming event by returning an empty stream.

### Wrapper Types

Most types are wrapped with opaque types to enable type safety and specific functionality. A type can be wrapped using
its companion object's apply method and can be unwrapped using an instance of the `Unwrap` type class. The `Unwrap` type class
can also be used to wrap values in an equivalent way to the apply method.

```scala
trait Unwrap[A, T]:
  def unwrap(a: A): T
  def wrap(t: T): A
```

A string representation of a wrapped type can be obtained by bringing an instance of show into scope.

```scala
import type_classes.instances.show.given
```

### Tags

Most messages sent by the Twitch IRC server are accompanied by a number of tags that relay useful information about the 
event. Each type of tags has its own accompanying methods for accessing some specific information. 
*Note that the presence of specific tags is not guaranteed by the Twitch IRC server*

```scala
// for example
extension (privmsgTags: PrivmsgTags)
    def getBits: Option[Int]     = ???
    def getId: Option[MessageId] = ???

extension (userNoticeTags: UserNoticeTags)
    def getType: Option[UserNoticeType] = ???
    def getLogin: Option[PlainUser]     = ???

extension (noticeTags: NoticeTags)
    def getType: Option[NoticeType] = ???
```

### Extracting values from tags using pattern matching

A more expressive way to handle tags is enabled through `Extract`.

```scala
// Simplified
case class Extract[T, A](key: String, f: String => Option[A])(using U: Unwrap[T, Map[String, String]]):
    def unapply(tags: T): Option[(A, T)] = ???
```

Extract provides an unapply method that will match, and extract the value of, a specific tag and return the rest of the tags.
Tags are represented internally as a hashmap, thus, the first constructor argument specifies the key (name) of the tag. The second
parameter is used to transform the value of the tag to some other type.

To illustrate its usage, consider the following use case:

A `USERNOTICE` event contains most of its useful information withing its tags. It is emitted in a number of cases, like a user
subscribing, resubscribing, gifting a sub etc. Information about the cause and the user who triggered it is contained 
within the tags. 

An Extract instance for the event type can be defined as:
```scala
enum UserNoticeType:
  case SUB
  case RESUB
  case SUBGIFT
  case GIFT_PAID_UPGRADE
  case REWARD_GIFT
  case ANON_GIFT_PAID_UPGRADE
  case RAID
  case UNRAID
  case RITUAL
  case BITS_BADGE_TIER

val strToUserNoticeType: String => Option[UserNoticeType] = {
  case "sub"                 => Some(UserNoticeType.SUB)
  case "resub"               => Some(UserNoticeType.RESUB)
  case "subgift"             => Some(UserNoticeType.SUBGIFT)
  case "giftpaidupgrade"     => Some(UserNoticeType.GIFT_PAID_UPGRADE)
  case "rewardgift"          => Some(UserNoticeType.REWARD_GIFT)
  case "anongiftpaidupgrade" => Some(UserNoticeType.ANON_GIFT_PAID_UPGRADE)
  case "raid"                => Some(UserNoticeType.RAID)
  case "unraid"              => Some(UserNoticeType.UNRAID)
  case "ritual"              => Some(UserNoticeType.RITUAL)
  case "bitsbadgetier"       => Some(UserNoticeType.BITS_BADGE_TIER)
  case _                     => None
}

val Type = Extract[UserNoticeTags, UserNoticeType]("msg-id", strToUserNoticeType)
```

and for the username as:
```scala
val Login = Extract[UserNoticeTags, String]("login", x => Some(x))
```

Having defined those, a match case can be defined for every `USERNOTICE` event which was triggered by a subscription
or a re-subscription, which sends back a personalized message in the chat of the channel:
```scala
case Incoming.USERNOTICE(Type(UserNoticeType.SUB | UserNoticeType.RESUB, Login(name, _)), channel, _) =>
  ZStream(Outgoing.PRIVMSG(None, channel, Message(s"Thank you $name for subscribing!")))
```
Notice how the `Type` pattern extracts the event type and matches with only two of the 10 possible values. The rest of the tags are returned
on the second value of the pattern and are then matched with the Login pattern which extracts the login name of the user which
is also contained withing the tags. Finally, the rest of the tags are ignored. These Extract instances along with a few others
are already defined and can be imported as follows:

```scala
import model.*
```

### Testing

TODO