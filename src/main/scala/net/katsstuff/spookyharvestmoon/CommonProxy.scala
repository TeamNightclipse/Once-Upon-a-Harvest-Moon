package net.katsstuff.spookyharvestmoon

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import net.katsstuff.spookyharvestmoon.block.{BlockHook, BlockJackOLantern, BlockLantern, BlockTotem}
import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, IGlowParticle}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.effect.PotionDrowning
import net.katsstuff.spookyharvestmoon.entity._
import net.katsstuff.spookyharvestmoon.helper.IdState
import net.katsstuff.spookyharvestmoon.item.{ItemBlockTotem, ItemNote, ItemWispyFire}
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.item.{Item, ItemBlock}
import net.minecraft.potion.Potion
import net.minecraft.world.World
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder}

object CommonProxy {

  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Block]): Unit =
    event.getRegistry.registerAll(
      new BlockLantern().setRegistryName(LibBlockName.Lantern),
      new BlockHook().setRegistryName(LibBlockName.Hook),
      new BlockTotem().setRegistryName(LibBlockName.Totem)
    )

  @SubscribeEvent
  def registerBlocks(event: RegistryEvent.Register[Item]): Unit =
    event.getRegistry.registerAll(
      new ItemBlock(SpookyBlocks.Lantern).setRegistryName(LibBlockName.Lantern),
      new ItemBlock(SpookyBlocks.Hook).setRegistryName(LibBlockName.Hook),
      new ItemBlock(SpookyBlocks.JackOLantern).setRegistryName(LibBlockName.JackOLantern),
      new ItemNote().setRegistryName(LibItemName.Note),
      new ItemWispyFire().setRegistryName(LibItemName.WispyFire),
      new ItemBlockTotem(SpookyBlocks.Totem).setRegistryName(LibBlockName.Totem)
    )

  @SubscribeEvent
  def registerPotions(event: RegistryEvent.Register[Potion]): Unit =
    event.getRegistry.registerAll((new PotionDrowning).setRegistryName(LibEffectName.Drowning))

  @SubscribeEvent
  def registerEntities(event: RegistryEvent.Register[EntityEntry]): Unit = {

    def registerEntity[A <: Entity](implicit classTag: ClassTag[A], info: EntityInfo[A]): IdState[EntityEntry] = {
      val clazz = classTag.runtimeClass.asInstanceOf[Class[A]]
      IdState { id =>
        (id + 1, {
          val builder = EntityEntryBuilder
            .create[A]
            .name(info.name)
            .id(info.name, id)
            .entity(clazz)
            .factory(world => info.create(world))
            .tracker(info.tracking.range, info.tracking.updateFrequency, info.tracking.sendVelocityUpdates)

          info.spawn.foreach(s => builder.spawn(s.creatureType, s.weight, s.min, s.max, s.biomes.asJava))
          info.egg.foreach(egg => builder.egg(egg.primary, egg.secondary))

          builder.build()
        })
      }
    }

    event.getRegistry.registerAll(IdState.run0 {
      for {
        _            <- IdState.init
        jackOLantern <- registerEntity[EntityJackOLantern]
        lanternMan   <- registerEntity[EntityLanternMan]
        mermaid      <- registerEntity[EntityMermaid]
        wisp         <- registerEntity[EntityWillOTheWisp]
        witch        <- registerEntity[EntityWitch]
        fireball     <- registerEntity[EntityWispyFireball]
      } yield Seq(jackOLantern, lanternMan, mermaid, wisp, witch, fireball)
    }: _*)
  }
}

case class EggInfo(primary: Int, secondary: Int)
class CommonProxy {

  def registerRenderers(): Unit = ()

  def bakeRenderModels(): Unit = ()

  def registerEntities(): Unit = {}

  def spawnParticleGlow(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      texture: GlowTexture
  ): Unit = {}

  def addParticle(particle: IGlowParticle): Unit = {}

  def isInRenderRange(entity: Entity): Boolean = false

}
