package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.{EggInfo, SpookyConfig}
import net.minecraft.entity.EnumCreatureType
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraftforge.common.BiomeDictionary
import scala.collection.JavaConverters._

trait EntityInfo[A] {
  def create(world: World): A

  def name: String
  def tracking: TrackingInfo      = TrackingInfo()
  def egg:      Option[EggInfo]   = None
  def spawn:    Option[SpawnInfo] = None
}
trait EntityInfoConfig[A] extends EntityInfo[A] {
  def configEntry:  SpookyConfig.Spawns.SpawnEntry
  def creatureType: EnumCreatureType
  def biomes:       Seq[Biome]

  def spawn: Option[SpawnInfo] =
    Some(
      SpawnInfo(
        creatureType,
        configEntry.weightedProbability(),
        configEntry.minAmount(),
        configEntry.maxAmount(),
        biomes
      )
    )
}

case class TrackingInfo(range: Int = 64, updateFrequency: Int = 1, sendVelocityUpdates: Boolean = true)
case class SpawnInfo(creatureType: EnumCreatureType, weight: Int, min: Int, max: Int, biomes: Seq[Biome])
object SpawnInfo {
  def apply(
      creatureType: EnumCreatureType,
      weight: Int,
      min: Int,
      max: Int,
      biomes: Seq[BiomeDictionary.Type]
  ): SpawnInfo = SpawnInfo(creatureType, weight, min, max, biomesForTypes(biomes: _*))

  def biomesForTypes(types: BiomeDictionary.Type*): Seq[Biome] =
    types.flatMap(BiomeDictionary.getBiomes(_).asScala).distinct
}
