package net.hypixel.api.reply;

import net.hypixel.api.util.Banner;

import java.util.List;

public class GuildReply extends AbstractReply {
	private Guild guild;

	public Guild getGuild() {
		return guild;
	}

	@Override
	public String toString() {
		return "GuildReply{" +
				"guild=" + guild +
				"} " + super.toString();
	}

	public static class Guild {
		private String _id;

		private String name;
		private String description;
		private String tag;
		private boolean publiclyListed;
		private Banner banner;
		private List<Member> members;
		private int coins;
		private int coinsEver;
		private long created;
		private boolean joinable;
		private long exp;
		private int memberSizeLevel;
		private int bankSizeLevel;
		private boolean canTag;
		private boolean canParty;
		private boolean canMotd;
		private int legacyRanking;
		private List<String> preferredGames;
		private List<Rank> ranks;

		/**
		 * @see #addRanksToMembers()
		 */
		private boolean isRankToMembersAdded = false;

		public String get_id() {
			return _id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getTag() {
			return tag;
		}

		public boolean getPubliclyListed() {
			return publiclyListed;
		}

		public Banner getBanner() {
			return banner;
		}

		public List<Member> getMembers() {
			addRanksToMembers();

			return members;
		}

		public int getCoins() {
			return coins;
		}

		public int getCoinsEver() {
			return coinsEver;
		}

		public long getCreated() {
			return created;
		}

		public boolean getJoinable() {
			return joinable;
		}

		public long getExp() {
			return exp;
		}

		public int getMemberSizeLevel() {
			return memberSizeLevel;
		}

		public int getBankSizeLevel() {
			return bankSizeLevel;
		}

		public boolean getCanTag() {
			return canTag;
		}

		public boolean getCanParty() {
			return canParty;
		}

		public boolean getCanMotd() {
			return canMotd;
		}

		public int getLegacyRanking() {
			return legacyRanking;
		}

		public List<Rank> getRanks() {
			return ranks;
		}

		public List<String> getPreferredGames() {
			return preferredGames;
		}

		@Override
		public String toString() {
			addRanksToMembers();

			return "Guild{" +
					"_id='" + _id + '\'' +
					", name='" + name + '\'' +
					", description='" + description + '\'' +
					", tag='" + tag + '\'' +
					", publiclyListed=" + publiclyListed +
					", banner=" + banner +
					", members=" + members +
					", coins=" + coins +
					", coinsEver=" + coinsEver +
					", created=" + created +
					", joinable=" + joinable +
					", exp=" + exp +
					", memberSizeLevel=" + memberSizeLevel +
					", bankSizeLevel=" + bankSizeLevel +
					", canTag=" + canTag +
					", canParty=" + canParty +
					", canMotd=" + canMotd +
					", legacyRanking=" + legacyRanking +
					'}';
		}

		private void addRanksToMembers() {
			if (!isRankToMembersAdded) {

				// GuildMaster rank is not included in {@link #ranks} list from hypixel network
				Rank guildMasterRank = new Rank();
				guildMasterRank.name = "GUILDMASTER";
				guildMasterRank.created = getCreated(); // the rank is created when the guild is created?
				guildMasterRank.priority = 10; // Some high number :P
				ranks.add(guildMasterRank);

				// associate the members to their rank

				// Ranks names are saved like "member", "Member" or "MEMBER"
				// So we need to lowercase all the ranks names, THEN compare them
				for (Rank rank : getRanks()) {
					rank.lower_name = rank.name.toLowerCase();
				}

				for (Member member : members) {
					String memberRank = member.rank.toLowerCase();

					for (Rank rank : getRanks()) {
						if (memberRank.equals(rank.lower_name)) {
							member.rankObject = rank;
						}
					}
				}

				isRankToMembersAdded = true;
			}
		}

		public class Member implements Comparable<Member> {
			public String username = null;

			private String uuid;
			private String rank;
			private long joined;
			private long questParticipation;
			private Rank rankObject;

			public String getUuid() {
				return uuid;
			}

			public Rank getRank() {
				return rankObject;
			}

			public long getJoined() {
				return joined;
			}

			public long getQuestParticipation() {
				return questParticipation;
			}

			@Override
			public int compareTo(Member o2) {
				// in rank priority
				if (this.getRank().priority > o2.getRank().priority) return -1;
				else if (this.getRank().priority < o2.getRank().priority) return 1;

				// who joined first
				return Long.compare(this.joined, o2.joined);
			}

			@Override
			public String toString() {
				return "Member{" +
						"uuid=" + uuid +
						", rank=" + rank +
						", joined=" + joined +
						", questParticipation=" + questParticipation +
						'}';
			}
		}

		public class Rank {
			private String name;
			private boolean default_;
			private String tag;
			private long created;
			private byte priority;

			private String lower_name;

			public String getName() {
				return name;
			}

			public boolean getDefault() {
				return default_;
			}

			public byte getPriority() {
				return priority;
			}

			public String getTag() {
				return tag;
			}

			public long getCreated() {
				return created;
			}

			@Override
			public String toString() {
				return "Rank{" +
						"name=" + name +
						", default=" + default_ +
						", tag=" + tag +
						", created=" + created +
						", priority" + priority +
						'}';
			}
		}
	}

}

