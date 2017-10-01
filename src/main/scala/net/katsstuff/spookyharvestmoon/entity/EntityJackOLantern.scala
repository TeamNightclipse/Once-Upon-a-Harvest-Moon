package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.SpookyConfig
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.World

class EntityJackOLantern(_world: World) extends EntitySpookySpawnedMob(_world) {
  override def spawnEntry: SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.jackOLantern
  override def spawnBlockCheck(state: IBlockState): Boolean = state.getBlock == Blocks.GRASS
  override def lootTableName: String = LibEntityName.JackOLantern
}
