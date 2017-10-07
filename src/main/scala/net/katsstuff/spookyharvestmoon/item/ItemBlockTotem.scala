package net.katsstuff.spookyharvestmoon.item

import net.katsstuff.spookyharvestmoon.helper.ItemNBTHelper
import net.minecraft.block.Block
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

class ItemBlockTotem(block: Block) extends ItemBlock(block) {
  addPropertyOverride(new ResourceLocation("broken"), (stack: ItemStack, _: World, _: EntityLivingBase) => {
    if (ItemNBTHelper.getBoolean(stack, "Broken")) 1F else 0F
  })

  override def getUnlocalizedName(stack: ItemStack): String = {
    if(ItemNBTHelper.getBoolean(stack, "Broken")) {
      super.getUnlocalizedName(stack) + ".broken"
    }
    else super.getUnlocalizedName(stack)
  }
}
