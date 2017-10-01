package net.katsstuff.spookyharvestmoon.item

import net.katsstuff.spookyharvestmoon.SpookyCreativeTab
import net.minecraft.item.Item

class ItemSpookyBase(name: String) extends Item {
  setCreativeTab(SpookyCreativeTab)
  setUnlocalizedName(name)
}
