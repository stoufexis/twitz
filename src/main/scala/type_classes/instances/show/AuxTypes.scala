package type_classes.instances.show

import cats.Show

import model.AuxTypes.*

given Show[Channel]      = showChannelContent
given Show[BodyChannel]  = showBodyChannelContent
given Show[Message]      = showMessageContent
given Show[FullUser]     = showFullUserContent
given Show[PlainUser]    = showPlainUserContent
given Show[BodyUser]     = showBodyUserContent
given Show[AccessToken]  = showAccessToken
given Show[RefreshToken] = showRefreshToken
