## TwitZ

TwitZ is a Scala 3, ZIO 2 based twitch library which interfaces with the Twitch IRC API. It can be used to create
bots/chatbots which respond to events emitted by twitch. This library should mirror the Twitch IRC API specification as
documented here: https://dev.twitch.tv/docs/irc

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
The `channels` provided are the channels that are going to be observed for events

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

The HttpClient requires a layer of `https://dev.twitch.tv/docs/authentication`

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
* Multiple outgoing events can be emitted as response to an incoming event by returning a stream of more that one element.
* Any effect can be evaluated while processing
* No outgoing events can be emitted as response to an incoming by returning an empty stream

### Interpreting Tags

TODO

### Wrapper Types

TODO

### Testing