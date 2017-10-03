package resourcegen

import scala.language.implicitConversions

import io.circe._
import io.circe.syntax._
import net.katsstuff.typenbt.{Mojangson, NBTCompound}

trait Common {
  implicit def mkOption[A](a: A):                        Option[A]      = Option(a)
  implicit def mkSeq[A](a: A):                           Seq[A]         = Seq(a)
  implicit def mkSingle(i: Int):                         Single         = Single(i)
  implicit def mkSingleOpt(i: Int):                      Option[Single] = Single(i)
  implicit def mkRange(range: scala.Range.Inclusive):    Range          = Range(range.start, range.end)
  implicit def mkRangeOpt(range: scala.Range.Inclusive): Option[Range]  = Range(range.start, range.end)

  case class ResourceId(domain: String, path: String) {
    override def toString: String = s"$domain:$path"
  }
  object ResourceId {
    implicit def mkId(string: String): ResourceId = {
      if (string.contains(":")) {
        val strings = string.split(":", 2)
        ResourceId(strings(0), strings(1))
      } else ResourceId("minecraft", string)
    }
    implicit def mkIdOpt(string: String): Option[ResourceId]  = Some(string)
    implicit val encoder:                 Encoder[ResourceId] = (a: ResourceId) => a.toString.asJson
  }

  sealed trait RangeOrSingle
  object RangeOrSingle {
    implicit val encoder: Encoder[RangeOrSingle] = {
      case Single(i)       => i.asJson
      case Range(min, max) => Json.obj("min" -> min.asJson, "max" -> max.asJson)
    }
  }
  case class Single(i: Int)                                          extends RangeOrSingle
  case class Range(min: Option[Int] = None, max: Option[Int] = None) extends RangeOrSingle

  implicit def toText(string: String):    TextString         = TextString(string)
  implicit def toTextOpt(string: String): Option[TextString] = TextString(string)

  sealed trait TextOrString
  object TextOrString {
    implicit val encoder: Encoder[TextOrString] = {
      case TextString(s)       => s.asJson
      case text: TextComponent => text.asJson
    }
  }
  case class TextString(string: String) extends TextOrString
  case class TextComponent(
      text: Option[String] = None,
      translate: Option[String] = None,
      withTranslate: Seq[TextOrString] = Nil,
      score: Option[Score] = None,
      selector: Option[String] = None,
      keyBind: Option[String] = None,
      extra: Seq[TextComponent] = Nil,
      color: Option[Color] = None,
      bold: Option[Boolean] = None,
      italic: Option[Boolean] = None,
      underlined: Option[Boolean] = None,
      strikeThrough: Option[Boolean] = None,
      obfuscated: Option[Boolean] = None,
      insertion: Option[String] = None,
      clickEvent: Option[ClickAction] = None,
      hoverAction: Option[HoverAction] = None
  ) extends TextOrString
  object TextComponent {
    implicit val encoder: Encoder[TextComponent] = (a: TextComponent) => {
      Json.obj(
        "text"      -> a.text.asJson,
        "translate" -> a.translate.asJson,
        "with"      -> Helper.listToJson(a.withTranslate),
        "score" -> a.score.map { score =>
          Json.obj("name" -> score.name.asJson, "objective" -> score.objective.asJson, "value" -> score.value.asJson)
        }.asJson,
        "selector"      -> a.selector.asJson,
        "keybind"       -> a.keyBind.asJson,
        "extra"         -> Helper.listToJson(a.extra),
        "color"         -> a.color.map(_.name).asJson,
        "bold"          -> a.bold.asJson,
        "italic"        -> a.italic.asJson,
        "underlined"    -> a.underlined.asJson,
        "strikethrough" -> a.strikeThrough.asJson,
        "obfuscated"    -> a.obfuscated.asJson,
        "insertion"     -> a.insertion.asJson,
        "clickEvent" -> a.clickEvent.map { event =>
          Json.obj("action" -> event.name.asJson, "value" -> event.value.asJson)
        }.asJson,
        "hoverEvent" -> a.hoverAction.map { event =>
          Json.obj("action" -> event.name.asJson, "value" -> event.value.asJson)
        }.asJson
      )
    }
  }

  case class Score(name: String, objective: String, value: Option[String] = None)

  sealed abstract case class Color(name: String)
  object Color {
    object Black       extends Color("black")
    object DarkBlue    extends Color("dark_blue")
    object DarkGreen   extends Color("dark_green")
    object DarkAqua    extends Color("dark_aqua")
    object DarkRed     extends Color("dark_red")
    object DarkPurple  extends Color("dark_purple")
    object Gold        extends Color("gold")
    object Gray        extends Color("gray")
    object DarkGray    extends Color("dark_gray")
    object Blue        extends Color("blue")
    object Green       extends Color("green")
    object Aqua        extends Color("aqua")
    object Red         extends Color("red")
    object LightPurple extends Color("light_purple")
    object Yellow      extends Color("yellow")
    object White       extends Color("white")
    object Reset       extends Color("reset")
  }

  sealed trait ClickAction {
    def name:  String
    def value: String
  }
  object ClickAction {
    case class OpenUrl(value: String) extends ClickAction {
      override def name: String = "open_url"
    }
    case class OpenFile(value: String) extends ClickAction {
      override def name: String = "open_file"
    }
    case class RunCommand(value: String) extends ClickAction {
      override def name: String = "run_command"
    }
    case class ChangePage(value: String) extends ClickAction {
      override def name: String = "change_page"
    }
    case class SuggestCommand(value: String) extends ClickAction {
      override def name: String = "suggest_command"
    }
  }

  sealed trait HoverAction {
    def name:  String
    def value: String
  }
  object HoverAction {
    case class ShowText(textValue: TextComponent) extends HoverAction {
      override def name:  String = "show_text"
      override def value: String = textValue.asJson.noSpaces
    }
    case class ShowItem(nbtValue: NBTCompound) extends HoverAction {
      override def name:  String = "show_item"
      override def value: String = Mojangson.toMojangson(nbtValue)
    }
    case class ShowEntity(nbtValue: NBTCompound) extends HoverAction {
      override def name:  String = "show_entity"
      override def value: String = Mojangson.toMojangson(nbtValue)
    }
  }
}
object Common extends Common
