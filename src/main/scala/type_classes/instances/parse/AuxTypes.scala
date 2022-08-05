package type_classes.instances.parse

import cats.{Semigroup, Show}

import model.AuxTypes.*

import type_classes.Read

given Read[Channel]     = readChannel
given Read[BodyChannel] = readBodyChannel
given Read[Message]     = readMessage
given Read[FullUser]    = readFullUser
given Read[PlainUser]   = readPlainUser
given Read[BodyUser]    = readBodyUser

given Show[Channel]      = showChannel
given Show[BodyChannel]  = showBodyChannel
given Show[Message]      = showMessage
given Show[FullUser]     = showFullUser
given Show[PlainUser]    = showPlainUser
given Show[BodyUser]     = showBodyUser
given Show[Capabilities] = showCapabilities
given Show[JoinChannels] = showJoinChannels
given Show[MessageId]    = showMessageId
given Show[AccessToken]  = showAccessToken

given Semigroup[Capabilities] = semigroupCapabilities
given Semigroup[JoinChannels] = semigroupJoinChannels
