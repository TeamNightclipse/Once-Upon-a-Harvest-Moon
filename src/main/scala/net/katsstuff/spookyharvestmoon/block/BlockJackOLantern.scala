package net.katsstuff.spookyharvestmoon.block

import net.katsstuff.spookyharvestmoon.LibBlockName
import net.minecraft.block.{BlockPumpkin, SoundType}

class BlockJackOLantern extends BlockPumpkin with TBlockSpookyBase {
  setLightLevel(1F)
  setHardness(1F)
  setSoundType(SoundType.WOOD)
  setLightLevel(1.0F)

  override def name: String = LibBlockName.JackOLantern
}
