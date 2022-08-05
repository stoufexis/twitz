package type_classes

trait Unwrap[A, Unwrapped]:
  def unwrap(a: A): Unwrapped

object Unwrap:
  def apply[A, U](using U: Unwrap[A, U]): Unwrap[A, U] = U

  extension [A, Unwrapped](a: A)
    def unwrap(using U: Unwrap[A, Unwrapped]): Unwrapped =
      U.unwrap(a)
