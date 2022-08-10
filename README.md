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

### Incoming Events

TODO

### Outgoing Events

TODO

### Interpreting Tags

TODO

### Wrapper Types

TODO

### Testing