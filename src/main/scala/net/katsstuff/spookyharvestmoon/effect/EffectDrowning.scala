package net.katsstuff.spookyharvestmoon.effect

import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion

class EffectDrowning extends Potion(true, 0x00FF40) {

  override def performEffect(entity: EntityLivingBase, amplifier: Int): Unit = {
    if(entity.isInWater) {
      entity.motionY -= amplifier * 0.5D
      if(entity.ticksExisted % 20 == 0) {
        entity.setAir(entity.getAir - 1 * amplifier)
      }
    }
  }
}
