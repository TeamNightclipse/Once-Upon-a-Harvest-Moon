package net.katsstuff.spookyharvestmoon;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(JLibMod.ID)
public class SpookyBlocks {

	@GameRegistry.ObjectHolder(LibBlockName.Lantern)
	public static final Block Lantern = new Block(Material.IRON);

}
