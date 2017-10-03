package resourcegen

import io.circe._
import io.circe.syntax._

object Helper {

  def listToJson[A: Encoder](seq: Seq[A]): Json = {
    if (seq.nonEmpty) Some(seq) else None
  }.asJson

  def mapToJson[A: Encoder](map: Map[String, A]): Json = {
    if(map.nonEmpty) Some(map) else None
  }.asJson

}
