package net.katsstuff.spookyharvestmoon.effect

import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.potion.Potion

class PotionDrowning extends Potion(true, 0x00FF40) {

  override def isReady(duration: Int, amplifier: Int): Boolean = true

  override def performEffect(entity: EntityLivingBase, amplifier: Int): Unit = {
    if(!entity.isInWater) {
      LogHelper.info("NotWater")
    }

    if (entity.isInWater) {
      val change = (amplifier + 1) * 0.005D

      entity match {
        case player: EntityPlayer =>
          player.addVelocity(0, -change, 0)
          player match {
            case mp: EntityPlayerMP => mp.connection.sendPacket(new SPacketEntityVelocity(player))
            case _ =>
          }

        case _ =>
          entity.motionX += entity.motionX / 2
          entity.motionX += entity.motionX / 2
          entity.motionY -= change
      }

      if (entity.ticksExisted % 20 == 0) {
        entity.setAir(entity.getAir - 1 * (amplifier + 1))
      }
    }
  }
}
