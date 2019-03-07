package com.aidn5.hypeapp.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.acra.ACRA;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Data Provider provides data with ease access.
 * <p>
 * Instead of accessing the data directly with hardcoded-names
 * from activities (frontend/gui) or services (background process),
 * this class gives methods to use and access these data.
 */
public final class DataManager {
	private static final String BEST_FRIENDS_UUID = "bestFriendsUuid";
	private static final String FRIENDS_UUID = "friendsUuid";
	private static final String GUILD_MEMBERS_UUID = "guildMembersUuid";
	private static final String IS_IN_GUILD = "isInGuild";

	private final DB db;

	public DataManager(@NonNull Context context) {
		DB db;
		try {
			db = DBFactory.open(context);
		} catch (SnappydbException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleException(e);
			db = null;
		}

		this.db = db;
	}

	public synchronized <T extends Serializable> T get(@NonNull String key, @NonNull Class<T> className) throws SnappydbException {
		return db.get(key, className);
	}

	public synchronized boolean put(String key, Serializable serializable) {
		try {
			db.put(key, serializable);
			return true;
		} catch (SnappydbException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleException(e);
			return false;
		}
	}

	public synchronized boolean exists(String key) throws SnappydbException {
		return this.db.exists(key);
	}

	/**
	 * @return Array with the data or null if not found
	 */
	public String[] getBestFriends() {
		try {
			return this.db.getObjectArray(BEST_FRIENDS_UUID, String.class);
		} catch (SnappydbException e) {
			return new String[0];
		}
	}

	public boolean setBestFriends(@NonNull String[] bestFriends) {
		return put(BEST_FRIENDS_UUID, bestFriends);
	}

	/**
	 * @return Array with the data or null if not found
	 */
	@Nullable
	public String[] getFriends() {
		try {
			return this.db.getObjectArray(FRIENDS_UUID, String.class);
		} catch (SnappydbException e) {
			return null;
		}
	}

	public boolean setFriends(@NonNull String[] bestFriends) {
		return put(FRIENDS_UUID, bestFriends);
	}

	/**
	 * @return Array with the data or null if not found
	 */
	@Nullable
	public String[] getGuildMembers() {
		try {
			return this.db.getObjectArray(GUILD_MEMBERS_UUID, String.class);
		} catch (SnappydbException e) {
			return null;
		}
	}

	public boolean setGuildMembers(@NonNull String[] bestFriends) {
		return put(GUILD_MEMBERS_UUID, bestFriends);
	}

	/**
	 * Indicates whether the user has/is in guild on hypixel network
	 */
	public boolean setIsInGuild(boolean isInGuild) {
		return put(IS_IN_GUILD, isInGuild);
	}

	/**
	 * Indicates whether the user has/is in guild on hypixel network
	 *
	 * @return false by default if not found
	 * @throws SnappydbException if not found/or something went wrong
	 */
	public boolean getIsInGuild() throws SnappydbException, NoSuchElementException {
		if (!exists(IS_IN_GUILD)) throw new NoSuchElementException();

		return get(IS_IN_GUILD, boolean.class);
	}
}
