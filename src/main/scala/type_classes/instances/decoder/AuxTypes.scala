package type_classes.instances.decoder

import io.circe.Decoder

import model.AuxTypes.{AccessToken, RefreshToken, accessTokenDecoder, refreshTokenDecoder}

given Decoder[AccessToken]  = accessTokenDecoder
given Decoder[RefreshToken] = refreshTokenDecoder
