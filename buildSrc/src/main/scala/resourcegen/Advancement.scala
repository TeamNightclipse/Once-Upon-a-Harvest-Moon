package resourcegen

import scala.language.implicitConversions

import net.katsstuff.typenbt.{Mojangson, NBTCompound}
import io.circe._
import io.circe.syntax._

trait Advancement { self: Advancement with Common =>

  case class AdvancementCore(
      fileName: ResourceId,
      display: Display,
      parent: Option[Parent] = None,
      criteria: Map[String, WithTypeclass[Criteria, Encoder]],
      requirements: Seq[Seq[String]] = Nil,
      rewards: Option[Reward] = None
  ) extends Parent

  sealed trait Parent
  case class IdParent(string: ResourceId) extends Parent

  implicit val encoder: Encoder[AdvancementCore] = (a: AdvancementCore) => {
    Json.obj(
      "display" -> Json.obj(
        "icon"             -> Json.obj("item" -> a.display.icon.item.asJson, "data" -> a.display.icon.data.asJson),
        "title"            -> a.display.title.asJson,
        "frame"            -> a.display.frame.asJson,
        "background"       -> a.display.background.asJson,
        "description"      -> a.display.description.asJson,
        "show_toast"       -> a.display.showToast.asJson,
        "announce_to_chat" -> a.display.announceToChat.asJson,
        "hidden"           -> a.display.hidden.asJson,
      ),
      "parent" -> a.parent.map {
        case adv: AdvancementCore => adv.fileName
        case IdParent(id)         => id
      }.asJson,
      "criteria" -> Helper.mapToJson(a.criteria.map {
        case (name, WithTypeclass(criteria, criteriaEncoder)) =>
          name -> criteriaEncoder.apply(criteria)
      }),
      "requirements" -> Helper.listToJson(a.requirements),
      "rewards" -> a.rewards.map { rewards =>
        Json.obj(
          "recipes"    -> Helper.listToJson(rewards.recipes),
          "loot"       -> Helper.listToJson(rewards.loot),
          "experience" -> rewards.experience.asJson,
          "function"   -> rewards.function.asJson
        )
      }.asJson
    )
  }

  case class Display(
      icon: Icon,
      title: TextOrString,
      frame: FrameType = FrameType.Task,
      background: Option[ResourceId] = None,
      description: TextOrString,
      showToast: Boolean = true,
      announceToChat: Boolean = true,
      hidden: Boolean = false
  )

  case class Icon(item: ResourceId, data: Option[Int] = None)

  sealed trait FrameType
  object FrameType {
    case object Task      extends FrameType
    case object Goal      extends FrameType
    case object Challenge extends FrameType

    implicit val encoder: Encoder[FrameType] = {
      case Task      => Json.fromString("task")
      case Goal      => Json.fromString("goal")
      case Challenge => Json.fromString("challenge")
    }
  }

  case class Reward(
      recipes: Seq[ResourceId] = Nil,
      loot: Seq[ResourceId] = Nil,
      experience: Option[Int] = None,
      function: Option[String] = None
  )

  trait Criteria

  case class Item(
      count: Option[RangeOrSingle] = None,
      data: Option[Int] = None,
      durability: Option[RangeOrSingle] = None,
      enhancements: Seq[Enhancement] = Nil,
      item: Option[ResourceId] = None,
      nbt: Option[NBTCompound] = None,
      potion: Option[ResourceId] = None
  )
  object Item {
    implicit val ender: Encoder[Item] = (a: Item) =>
      Json.obj(
        "count"      -> a.count.asJson,
        "data"       -> a.data.asJson,
        "durability" -> a.durability.asJson,
        "enhancements" -> Helper.listToJson(a.enhancements.map { enhancement =>
          Json.obj("enhancement" -> enhancement.enchantment.asJson, "levels" -> enhancement.levels.asJson)
        }),
        "item"   -> a.item.asJson,
        "nbt"    -> a.nbt.map(Mojangson.toMojangson).asJson,
        "potion" -> a.potion.asJson
    )
  }

