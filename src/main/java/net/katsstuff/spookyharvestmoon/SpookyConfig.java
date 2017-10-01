package net.katsstuff.spookyharvestmoon;

import net.minecraftforge.common.config.Config;

@Config(modid = JLibMod.Id)
public class SpookyConfig {

	public static Spawns spawns = new Spawns();

	public static class Spawns {

		public interface SpawnEntry {

			int weightedProbability();
			int minAmount();
			int maxAmount();

			int lastProbability();
			int maxInChunk();
		}
	}
}
