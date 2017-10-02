package net.katsstuff.spookyharvestmoon

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import net.katsstuff.spookyharvestmoon.block.BlockLantern
import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, IGlowParticle}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.entity._
import net.katsstuff.spookyharvestmoon.helper.IdState
import net.katsstuff.spookyharvestmoon.lib.{LibEntityName, LibMod}
import net.minecraft.block.Block
import net.minecraft.entity.{Entity, EntityLiving, EnumCreatureType}
import net.minecraft.item.{Item, ItemBlock}
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraftforge.common.BiomeDictionary
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityRegistry

object CommonProxy {

  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Block]): Unit =
    event.getRegistry.registerAll((new BlockLantern).setRegistryName(LibBlockName.Lantern))

  @SubscribeEvent
  def registerBlocks(event: RegistryEvent.Register[Item]): Unit =
    event.getRegistry.registerAll(new ItemBlock(SpookyBlocks.Lantern).setRegistryName(LibBlockName.Lantern))
}
case class Egg(primary: Int, secondary: Int)
class CommonProxy {

  def registerRenderers(): Unit = ()

  def bakeRenderModels(): Unit = ()

  def registerEntities(): Unit = {
    import BiomeDictionary.{Type => BiomeType}

    def registerEntity[A <: Entity](
        name: String,
        egg: Egg,
        trackingRange: Int = 64,
        updateFrequency: Int = 1,
        sendVelocityUpdates: Boolean = true
    )(implicit classTag: ClassTag[A]): IdState[Unit] =
      IdState(
        id =>
          (
            id + 1,
            EntityRegistry.registerModEntity(
              new ResourceLocation(LibMod.Id, name),
              classTag.runtimeClass.asInstanceOf[Class[A]],
              name,
              id,
              SpookyHarvestMoon,
              trackingRange,
              updateFrequency,
              sendVelocityUpdates,
              egg.primary,
              egg.secondary
            )
        )
      )

    def registerSpawn[A <: EntityLiving](
        entry: SpookyConfig.Spawns.SpawnEntry,
        creatureType: EnumCreatureType,
        biomeTypes: BiomeDictionary.Type*
    )(implicit classTag: ClassTag[A]): Unit =
      EntityRegistry.addSpawn(
        classTag.runtimeClass.asInstanceOf[Class[A]],
        entry.weightedProbability(),
        entry.minAmount(),
        entry.maxAmount(),
        creatureType,
        biomesForTypes(biomeTypes: _*): _*
      )

    def biomesForTypes(types: BiomeDictionary.Type*): Seq[Biome] =
      types.flatMap(BiomeDictionary.getBiomes(_).asScala).distinct

    IdState.run0 {
      for {
        _ <- IdState.init
        _ <- registerEntity[EntityJackOLantern](LibEntityName.JackOLantern, Egg(0xFFFFFF, 0x000000))
        _ <- registerEntity[EntityLanternMan](LibEntityName.LanternMan, Egg(0xFFFFFF, 0x000000))
        _ <- registerEntity[EntityMermaid](LibEntityName.Mermaid, Egg(0xFFFFFF, 0x000000))
        _ <- registerEntity[EntityWillOTheWisp](LibEntityName.WillOTheWisp, Egg(0xFFFFFF, 0x000000))
        _ <- registerEntity[EntityWitch](LibEntityName.Witch, Egg(0xFFFFFF, 0x000000))
      } yield ()
    }

    val spawns = SpookyConfig.spawns

    registerSpawn[EntityJackOLantern](spawns.jackOLantern, EnumCreatureType.MONSTER, BiomeType.SWAMP, BiomeType.FOREST)
    registerSpawn[EntityLanternMan](spawns.lanternMan, EnumCreatureType.MONSTER, BiomeType.SWAMP)
    registerSpawn[EntityMermaid](spawns.mermaid, EnumCreatureType.MONSTER, BiomeType.OCEAN)
    registerSpawn[EntityWillOTheWisp](spawns.willOTheWisp, EnumCreatureType.MONSTER, BiomeType.SWAMP)
  }

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

}
