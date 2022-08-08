package service.local_storage

import zio.*

import scala.collection.immutable.HashMap

import common.*

import java.io.{FileNotFoundException, IOException}
import java.nio.file.{Path, Paths}

trait LocalStorage:
  def set(path: Path, content: String): IO[IOException, Unit]
  def get(path: Path): IO[IOException, Option[String]]

object LocalStorage:
  def set(path: Path, content: String): ZIO[LocalStorage, IOException, Unit] =
    ZIO.serviceWithZIO(_.set(path, content))

  def get(path: Path): ZIO[LocalStorage, IOException, Option[String]] =
    ZIO.serviceWithZIO(_.get(path))

  def layer(basePath: Path): URLayer[Scope, LocalStorage] =
    ZLayer.fromFunctionEnvironment { scope =>
      new:
        def set(path: Path, content: String) =
          ZIO.writeFile(basePath.resolve(path), content)
            .provideEnvironment(scope)

        def get(path: Path) =
          ZIO.readFile(basePath.resolve(path))
            .map(Some(_))
            .catchSome { case _: FileNotFoundException => ZIO.succeed(None) }
            .provideEnvironment(scope)
    }

  def mockLayer: ULayer[LocalStorage] =
    ZLayer.fromZIO {
      for
        storage <- Ref.make(HashMap[String, String]())
      yield new:
        def set(path: Path, content: String) =
          for
            oldMap <- storage.get
            _      <- storage.set(oldMap.updated(path.toString, content))
          yield ()

        def get(path: Path) = storage.get.map(_.get(path.toString))
    }