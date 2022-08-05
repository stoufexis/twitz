package type_classes.instances.parse

import cats.Show

import sttp.ws.WebSocketFrame

given Show[WebSocketFrame] = _ match
  case WebSocketFrame.Binary(payload, finalFragment, rsv) =>
    s"BINARY ${payload.mkString("Array(", ", ", ")")}, $finalFragment, $rsv"

  case WebSocketFrame.Text(payload, finalFragment, rsv) => s"TEXT $payload, $finalFragment, $rsv"
  case WebSocketFrame.Ping(payload)                     => s"PING ${payload.mkString("Array(", ", ", ")")}"
  case WebSocketFrame.Pong(payload)                     => s"PONG ${payload.mkString("Array(", ", ", ")")}"
  case WebSocketFrame.Close(statusCode, reasonText)     => s"CLOSE $statusCode, $reasonText"
