package resourcegen

import java.io.File
import java.nio.file.{Files, Path}

import scala.collection.mutable.ArrayBuffer

import io.circe.syntax._
import scala.collection.JavaConverters._

import io.circe.Encoder

trait AdvancementResource {

  object AdvancementApi extends Advancement with Common
  import AdvancementApi._

  private val advancements = ArrayBuffer.empty[AdvancementCore]

  def Advancement(
      fileName: ResourceId,
      display: Display,
      parent: Option[Parent] = None,
      criteria: Map[String, WithTypeclass[Criteria, Encoder]],
      requirements: Seq[Seq[String]] = Nil,
      rewards: Option[Reward] = None
  ): AdvancementCore = {
    val advancement = AdvancementCore(
      fileName = fileName,
      display = display,
      parent = parent,
      criteria = criteria,
      requirements = requirements,
      rewards = rewards
    )

    advancements += advancement
    advancement
  }

  def advancementFileMap: Map[String, String] =
    advancements
      .map(a => s"assets/${a.fileName.domain}/advancements/${a.fileName.path}.json" -> a.asJson.pretty(ResourcePrinter))
      .toMap

  def advancementCreateFiles(resources: Path): Unit = {
    val map = advancementFileMap

    map.foreach {
      case (advPath, content) =>
        val path = resources.resolve(advPath)
        Files.createDirectories(path.getParent)
        Files.write(path, content.split("\n").toSeq.asJava)
    }
  }

  def advancementCreateFiles(resource: File): Unit = advancementCreateFiles(resource.toPath)
}
