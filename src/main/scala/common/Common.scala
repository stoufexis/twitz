package common

import zio.*
import zio.stream.*

import cats.Show
import cats.syntax.all.*

import sttp.client3.{Request, Response, SttpBackend}
import sttp.ws.WebSocketFrame

import scala.annotation.tailrec
import scala.compiletime.*
import scala.compiletime.ops.int.*
import scala.deriving.Mirror
import scala.util.matching.Regex

def printStream[A: Show](a: A): ZStream[Any, Throwable, Unit] =
  ZStream.execute(Console.printLine(Show[A].show(a)))

def printIgnore[A: Show](a: A): ZStream[Any, Throwable, Nothing] =
  printStream(a) *> ZStream.empty

extension (s: String)
  def splitList(regex: String): List[String] =
    s.split(regex).toList

def matchEither[A](regex: Regex, f: String => A): String => Either[String, A] =
  case regex(str) => Right(f(str))
  case s          => Left(s)

def matchOption(regex: Regex): String => Option[String] =
  case regex(str) => Some(str)
  case _          => None

extension (_s: ZStream.type)
  def fromOption[A]: Option[A] => ZStream[Any, Nothing, A] =
    case Some(value) => ZStream.succeed(value)
    case None        => ZStream.empty

extension (_l: ZLayer.type)
  def fromFunctionEnvironment[Rin, Rout: Tag](f: ZEnvironment[Rin] => Rout): URLayer[Rin, Rout] =
    for
      env <- ZLayer.environment[Rin]
      out <- ZLayer.succeed(f(env))
    yield out

extension [T, R](request: Request[T, R])
  inline def sendZIO: RIO[SttpBackend[Task, R], Response[T]] =
    ZIO.serviceWithZIO(request.send(_))

def toFrame[A: Show](a: A): WebSocketFrame =
  WebSocketFrame.text(Show[A].show(a))
