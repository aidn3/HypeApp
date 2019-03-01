package com.aidn5.hypeapp.hypixelapi.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class Guild {
	public final String guildId;
	public final String guildName;
	public final String guildTag;
	public final String guildDescription;
	private final JSONObject jsonGuild;

	public Guild(JSONObject json) {
		this.jsonGuild = json;

		this.guildId = this.jsonGuild.optString("_id");
		this.guildName = this.jsonGuild.optString("name");
		this.guildTag = this.jsonGuild.optString("tag");
		this.guildDescription = this.jsonGuild.optString("description");
	}

	/**
	 * fetch the data from the JSON and create an array with all the members UUIDs
	 *
	 * @return String[] with all members UUIDs
	 */
	public String[] getUUIDMembers() {
		JSONArray jsonMembers = jsonGuild.optJSONArray("members");
		String[] members = new String[jsonMembers.length()];

		for (int i = 0; i < jsonMembers.length(); i++) {
			JSONObject jsonMember = jsonMembers.optJSONObject(i);
			members[i] = jsonMember.optString("uuid");
		}

		return members;
	}

	/**
	 * Fetch the data from the JSON string and create a list from it
	 *
	 * @return {@link List}[{@link GuildMember}] with all the members of the guild
	 */
	public List<GuildMember> getDetailedGuildMembers() {
		JSONArray jsonMembers = jsonGuild.optJSONArray("members");

		List<GuildMember> guildMembers = new ArrayList<>(jsonMembers.length());
		JSONArray ranks = jsonGuild.optJSONArray("ranks");

		for (int i = 0; i < jsonMembers.length(); i++) {
			JSONObject jsonMember = jsonMembers.optJSONObject(i);

			GuildMember guildMember = new GuildMember();
			guildMembers.add(guildMember);

			guildMember.guildName = jsonGuild.optString("name");
			guildMember.uuid = jsonMember.optString("uuid");
			guildMember.rank = jsonMember.optString("rank");
			guildMember.joinedAt = jsonMember.optInt("joined", -1);
			guildMember.rank = jsonMember.optString("rank").toLowerCase();

			JSONObject rank = getRank(guildMember.rank, ranks);
			// Guild master is not on the ranks list
			// To avoid NULL Pointer exception, We only need any JSONObject to replace the null
			// All the operations are opt_
			// so there is must always be a fallback value
			if (rank == null) rank = jsonMember;

			guildMember.tag = rank.optString("tag", "");
			guildMember.tagColor = jsonGuild.optString("tagColor");
			guildMember.rankPriority = rank.optInt("priority", 100);
		}

		return guildMembers;
	}

	/**
	 * Get rank Object by name
	 * <p>
	 * e.g. {"name":"Officer","default":false,"tag":null,"created":1542231130320,"priority":2}
	 *
	 * @param rankName rank's name
	 * @param ranks    an Array with all the ranks
	 * @return JSONObject with all the meta-data of the rank
	 */
	private JSONObject getRank(String rankName, JSONArray ranks) {
		for (int i = 0; i < ranks.length(); i++) {
			JSONObject jsonRank = ranks.optJSONObject(i);
			if (rankName.equals(jsonRank.optString("name").toLowerCase())) {
				return jsonRank;
			}
		}
		return null;
	}
}
