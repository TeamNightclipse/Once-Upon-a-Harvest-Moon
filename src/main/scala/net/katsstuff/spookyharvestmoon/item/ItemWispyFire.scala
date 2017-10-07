package net.katsstuff.spookyharvestmoon.item

import scala.util.Random

import net.katsstuff.spookyharvestmoon.LibItemName
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.entity.EntityWispyFireball
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
      val snowball = new EntityWispyFireball(worldIn, player, new Vector3(player.getLookVec), 0.1D, 0.025D, 0xFFFFFF)
      worldIn.spawnEntity(snowball)
    }

    ActionResult.newResult(EnumActionResult.SUCCESS, stack)
  }

}
