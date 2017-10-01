package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.SpookyConfig
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class EntitySpookySpawnedMob(_world: World) extends EntitySpookyBaseMob(_world) {

  def spawnEntry: SpookyConfig.Spawns.SpawnEntry

  def spawnBlockCheck(state: IBlockState): Boolean

  override def getMaxSpawnedInChunk: Int = spawnEntry.maxInChunk

  override def getCanSpawnHere: Boolean =
    if (rand.nextInt(100) <= spawnEntry.lastProbability) {
      spawnBlockCheck(world.getBlockState(new BlockPos(posX, getEntityBoundingBox.minY, posZ).down)) && super.getCanSpawnHere
    } else false
}
