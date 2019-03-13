/*
 * MIT License
 *
 * Copyright (c) 2019 aidn5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


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
	private final String dbPath;
	private DB db;

	public DataManager(@NonNull Context context) {
		this.dbPath = context.getFilesDir().getAbsolutePath();
		openDB();
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

	public synchronized void remove(String key) {
		try {
			this.db.del(key);
		} catch (SnappydbException ignored) {
		}
	}

	public synchronized boolean removeAll() {
		try {
			this.db.destroy();
			openDB();
		} catch (SnappydbException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	private void openDB() {
		try {
			db = DBFactory.open(dbPath);
		} catch (SnappydbException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleException(e);
			db = null;
		}
	}
}
