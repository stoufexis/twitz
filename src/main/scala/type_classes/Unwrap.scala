package type_classes

trait Unwrap[A, T]:
  def unwrap(a: A): T
  def wrap(t: T): A

object Unwrap:
  def apply[A, T](using Unwrap[A, T]): Unwrap[A, T] = summon

  def make[A, T](f1: A => T, f2: T => A): Unwrap[A, T] =
    new:
      def unwrap(a: A) = f1(a)
      def wrap(t: T)   = f2(t)

  extension [A](a: A) def unwrap[T](using Unwrap[A, T]): T = Unwrap[A, T].unwrap(a)
