package runresourcegen

import resourcegen.AdvancementResource

//noinspection TypeAnnotation
object Advancements extends AdvancementResource {
  import Api._

  val root = Advancement(
    fileName = "domain:foo",
    display = Display(
      icon = Icon(item = "minecraft:carrot"),
      title = "Foo title",
      background = "minecraft:textures/gui/advancement/backgrounds/adventure.png",
      description = TextComponent(translate = "foo.description")
    ),
    criteria = Map("have_carrot" -> InventoryChanged(Item(item = "minecraft:carrot"))),
  )

  val ModId = "domain"

  def standardDisplay(
      name: String,
      icon: Icon
  ): Display = Display(
    icon = icon,
    title = TextComponent(translate = s"domain.advancement.$name.title"),
    description = TextComponent(translate = s"domain.advancement.$name.description")
  )

  def ingotAdvancement(name: String, ore: String, parent: Parent): AdvancementCore = Advancement(
    fileName = s"$ModId:$name",
    display = standardDisplay(name, Icon(s"minecraft:${ore}_ingot")),
    parent = parent,
    criteria = Map(s"have_$ore" -> InventoryChanged(Item(item = s"minecraft:${ore}_ingot")))
  )

  val iron = ingotAdvancement("bar", "iron", root)
  val gold = ingotAdvancement("baz", "gold", iron)
}