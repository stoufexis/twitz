package type_classes

import scala.deriving.Mirror

import model.Incoming.PING

trait Read[+T]:
  def read(s: String): Either[String, T]

object Read:
  def apply[T](using read: Read[T]): Read[T]        = read
  def read[T: Read](str: String): Either[String, T] = Read[T].read(str)

  given Read[String] = Right(_)
  given Read[Int]    = i => i.toIntOption.toRight(i)

  given [A: Read]: Read[Option[A]] =
    case "" => Right(None)
    case s  => Read[A].read(s).map(Some(_))

  private val RNil = Right(EmptyTuple)

  extension [A: Read](s: String)
    private infix def +++[B <: Tuple](b: Either[String, B]): Either[String, A *: B] =
      for
        ao <- Read[A].read(s)
        ab <- b
      yield ao *: ab

  def readProduct[E1: Read, E2: Read, A](
      elem1: String,
      elem2: String)(
      using
      mirror: Mirror.ProductOf[A],
      ev: mirror.MirroredElemTypes =:= (E1, E2)): Either[String, A] =
    val e: Either[String, (E1, E2)] = elem1 +++ (elem2 +++ RNil)
    e.map(mirror.fromProduct)

  def readProduct[E1: Read, E2: Read, E3: Read, A](
      elem1: String,
      elem2: String,
      elem3: String)(
      using
      mirror: Mirror.ProductOf[A],
      ev: mirror.MirroredElemTypes =:= (E1, E2, E3)): Either[String, A] =
    val e: Either[String, (E1, E2, E3)] = elem1 +++ (elem2 +++ (elem3 +++ RNil))
    e.map(mirror.fromProduct)

  def readProduct[E1: Read, E2: Read, E3: Read, E4: Read, A](
      elem1: String,
      elem2: String,
      elem3: String,
      elem4: String)(
      using
      mirror: Mirror.ProductOf[A],
      ev: mirror.MirroredElemTypes =:= (E1, E2, E3, E4)): Either[String, A] =
    val e: Either[String, (E1, E2, E3, E4)] = elem1 +++ (elem2 +++ (elem3 +++ (elem4 +++ RNil)))
    e.map(mirror.fromProduct)