  case class Enhancement(enchantment: Option[ResourceId] = None, levels: Option[RangeOrSingle] = None)

  case class Entity(
      distance: Option[Distance] = None,
      effects: Seq[Effect] = Nil,
      location: Option[Location] = None,
      nbt: Option[NBTCompound] = None,
      tpe: Option[ResourceId] = None
  )
  object Entity {
    implicit val encoder: Encoder[Entity] = (a: Entity) => {
      Json.obj(
        "distance" -> a.distance.asJson,
        "effects"  -> Helper.mapToJson(a.effects.map(e => e.name.toString -> e).toMap),
        "location" -> a.location.asJson,
        "nbt"      -> a.nbt.map(Mojangson.toMojangson).asJson,
        "type"     -> a.tpe.asJson
      )
    }
  }

  case class Effect(
      name: ResourceId,
      amplifier: Option[RangeOrSingle] = None,
      duration: Option[RangeOrSingle] = None
  )
  object Effect {
    implicit val encoder: Encoder[Effect] = (a: Effect) =>
      Json.obj("amplifier" -> a.amplifier.asJson, "duration" -> a.duration.asJson)
  }

  case class Distance(
      absolute: Option[RangeOrSingle] = None,
      horizontal: Option[RangeOrSingle] = None,
      x: Option[RangeOrSingle] = None,
      y: Option[RangeOrSingle] = None,
      z: Option[RangeOrSingle] = None,
  )
  object Distance {
    implicit val encoder: Encoder[Distance] = (a: Distance) =>
      Json.obj(
        "absolute"   -> a.absolute.asJson,
        "horizontal" -> a.horizontal.asJson,
        "x"          -> a.x.asJson,
        "y"          -> a.y.asJson,
        "z"          -> a.z.asJson
    )
  }

  case class Location(
      biome: Option[String] = None,
      dimension: Option[String] = None,
      feature: Option[String] = None,
      position: Option[Position] = None
  )
  object Location {
    implicit val encoder: Encoder[Location] = (a: Location) =>
      Json.obj(
        "biome"     -> a.biome.asJson,
        "dimension" -> a.dimension.asJson,
        "feature"   -> a.feature.asJson,
        "position"  -> a.position.asJson
    )
  }

  case class Position(x: Option[RangeOrSingle] = None, y: Option[RangeOrSingle] = None, z: Option[RangeOrSingle] = None)
  object Position {
    implicit val encoder: Encoder[Position] = (a: Position) =>
      Json
        .obj("x" -> a.x.asJson, "y" -> a.y.asJson, "z" -> a.z.asJson)
  }

