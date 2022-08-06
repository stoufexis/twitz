package service.http_client

import io.circe.{Decoder, Error}
import zio.*
import zio.stream.*

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.model.*
import sttp.ws.WebSocketFrame

import common.*

type WSFunction       = ZStream[Any, Throwable, WebSocketFrame] => ZStream[Any, Throwable, WebSocketFrame]
type RequestResult[T] = Either[ResponseException[String, Error], T]
type WSResult         = Either[String, Unit]

trait HttpClient:
  def websocket(uri: Uri, f: WSFunction): Task[Response[WSResult]]

  def simpleRequest[T](request: Request[RequestResult[T], Any]): Task[Response[RequestResult[T]]]

object HttpClient:
  def websocket(uri: Uri, f: WSFunction): RIO[HttpClient, Response[WSResult]] =
    ZIO.serviceWithZIO(_.websocket(uri, f))

  def simpleRequest[T](request: Request[RequestResult[T], Any]): RIO[HttpClient, Response[RequestResult[T]]] =
    ZIO.serviceWithZIO(_.simpleRequest(request))

  val layer: URLayer[SttpBackend[Task, WebSockets & ZioStreams], HttpClient] =
    ZLayer.fromFunctionEnvironment { backend =>
      new:
        def websocket(uri: Uri, f: WSFunction): Task[Response[WSResult]] =
          basicRequest
            .get(uri)
            .response(asWebSocketStream(ZioStreams)(f))
            .sendZIO
            .provideEnvironment(backend)

        def simpleRequest[T](request: Request[RequestResult[T], Any]): Task[Response[RequestResult[T]]] =
          request
            .sendZIO
            .provideEnvironment(backend)
    }

  def mockLayer(
      wsInput: List[WebSocketFrame],
      wsOutput: Ref[List[WebSocketFrame]],
      fRequest: [T] => Request[RequestResult[T], Any] => Task[Response[RequestResult[T]]]): ULayer[HttpClient] =
    ZLayer.succeed {
      new HttpClient:
        def websocket(uri: Uri, f: WSFunction): Task[Response[WSResult]] =
          ZStream
            .fromIterable(wsInput)
            .viaFunction(f)
            .runFold[List[WebSocketFrame]](Nil)(_ :+ _)
            .flatMap(wsOutput.set(_)) *>
            ZIO.succeed(Response(Right(()), StatusCode.Ok))

        def simpleRequest[T](request: Request[RequestResult[T], Any]): Task[Response[RequestResult[T]]] =
          fRequest(request)
    }
