package service.read_access_info.impl

import io.circe.Decoder
import zio.*
import zio.interop.catz.core.*

import cats.syntax.all.*

import sttp.client3.*
import sttp.client3.circe.*

import service.http_client.*
import service.local_storage.LocalStorage

import model.AccessInfo
import model.AuxTypes.{AccessToken, RefreshToken}

import common.*

import type_classes.instances.decoder.given

import java.io.{FileNotFoundException, IOException}
import java.nio.file.{Path, Paths}

case class RefreshResponse(
    access_token: AccessToken,
    refresh_token: RefreshToken,
    scope: List[String],
    token_type: String) derives Decoder

val path: Path = Paths.get("tokens.txt")

def updateInfo(info: AccessInfo): RIO[HttpClient, AccessInfo] =
  val AccessInfo(_, RefreshToken(refreshToken), clientId, clientSecret, _) = info

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
    response <- HttpClient.simpleRequest(request)
    body     <- ZIO.fromEither(response.body)
  yield info.copy(refreshToken = body.refresh_token, accessToken = body.access_token)

def getSavedInfo: RIO[LocalStorage, Option[(RefreshToken, AccessToken)]] =
  for
    savedInfo <- LocalStorage.get(path)
    out       <-
      savedInfo.map(_.splitList(" ")).traverse {
        case refresh :: access :: _ => ZIO.succeed((RefreshToken(refresh), AccessToken(access)))
        case content                => ZIO.fail(Exception(s"Corrupt file with content: $content"))
      }
  yield out

def writeTokens(refreshToken: RefreshToken, accessToken: AccessToken): ZIO[LocalStorage, IOException, Unit] =
  val RefreshToken(refresh) = refreshToken
  val AccessToken(access)   = accessToken
  LocalStorage.set(path, refresh + " " + access)

def updateSavedInfo(initial: AccessInfo): RIO[LocalStorage & HttpClient, AccessInfo] =
  for
    oldInfo <- getSavedInfo.map {
      case Some((refresh, access)) => initial.copy(refreshToken = refresh, accessToken = access)
      case None                    => initial
    }
    newInfo <- updateInfo(oldInfo)
    _       <- writeTokens(newInfo.refreshToken, newInfo.accessToken)
  yield newInfo
