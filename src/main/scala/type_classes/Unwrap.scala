package type_classes

trait Unwrap[A, T]:
  def unwrap(a: A): T

object Unwrap:
  def apply[A, T](using Unwrap[A, T]): Unwrap[A, T] = summon

  extension [A](a: A) def unwrap[T](using Unwrap[A, T]): T = Unwrap[A, T].unwrap(a)
