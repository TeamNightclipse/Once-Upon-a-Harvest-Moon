package net.katsstuff.spookyharvestmoon.entity

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.MoverType
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.pathfinding.PathNavigate
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class EntityFlyingMob(_world: World) extends EntitySpookySpawnedMob(_world) {
  var flying: Boolean = true
  moveHelper = new FlyMoveHelper(this)

  override protected def createNavigator(world: World): PathNavigate = new PathNavigateFlyer(this, world)

  override def travel(strafe: Float, vertical: Float, forward: Float): Unit = {
    if (isServerWorld && flying) {
      moveRelative(strafe, vertical, forward, 0.1F)
      move(MoverType.SELF, this.motionX, this.motionY, this.motionZ)
      motionX *= 0.9D
      motionY *= 0.9D
      motionZ *= 0.9D
    }
    else super.travel(strafe, vertical, forward)
  }

  override def isOnLadder: Boolean = !flying && super.isOnLadder

  override def fall(distance: Float, damageMultiplier: Float): Unit =
    if (!flying) super.fall(distance, damageMultiplier)

  override protected def updateFallState(y: Double, onGroundIn: Boolean, state: IBlockState, pos: BlockPos): Unit =
    if (!flying) super.updateFallState(y, onGroundIn, state, pos)

  override def getMaxFallHeight: Int = if (flying) 16 else super.getMaxFallHeight

  override def readEntityFromNBT(tag: NBTTagCompound): Unit = {
    super.readEntityFromNBT(tag)
    flying = tag.getBoolean("flying")
  }

  override def writeEntityToNBT(tag: NBTTagCompound): Unit = {
    super.writeEntityToNBT(tag)
    tag.setBoolean("flying", flying)
  }

  override protected def canTriggerWalking: Boolean = !flying
}
