package type_classes.instances.parse

import cats.Show
import cats.syntax.all.*

import model.Auth.*
import model.AuxTypes.Capabilities
import model.{Auth, Outgoing}

given Show[Auth] =
  case Auth.CAP_REQ(caps) => show"CAP REQ $caps"
  case Auth.PASS(token)   => show"PASS oauth:$token"
