package net.katsstuff.spookyharvestmoon.block

import net.katsstuff.spookyharvestmoon.SpookyCreativeTab
import net.minecraft.block.Block

trait TBlockSpookyBase extends Block {
  def name: String
  setCreativeTab(SpookyCreativeTab)
  setUnlocalizedName(name)
}
