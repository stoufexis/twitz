package service.read_access_info

import io.circe.Decoder
import zio.*

import sttp.client3.*
import sttp.client3.circe.*

import service.http_client.HttpClient
import service.read_access_info.impl.*

import model.AccessInfo

import common.*

import java.io.IOException
import java.nio.file.Path

trait ReadAccessInfo:
  def get: Task[AccessInfo]

object ReadAccessInfo:
  def get: RIO[ReadAccessInfo, AccessInfo] = ZIO.serviceWithZIO(_.get)

  def makeLayer(initial: AccessInfo, savePath: Path): URLayer[Scope & HttpClient, ReadAccessInfo] =
    ZLayer.fromFunctionEnvironment { env =>
      new:
        def get = updateSavedInfo(initial, savePath).provideEnvironment(env)
    }

  def makeNonFSLayer(initial: AccessInfo): URLayer[HttpClient, ReadAccessInfo] =
    ZLayer.fromFunctionEnvironment { backend =>
      new:
        def get = updateInfo(initial).provideEnvironment(backend)
    }

  def mockLayer(info: AccessInfo): ULayer[ReadAccessInfo] =
    ZLayer.succeed {
      new:
        def get = ZIO.succeed(info)
    }
