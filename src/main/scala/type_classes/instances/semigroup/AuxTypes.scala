package type_classes.instances.semigroup

import cats.Semigroup
import model.AuxTypes.*

given Semigroup[Capabilities] = semigroupCapabilities
given Semigroup[JoinChannels] = semigroupJoinChannels
