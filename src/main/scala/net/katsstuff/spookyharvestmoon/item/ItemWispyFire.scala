package net.katsstuff.spookyharvestmoon.item

import scala.util.Random

import net.katsstuff.spookyharvestmoon.LibItemName
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand, SoundCategory}
import net.minecraft.world.World

class ItemWispyFire extends ItemSpookyBase(LibItemName.WispyFire) {

  override def onItemRightClick(worldIn: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    val stack = player.getHeldItem(hand)

    if (!player.capabilities.isCreativeMode) stack.shrink(1)

    worldIn.playSound(
      null,
      player.posX,
      player.posY,
      player.posZ,
      SoundEvents.ITEM_FIRECHARGE_USE,
      SoundCategory.NEUTRAL,
      1F,
      (Random.nextFloat - Random.nextFloat) * 0.2F + 1F
    )

    if (!worldIn.isRemote) {
      val snowball = new EntitySnowball(worldIn, player)
      snowball.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F)
      worldIn.spawnEntity(snowball)
    }

    ActionResult.newResult(EnumActionResult.SUCCESS, stack)
  }

}
