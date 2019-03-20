package net.hypixel.api.reply;

public class KeyReply extends AbstractReply {
	private Key record;

	public Key getRecord() {
		return record;
	}

	@Override
	public String toString() {
		return "KeyReply{" +
				"record=" + record +
				"} " + super.toString();
	}

	public class Key {
		private String key;
		private String ownerUuid;
		private int totalQueries;
		private int queriesInPastMin;

		public String getKey() {
			return key;
		}

		public String getOwnerUuid() {
			return ownerUuid;
		}

		public int getTotalQueries() {
			return totalQueries;
		}

		public int getQueriesInPastMin() {
			return queriesInPastMin;
		}

		@Override
		public String toString() {
			return "Key{" +
					"key=" + key +
					", ownerUuid=" + ownerUuid +
					", totalQueries=" + totalQueries +
					", queriesInPastMin=" + queriesInPastMin +
					'}';
		}
	}
}
