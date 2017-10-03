package runresourcegen

import io.circe.Encoder
import resourcegen.{AdvancementResource, WithTypeclass}

//noinspection TypeAnnotation
object Advancements extends AdvancementResource {
  import AdvancementApi._

  val ModId = "spookyharvestmoon"

  def spookyDisplay(name: String, icon: Icon, announceToChat: Option[Boolean] = None, frameType: Option[FrameType] = None): Display = Display(
    icon = icon,
    title = TextComponent(translate = s"$ModId.advancement.$name.title"),
    description = TextComponent(translate = s"$ModId.advancement.$name.description"),
    announceToChat = announceToChat
  )

  def haveItemCriteria(name: String, item: Item*): Map[String, WithTypeclass[InventoryChanged, Encoder]] =
    Map(s"have_$name" -> InventoryChanged(item))

  val root = Advancement(
    fileName = s"$ModId:story/root",
    display = Display(
      icon = Icon(item = s"$ModId:note", data = 0),
      title = TextComponent(translate = s"$ModId.advancement.story.root.title"),
      background = "minecraft:textures/gui/advancement/backgrounds/adventure.png",
      description = TextComponent(translate = s"$ModId.advancement.story.root.title"),
      announceToChat = false,
      showToast = true
    ),
    criteria = haveItemCriteria("note_0", Item(s"$ModId:note", 0)),
  )

  def findItemAdvancement(
      name: String,
      icon: Icon,
      parent: Parent,
      item: Item,
      loot: Seq[ResourceId]
  ): AdvancementCore =
    Advancement(
      fileName = s"$ModId:$name",
      display = spookyDisplay(name.replace('/', '.'), icon),
      parent = parent,
      criteria = haveItemCriteria(s"have_${icon.item.path}", item),
      rewards = Reward(loot = loot)
    )

  def findItemAdvancementSimple(itemDomain: String, itemPath: String, parent: Parent) = {
    val resource = ResourceId(itemDomain, itemPath)
    findItemAdvancement(s"story/$itemPath", Icon(resource), parent, Item(resource), Seq(s"$ModId:story/$itemPath"))
  }

  val spiderEye = findItemAdvancementSimple("minecraft", "spider_eye", root)
  val jackOLantern = findItemAdvancementSimple(ModId, "jack_o_lantern", spiderEye)
}
