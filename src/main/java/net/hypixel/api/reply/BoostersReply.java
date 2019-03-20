package net.hypixel.api.reply;

import net.hypixel.api.util.GameType;

import java.util.List;

public class BoostersReply extends AbstractReply {
	private List<Booster> boosters;
	private BoosterState boosterState;

	public List<Booster> getBoosters() {
		return boosters;
	}

	public BoosterState getBoosterState() {
		return boosterState;
	}

	@Override
	public String toString() {
		return "BoostersReply{" +
				"boosters=" + boosters +
				", boosterState=" + boosterState +
				"} " + super.toString();
	}

	public static class BoosterState {
		private boolean decrementing;

		public boolean isDecrementing() {
			return decrementing;
		}

		@Override
		public String toString() {
			return "BoosterState{" +
					"decrementing=" + decrementing +
					'}';
		}
	}

	public static class Booster {
		private String purchaserUuid;
		private double amount;
		private int originalLength;
		private int length;
		private GameType gameType;
		private long dateActivated;
		private List<String> stacked;
		private boolean queuedToStack;

		public String getPurchaserUuid() {
			return purchaserUuid;
		}

		public double getAmount() {
			return amount;
		}

		public int getOriginalLength() {
			return originalLength;
		}

		public int getLength() {
			return length;
		}

		public GameType getGameType() {
			return gameType;
		}

		public long getDateActivated() {
			return dateActivated;
		}

		public List<String> getStacked() {
			return stacked;
		}

		public boolean isQueuedToStack() {
			return queuedToStack;
		}

		@Override
		public String toString() {
			return "Booster{" +
					"purchaserUuid=" + purchaserUuid +
					", amount=" + amount +
					", originalLength=" + originalLength +
					", length=" + length +
					", gameType=" + gameType +
					", dateActivated=" + dateActivated +
					", stacked=" + stacked +
					", queuedToStack=" + queuedToStack +
					'}';
		}
	}
}
