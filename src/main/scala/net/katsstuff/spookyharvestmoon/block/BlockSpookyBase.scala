package net.katsstuff.spookyharvestmoon.block

import net.katsstuff.spookyharvestmoon.SpookyCreativeTab
import net.minecraft.block.Block
import net.minecraft.block.material.Material

class BlockSpookyBase(name: String, material: Material) extends Block(material) {
  setCreativeTab(SpookyCreativeTab)
  setUnlocalizedName(name)
}