  case class BredAnimals(child: Option[Entity] = None, parent: Option[Entity] = None, partner: Option[Entity] = None)
      extends Criteria
  object BredAnimals {
    implicit val encoder: Encoder[BredAnimals] = (a: BredAnimals) =>
      Json.obj(
        "criteria"   -> "minecraft:bred_animals".asJson,
        "conditions" -> Json.obj("child" -> a.child.asJson, "parent" -> a.parent.asJson, "partner" -> a.parent.asJson)
    )
  }
  case class BrewedPotion(potion: Option[ResourceId] = None) extends Criteria
  object BrewedPotion {
    implicit val encoder: Encoder[BrewedPotion] = (a: BrewedPotion) =>
      Json.obj("trigger" -> "minecraft:brewed_potion".asJson, "conditions" -> Json.obj("potion" -> a.potion.asJson))
  }
  case class ChangedDimension(from: Option[String] = None, to: Option[String] = None) extends Criteria
  object ChangedDimension {
    implicit val encoder: Encoder[ChangedDimension] = (a: ChangedDimension) =>
      Json.obj(
        "trigger"    -> "minecraft:changed_dimension".asJson,
        "conditions" -> Json.obj("from" -> a.from.asJson, "to" -> a.to.asJson)
    )
  }
  case class ConstructBeacon(level: Option[RangeOrSingle] = None) extends Criteria
  object ConstructBeacon {
    implicit val encoder: Encoder[ConstructBeacon] = (a: ConstructBeacon) =>
      Json.obj("trigger" -> "minecraft:construct_beacon".asJson, "conditions" -> Json.obj("level" -> a.level.asJson))
  }
  case class ConsumeItem(item: Option[Item] = None) extends Criteria
  object ConsumeItem {
    implicit val encoder: Encoder[ConsumeItem] = (a: ConsumeItem) =>
      Json.obj("trigger" -> "minecraft:consume_item".asJson, "conditions" -> Json.obj("item" -> a.item.asJson))
  }
  case class CuredZombieVillager(villager: Option[Entity] = None, zombie: Option[Entity] = None) extends Criteria
  object CuredZombieVillager {
    implicit val encoder: Encoder[CuredZombieVillager] = (a: CuredZombieVillager) =>
      Json.obj(
        "trigger"    -> "minecraft:cured_zombie_villager".asJson,
        "conditions" -> Json.obj("villager" -> a.villager.asJson, "zombie" -> a.zombie.asJson)
    )
  }
  case class EffectsChanged(effects: Seq[Effect] = Nil) extends Criteria
  object EffectsChanged {
    implicit val encoder: Encoder[EffectsChanged] = (a: EffectsChanged) =>
      Json.obj(
        "trigger"    -> "minecraft:effects_changed".asJson,
        "conditions" -> Json.obj("effects" -> Helper.mapToJson(a.effects.map(e => e.name.toString -> e).toMap))
    )
  }
  case class EnchantedItem(item: Option[Item] = None, levels: Option[RangeOrSingle] = None) extends Criteria
  object EnchantedItem {
    implicit val encoder: Encoder[EnchantedItem] = (a: EnchantedItem) =>
      Json.obj(
        "trigger"    -> "minecraft:enchanted_item".asJson,
        "conditions" -> Json.obj("item" -> a.item.asJson, "levels" -> a.levels.asJson)
    )
  }
  case class EnterBlock(block: Option[ResourceId] = None, state: Map[String, String] = Map.empty) extends Criteria
  object EnterBlock {
    implicit val encoder: Encoder[EnterBlock] = (a: EnterBlock) =>
      Json.obj(
        "trigger"    -> "minecraft:enter_block".asJson,
        "conditions" -> Json.obj("block" -> a.block.asJson, "state" -> Helper.mapToJson(a.state))
    )
  }
  case class EntityHurtPlayer(damage: Option[Damage] = None) extends Criteria
  object EntityHurtPlayer {
    implicit val encoder: Encoder[EntityHurtPlayer] = (a: EntityHurtPlayer) =>
      Json.obj(
        "trigger"    -> "minecraft:entity_hurt_player".asJson,
        "conditions" -> Json.obj("damage" -> a.damage.asJson)
    )
  }

  case class Damage(
      blocked: Option[Boolean] = None,
      dealt: Option[RangeOrSingle] = None,
      directEntity: Option[Entity] = None,
      sourceEntity: Option[Entity] = None,
      taken: Option[RangeOrSingle] = None,
      tpe: Option[DamageType] = None
  )
  object Damage {
    implicit val encoder: Encoder[Damage] = (a: Damage) =>
      Json.obj(
        "blocked"       -> a.blocked.asJson,
        "dealt"         -> a.dealt.asJson,
        "direct_entity" -> a.directEntity.asJson,
        "source_entity" -> a.sourceEntity.asJson,
        "taken"         -> a.taken.asJson,
        "type"          -> a.tpe.asJson
    )
  }

  case class DamageType(
      bypassesArmor: Option[Boolean] = None,
      bypassesInvulnerability: Option[Boolean] = None,
      bypassesMagic: Option[Boolean] = None,
      directEntity: Option[Entity] = None,
      isExplosion: Option[Boolean] = None,
      isFire: Option[Boolean] = None,
      isMagic: Option[Boolean] = None,
      isProjectile: Option[Boolean] = None,
      sourceEntity: Option[Entity] = None,
  )
  object DamageType {
    implicit val encoder: Encoder[DamageType] = (a: DamageType) =>
      Json.obj(
        "bypasses_armor"           -> a.bypassesArmor.asJson,
        "bypasses_invulnerability" -> a.bypassesInvulnerability.asJson,
        "direct_entity"            -> a.directEntity.asJson,
        "is_explosion"             -> a.isExplosion.asJson,
        "is_fire"                  -> a.isFire.asJson,
        "is_magic"                 -> a.isMagic.asJson,
        "is_projectile"            -> a.isProjectile.asJson,
        "source_entity"            -> a.sourceEntity.asJson
    )
  }

