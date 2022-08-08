package type_classes.instances.unwrap

import model.AuxTypes.*

import type_classes.Unwrap

given Unwrap[Channel, String]            = unwrapChannel
given Unwrap[BodyChannel, String]        = unwrapBodyChannel
given Unwrap[Message, String]            = unwrapMessage
given Unwrap[FullUser, String]           = unwrapFullUser
given Unwrap[PlainUser, String]          = unwrapPlainUser
given Unwrap[BodyUser, String]           = unwrapBodyUser
given Unwrap[JoinChannels, List[String]] = unwrapJoinChannels
given Unwrap[MessageId, String]          = unwrapMessageId
given Unwrap[AccessToken, String]        = unwrapAccessToken
given Unwrap[RefreshToken, String]       = unwrapRefreshToken
given Unwrap[Viewers, Int]               = unwrapViewers
