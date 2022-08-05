package service.read_access_info.impl

import io.circe.Decoder
import zio.*
import zio.interop.catz.core.*

import cats.syntax.all.*

import sttp.client3.*
import sttp.client3.circe.*

import model.AccessInfo
import model.AuxTypes.{AccessToken, RefreshToken}

import common.*

import type_classes.Unwrap.unwrap
import type_classes.instances.decoder.given
import type_classes.instances.unwrap.given

import java.io.{FileNotFoundException, IOException}
import java.nio.file.Path

case class RefreshResponse(
    access_token: AccessToken,
    refresh_token: RefreshToken,
    scope: List[String],
    token_type: String)
    derives Decoder

def updateInfo(info: AccessInfo): RIO[SttpBackend[Task, Any], AccessInfo] =
  val AccessInfo(_, refreshToken, clientId, clientSecret, _) = info

  val body = s"grant_type=refresh_token" +
    s"&refresh_token=$refreshToken" +
    s"&client_id=$clientId" +
    s"&client_secret=$clientSecret"

  val request = basicRequest
    .post(uri"https://id.twitch.tv/oauth2/token")
    .contentType("application/x-www-form-urlencoded")
    .body(body)
    .response(asJson[RefreshResponse])

  for
    response <- request.sendZIO
    body     <- ZIO.fromEither(response.body)
  yield info.copy(refreshToken = body.refresh_token, accessToken = body.access_token)

def getSavedInfo(path: Path): RIO[Scope, Option[(RefreshToken, AccessToken)]] =
  for
    savedInfo <- ZIO.readFile(path)
      .map(Some(_))
      .catchSome { case _: FileNotFoundException => ZIO.succeed(None) }

    out <- savedInfo.traverse {
      _.splitList(" ") match
        case refresh :: access :: _ => ZIO.succeed((RefreshToken(refresh), AccessToken(access)))
        case content                => ZIO.fail(Exception(s"Corrupt file with content: $content"))
    }
  yield out

def writeTokens(path: Path, refreshToken: RefreshToken, accessToken: AccessToken): ZIO[Scope, IOException, Unit] =
  ZIO.writeFile(path, refreshToken.unwrap + " " + accessToken.unwrap)

def updateSavedInfo(initial: AccessInfo, path: Path): RIO[Scope & SttpBackend[Task, Any], AccessInfo] =
  for
    oldInfo <- getSavedInfo(path).map {
      case Some((refresh, access)) => initial.copy(refreshToken = refresh, accessToken = access)
      case None                    => initial
    }
    newInfo <- updateInfo(oldInfo)
    _       <- writeTokens(path, newInfo.refreshToken, newInfo.accessToken)
  yield newInfo