  case class EntityKilledPlayer(entity: Option[Entity] = None, killingBlow: Option[DamageType] = None) extends Criteria
  object EntityKilledPlayer {
    implicit val encoder: Encoder[EntityKilledPlayer] = (a: EntityKilledPlayer) =>
      Json.obj(
        "trigger"    -> "minecraft:entity_killed_player".asJson,
        "conditions" -> Json.obj("entity" -> a.entity.asJson, "killing_blow" -> a.killingBlow.asJson)
    )
  }
  case object Impossible extends Criteria
  implicit val impossibleEncoder: Encoder[Impossible.type] = (_: Impossible.type) =>
    Json.obj("trigger" -> "minecraft:impossible".asJson)

  case class InventoryChanged(items: Seq[Item] = Nil, slots: Option[Slots] = None) extends Criteria
  object InventoryChanged {
    implicit val encoder: Encoder[InventoryChanged] = (a: InventoryChanged) =>
      Json.obj(
        "trigger"    -> "minecraft:inventory_changed".asJson,
        "conditions" -> Json.obj("items" -> Helper.listToJson(a.items), "slots" -> a.slots.asJson)
    )
  }
  case class Slots(
      empty: Option[RangeOrSingle] = None,
      full: Option[RangeOrSingle] = None,
      occupied: Option[RangeOrSingle] = None
  )
  object Slots {
    implicit val encoder: Encoder[Slots] = (a: Slots) =>
      Json.obj("empty" -> a.empty.asJson, "full" -> a.full.asJson, "occupied" -> a.occupied.asJson)
  }
  case class ItemDurabilityChanged(
      delta: Option[RangeOrSingle] = None,
      durability: Option[RangeOrSingle] = None,
      item: Option[Item] = None
  ) extends Criteria
  object ItemDurabilityChanged {
    implicit val encoder: Encoder[ItemDurabilityChanged] = (a: ItemDurabilityChanged) =>
      Json.obj(
        "trigger" -> "minecraft:item_durability_changed".asJson,
        "conditions" -> Json
          .obj("delta" -> a.delta.asJson, "durability" -> a.durability.asJson, "item" -> a.item.asJson)
    )
  }

  case class Levitation(distance: Option[Distance] = None, duration: Option[RangeOrSingle] = None) extends Criteria
  object Levitation {
    implicit val encoder: Encoder[Levitation] = (a: Levitation) =>
      Json.obj(
        "trigger"    -> "minecraft:levitation".asJson,
        "conditions" -> Json.obj("distance" -> a.distance.asJson, "duration" -> a.duration.asJson)
    )
  }
  case class LocationCheck(location: Option[Location] = None) extends Criteria
  object LocationCheck {
    implicit val encoder: Encoder[LocationCheck] = (a: LocationCheck) =>
      Json.obj("trigger" -> "minecraft:location".asJson, "conditions" -> a.location.asJson)
  }
  case class NetherTravel(distance: Option[Distance] = None) extends Criteria
  object NetherTravel {
    implicit val encoder: Encoder[NetherTravel] = (a: NetherTravel) =>
      Json.obj("trigger" -> "minecraft:nether_travel".asJson, "conditions" -> Json.obj("distance" -> a.distance.asJson))
  }
  case class PlacedBlock(
      block: Option[ResourceId] = None,
      item: Option[Item] = None,
      location: Option[Location] = None,
      state: Map[String, String] = Map.empty
  ) extends Criteria
  object PlacedBlock {
    implicit val encoder: Encoder[PlacedBlock] = (a: PlacedBlock) =>
      Json.obj(
        "trigger" -> "minecraft:placed_block".asJson,
        "conditions" -> Json.obj(
          "block"    -> a.block.asJson,
          "item"     -> a.item.asJson,
          "location" -> a.location.asJson,
          "state"    -> Helper.mapToJson(a.state)
        )
    )
  }

