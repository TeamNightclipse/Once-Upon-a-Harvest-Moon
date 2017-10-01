package net.katsstuff.spookyharvestmoon

import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

object SpookyCreativeTab extends CreativeTabs(LibMod.Id) {
  override def getTabIconItem: ItemStack = new ItemStack(SpookyBlocks.Lantern)
}
