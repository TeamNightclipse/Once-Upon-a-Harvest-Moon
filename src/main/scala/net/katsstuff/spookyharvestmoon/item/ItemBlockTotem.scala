package net.katsstuff.spookyharvestmoon.item

import net.katsstuff.spookyharvestmoon.helper.ItemNBTHelper
import net.minecraft.block.Block
import net.minecraft.item.{ItemBlock, ItemStack}

class ItemBlockTotem(block: Block) extends ItemBlock(block) {

  override def getUnlocalizedName(stack: ItemStack): String = {
    if(ItemNBTHelper.getBoolean(stack, "Broken")) {
      super.getUnlocalizedName(stack) + ".broken"
    }
    else super.getUnlocalizedName(stack)
  }
}
