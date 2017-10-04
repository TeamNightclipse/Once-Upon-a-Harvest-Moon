package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.{EggInfo, SpookyConfig}
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraftforge.common.BiomeDictionary

object EntityJackOLantern {
  implicit val info: EntityInfoConfig[EntityJackOLantern] = new EntityInfoConfig[EntityJackOLantern] {
    override def create(world: World): EntityJackOLantern = new EntityJackOLantern(world)
    override def name:                 String             = LibEntityName.JackOLantern
    override def egg:                  Option[EggInfo]    = Some(EggInfo(0xFFFFFF, 0x000000))

    override def configEntry: SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.jackOLantern
    override def creatureType = EnumCreatureType.MONSTER
    override def biomes: Seq[Biome] = SpawnInfo.biomesForTypes(BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.FOREST)
  }
}
class EntityJackOLantern(_world: World) extends EntitySpookySpawnedMob(_world) {
  override def spawnEntry:                          SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.jackOLantern
  override def spawnBlockCheck(state: IBlockState): Boolean                        = state.getBlock == Blocks.GRASS
  override def lootTableName:                       String                         = LibEntityName.JackOLantern
}
