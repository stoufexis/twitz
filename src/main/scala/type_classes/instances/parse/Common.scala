package type_classes.instances.parse

import cats.Show

given [A: Show]: Show[Option[A]] =
  case None    => ""
  case Some(a) => Show[A].show(a)
