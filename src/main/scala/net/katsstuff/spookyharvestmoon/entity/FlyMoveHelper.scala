package net.katsstuff.spookyharvestmoon.entity

import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.EntityMoveHelper
import net.minecraft.util.math.MathHelper

class FlyMoveHelper(flyingMob: EntityFlyingMob) extends EntityMoveHelper(flyingMob) {

  override def onUpdateMoveHelper(): Unit = {
    if (flyingMob.flying) {
      if ((this.action == EntityMoveHelper.Action.MOVE_TO) && !this.flyingMob.getNavigator.noPath) {
        val dx = posX - flyingMob.posX
        var dy = posY - flyingMob.posY
        val dz = posZ - flyingMob.posZ

        val dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz)
        val f    = (MathHelper.atan2(dz, dx) * (180D / Math.PI)).toFloat - 90F

        dy = dy / dist

        flyingMob.rotationYaw = limitAngle(flyingMob.rotationYaw, f, 90.0F)
        flyingMob.renderYawOffset = flyingMob.rotationYaw
        val acceleration =
          (speed * flyingMob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue).toFloat
        this.flyingMob.setAIMoveSpeed(flyingMob.getAIMoveSpeed + (acceleration - flyingMob.getAIMoveSpeed) * 0.125F)

        var d4 = Math.sin((flyingMob.ticksExisted + flyingMob.getEntityId) * 0.5D) * 0.05D
        val d5 = Math.cos(flyingMob.rotationYaw * 0.017453292F)
        val d6 = Math.sin(flyingMob.rotationYaw * 0.017453292F)

        flyingMob.motionX += d4 * d5
        flyingMob.motionZ += d4 * d6
        d4 = Math.sin((flyingMob.ticksExisted + flyingMob.getEntityId) * 0.75D) * 0.05D

        flyingMob.motionY += d4 * (d6 + d5) * 0.25D
        flyingMob.motionY += flyingMob.getAIMoveSpeed * dy * 0.1D

        val d7 = flyingMob.posX + dx / dist * 2.0D
        val d8 = flyingMob.getEyeHeight + flyingMob.posY + dy / dist
        val d9 = flyingMob.posZ + dz / dist * 2.0D

        val lookHelper = flyingMob.getLookHelper
        val (d10, d11, d12) = if (lookHelper.getIsLooking) {
          (lookHelper.getLookPosX, lookHelper.getLookPosY, lookHelper.getLookPosZ)
        } else {
          (d7, d8, d9)
        }
        flyingMob.getLookHelper.setLookPosition(
          d10 + (d7 - d10) * 0.125D,
          d11 + (d8 - d11) * 0.125D,
          d12 + (d9 - d12) * 0.125D,
          10.0F,
          40.0F
        )
      } else {
        this.flyingMob.setAIMoveSpeed(0.0F)
      }
    } else super.onUpdateMoveHelper()
  }

}
