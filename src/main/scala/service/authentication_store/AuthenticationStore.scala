package service.authentication_store

import io.circe.Decoder
import zio.*

import sttp.client3.*
import sttp.client3.circe.*

import service.authentication_store.impl.*
import service.http_client.HttpClient
import service.local_storage.LocalStorage

import model.Credentials

import common.*

import java.io.IOException
import java.nio.file.Path

trait AuthenticationStore:
  def get: Task[Credentials]

object AuthenticationStore:
  def get: RIO[AuthenticationStore, Credentials] = ZIO.serviceWithZIO(_.get)

  def layer(initial: Credentials): URLayer[LocalStorage & HttpClient, AuthenticationStore] =
    ZLayer.fromFunctionEnvironment { env =>
      new:
        def get = updateSavedInfo(initial).provideEnvironment(env)
    }

  def mockLayer(info: Credentials): ULayer[AuthenticationStore] =
    ZLayer.succeed {
      new:
        def get = ZIO.succeed(info)
    }
