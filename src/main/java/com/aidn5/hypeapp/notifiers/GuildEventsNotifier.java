package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.GuildRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.hypixelapi.models.Guild;
import com.aidn5.hypeapp.notifiers.friends.FriendRemovalEvent;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.Arrays;
import java.util.List;

public final class GuildEventsNotifier extends NotifierFactory {
	private static final String SETTINGS_IS_IN_GUILD = FriendRemovalEvent.class.getSimpleName() + "_IsInGuild";
	private static final String SETTINGS_GUILD_MEMBERS = FriendRemovalEvent.class.getSimpleName() + "_GuildMembers";

	public GuildEventsNotifier(@NonNull Context context, @NonNull DB db, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		super(context, db, ignProvider, settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLoop() {
		boolean guildMemberJoins = settings.getBoolean(Settings.showNotificationOnGuildMemberJoins.name(), false);
		boolean guildMemberLeaves = settings.getBoolean(Settings.showNotificationOnGuildMemberLeaves.name(), false);
		boolean guildSelfLeaves = settings.getBoolean(Settings.showNotificationOnGuildSelfLeaves.name(), false);

		if (!guildMemberJoins && !guildMemberLeaves && !guildSelfLeaves)
			return;//Nothing checked? just return.

		boolean isTheUserOnline = isTheUserOnline();// User online? no need to notify

		HypixelReplay hypixelReplay = new GuildRequest(context).getGuildMembersByMemberUUID(settings);
		if (!hypixelReplay.isSuccess || hypixelReplay.isDataFromCache())
			return;//No updated data. -> nothing to do/update -> return;

		Guild guild = (Guild) hypixelReplay.value;
		boolean isInGuild = (guild != null);

		isUserKickedEvent(isInGuild, guildSelfLeaves, isTheUserOnline);

		if (!isInGuild) {
			//Let's remove all the members from the list to not conflict them with another guild
			cacheGuildMembers(new String[0]);
			return;
		}

		String[] cachedMembers = getCachedGuildMembers();
		String[] members = guild.getUUIDMembers();

		if (cachedMembers != null)
			isMemberLeftJoinedEvent(members, cachedMembers, guildMemberJoins, guildMemberLeaves, isTheUserOnline);

		cacheGuildMembers(members);
	}

	/**
	 * Check whether the user has been kicked from the guild or not.
	 * If it they have been kicked and enabled guildSelfLeaves option
	 * from the settings and they are not online at the moment, then notify them
	 *
	 * @param inGuild         whether it's in guild or not
	 * @param guildSelfLeaves is showing notifications granted?
	 * @param isTheUserOnline is the user of this app is on hypixel network?
	 */
	private void isUserKickedEvent(boolean inGuild, boolean guildSelfLeaves, boolean isTheUserOnline) {
		try {
			if (!isTheUserOnline && guildSelfLeaves) {
				boolean isInGuildCache = db.getBoolean(SETTINGS_IS_IN_GUILD);
				if (isInGuildCache != inGuild) {
					notificationFactory.notify(
							context.getString(R.string.guildEventKickedTitle),
							context.getString(R.string.guildEventKickedMessage)
					);
				}
			}
		} catch (SnappydbException ignored) {
		} finally {
			try {//Set whether is the user inGuild
				db.put(SETTINGS_IS_IN_GUILD, inGuild);
			} catch (SnappydbException ignored) {
			}
		}
	}

	/**
	 * Check whether a member has joined or left the guild.
	 * <p>
	 * First check whether it should check
	 * by looking at the notifications permissions (guildMemberJoins and guildMemberLeaves)
	 * and by looking at the isTheUserOnline.
	 * Then start checking based on what enabled (guildMemberJoins and guildMemberLeaves)
	 * <p>
	 * if the user is online (isTheUserOnline),
	 * then there is no need to check anyways so the whole method will be ignored
	 *
	 * @param members           the current (real) members of the guild
	 * @param cachedMembers     the saved guild's members
	 * @param guildMemberJoins  is showing notifications granted for joining the guild event?
	 * @param guildMemberLeaves is showing notifications granted for leaving the guild event?
	 * @param isTheUserOnline   is the user of this app is on hypixel network?
	 */
	private void isMemberLeftJoinedEvent(@NonNull String[] members, @NonNull String[] cachedMembers, boolean guildMemberJoins, boolean guildMemberLeaves, boolean isTheUserOnline) {
		if ((!guildMemberJoins && !guildMemberLeaves) || isTheUserOnline)
			return; //No need to create objects, compare then show notifications

		if (guildMemberJoins) {
			List<String> cachedMembersList = Arrays.asList(cachedMembers);

			for (String member : members) {
				if (!cachedMembersList.contains(member)) {

					String username = ignProvider.getUsername(member, false);
					if (username == null) username = member;

					notificationFactory.notify(
							context.getString(R.string.guildEventMemberJoinsTitle),
							context.getString(R.string.guildEventMemberJoinsMessage, username)
					);
				}
			}
		}

		if (guildMemberLeaves) {
			List<String> membersList = Arrays.asList(members);

			for (String member : cachedMembers) {
				if (!membersList.contains(member)) {

					String username = ignProvider.getUsername(member, false);
					if (username == null) username = member;

					notificationFactory.notify(
							context.getString(R.string.guildEventMemberLeavesTitle),
							context.getString(R.string.guildEventMemberLeavesMessage, username)
					);
				}
			}
		}
	}

	/**
	 * Save the guild's members into the cache by their UUIDs
	 *
	 * @param UUIDs guild's members UUIDs
	 */
	private void cacheGuildMembers(@NonNull String[] UUIDs) {
		try {
			db.put(SETTINGS_GUILD_MEMBERS, UUIDs);
		} catch (SnappydbException ignored) {
		}
	}

	/**
	 * Retrieve the guild's members from the cache
	 *
	 * @return guild's members associated by their UUIDs
	 */
	@Nullable
	private String[] getCachedGuildMembers() {
		try {
			return db.getObjectArray(SETTINGS_GUILD_MEMBERS, String.class);
		} catch (SnappydbException ignored) {
			return null;
		}
	}
}