  case class PlayerHurtEntity(damage: Option[Damage] = None, entity: Option[Entity] = None) extends Criteria
  object PlayerHurtEntity {
    implicit val encoder: Encoder[PlayerHurtEntity] = (a: PlayerHurtEntity) =>
      Json.obj(
        "trigger"    -> "minecraft:player_hurt_entity".asJson,
        "conditions" -> Json.obj("damage" -> a.damage.asJson, "entity" -> a.entity.asJson)
    )
  }
  case class PlayerKilledEntity(entity: Option[Entity] = None, killingBlow: Option[DamageType] = None) extends Criteria
  object PlayerKilledEntity {
    implicit val encoder: Encoder[PlayerKilledEntity] = (a: PlayerKilledEntity) =>
      Json.obj(
        "trigger"    -> "minecraft:player_killed_entity".asJson,
        "conditions" -> Json.obj("entity" -> a.entity.asJson, "killing_blow" -> a.killingBlow.asJson)
    )
  }
  case class RecipeUnlocked(recipe: Option[String] = None) extends Criteria
  object RecipeUnlocked {
    implicit val encoder: Encoder[RecipeUnlocked] = (a: RecipeUnlocked) =>
      Json.obj("trigger" -> "minecraft:recipe_unlocked".asJson, "conditions" -> Json.obj("recipe" -> a.recipe.asJson))
  }
  case class SleptInBed(location: Option[Location] = None) extends Criteria
  object SleptInBed {
    implicit val encoder: Encoder[SleptInBed] = (a: SleptInBed) =>
      Json.obj("trigger" -> "minecraft:slept_in_bed".asJson, "conditions" -> a.location.asJson)
  }
  case class SummonedEntity(entity: Option[Entity] = None) extends Criteria
  object SummonedEntity {
    implicit val encoder: Encoder[SummonedEntity] = (a: SummonedEntity) =>
      Json.obj("trigger" -> "minecraft:summoned_entity".asJson, "conditions" -> Json.obj("entity" -> a.entity.asJson))
  }
  case class TameAnimal(entity: Option[Entity] = None) extends Criteria
  object TameAnimal {
    implicit val encoder: Encoder[TameAnimal] = (a: TameAnimal) =>
      Json.obj("trigger" -> "minecraft:tame_animal".asJson, "conditions" -> Json.obj("entity" -> a.entity.asJson))
  }
  case object Tick extends Criteria
  implicit val tickEncoder: Encoder[Tick.type] = (_: Tick.type) => Json.obj("trigger" -> "minecraft:tick".asJson)
  case class UsedEnderEye(distance: Option[RangeOrSingle] = None) extends Criteria
  object UsedEnderEye {
    implicit val encoder: Encoder[UsedEnderEye] = (a: UsedEnderEye) =>
      Json.obj(
        "trigger"    -> "minecraft:used_ender_eye".asJson,
        "conditions" -> Json.obj("distance" -> a.distance.asJson)
    )
  }
  case class UsedTotem(item: Option[Item] = None) extends Criteria
  object UsedTotem {
    implicit val encoder: Encoder[UsedTotem] = (a: UsedTotem) =>
      Json.obj("trigger" -> "minecraft:used_totem".asJson, "conditions" -> Json.obj("item" -> a.item.asJson))
  }
  case class VillagerTrade(item: Option[Item] = None, villager: Option[Entity] = None) extends Criteria
  object VillagerTrade {
    implicit val encoder: Encoder[VillagerTrade] = (a: VillagerTrade) =>
      Json.obj(
        "trigger"    -> "minecraft:villager_trade".asJson,
        "conditions" -> Json.obj("item" -> a.item.asJson, "villager" -> a.villager.asJson)
    )
  }
}
object Advancement extends Advancement with Common
