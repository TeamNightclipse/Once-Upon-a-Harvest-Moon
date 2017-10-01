package net.katsstuff.spookyharvestmoon;

import net.minecraftforge.common.config.Config;

@Config(modid = JLibMod.ID)
public class SpookyConfig {

	public static Spawns spawns = new Spawns();

	public static class Spawns {

		public WillOTheWisp willOTheWisp = new WillOTheWisp();
		public Mermaid mermaid = new Mermaid();
		public LanternMan lanternMan = new LanternMan();
		public JackOLantern jackOLantern = new JackOLantern();

		public static class WillOTheWisp implements SpawnEntry {

			public int weightedProbability = 20;
			public int minAmount = 1;
			public int maxAmount = 2;
			public int lastProbability = 100;
			public int maxInChunk = 3;

			@Override
			public int weightedProbability() {
				return weightedProbability;
			}

			@Override
			public int minAmount() {
				return minAmount;
			}

			@Override
			public int maxAmount() {
				return maxAmount;
			}

			@Override
			public int lastProbability() {
				return lastProbability;
			}

			@Override
			public int maxInChunk() {
				return maxInChunk;
			}
		}

		public static class Mermaid implements SpawnEntry {

			public int weightedProbability = 15;
			public int minAmount = 1;
			public int maxAmount = 1;
			public int lastProbability = 100;
			public int maxInChunk = 1;

			@Override
			public int weightedProbability() {
				return weightedProbability;
			}

			@Override
			public int minAmount() {
				return minAmount;
			}

			@Override
			public int maxAmount() {
				return maxAmount;
			}

			@Override
			public int lastProbability() {
				return lastProbability;
			}

			@Override
			public int maxInChunk() {
				return maxInChunk;
			}
		}

		public static class LanternMan implements SpawnEntry {

			public int weightedProbability = 15;
			public int minAmount = 1;
			public int maxAmount = 1;
			public int lastProbability = 100;
			public int maxInChunk = 1;

			@Override
			public int weightedProbability() {
				return weightedProbability;
			}

			@Override
			public int minAmount() {
				return minAmount;
			}

			@Override
			public int maxAmount() {
				return maxAmount;
			}

			@Override
			public int lastProbability() {
				return lastProbability;
			}

			@Override
			public int maxInChunk() {
				return maxInChunk;
			}
		}

		public static class JackOLantern implements SpawnEntry {

			public int weightedProbability = 15;
			public int minAmount = 1;
			public int maxAmount = 2;
			public int lastProbability = 100;
			public int maxInChunk = 2;

			@Override
			public int weightedProbability() {
				return weightedProbability;
			}

			@Override
			public int minAmount() {
				return minAmount;
			}

			@Override
			public int maxAmount() {
				return maxAmount;
			}

			@Override
			public int lastProbability() {
				return lastProbability;
			}

			@Override
			public int maxInChunk() {
				return maxInChunk;
			}
		}

		public interface SpawnEntry {

			int weightedProbability();
			int minAmount();
			int maxAmount();

			int lastProbability();
			int maxInChunk();
		}
	}
}
