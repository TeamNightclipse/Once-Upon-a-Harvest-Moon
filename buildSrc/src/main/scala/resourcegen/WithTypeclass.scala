package resourcegen

import scala.annotation.unchecked.uncheckedVariance
import scala.language.higherKinds

case class WithTypeclass[+A, TC[_]] private(a: A, tc: TC[A @uncheckedVariance])
object WithTypeclass {
  implicit def mk[A, TC[_]](a: A)(implicit tc: TC[A]): WithTypeclass[A, TC] = WithTypeclass(a, tc)
}
