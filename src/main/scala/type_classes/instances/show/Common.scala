package type_classes.instances.show

import cats.Show
import sttp.ws.WebSocketFrame

given [A: Show]: Show[Option[A]] =
  case None    => ""
  case Some(a) => Show[A].show(a)


given Show[WebSocketFrame] = frame => frame.toString