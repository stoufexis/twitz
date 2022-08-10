package model

import model.AuxTypes.{AccessToken, JoinChannels, RefreshToken}

case class Credentials(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
    clientId: String,
    clientSecret: String,
    channels: JoinChannels)
