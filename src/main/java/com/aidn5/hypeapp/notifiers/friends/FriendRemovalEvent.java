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


package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import net.hypixel.api.HypixelAPI;

import java.util.Arrays;
import java.util.List;

/**
 * Check friends list every while and save it.
 * Notify the user when someone removes the user
 * from their friends list while they being offline
 */
public final class FriendRemovalEvent extends NotifierFactory {
	public FriendRemovalEvent(@NonNull Context context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		String[] friends = getFriendsUUIDsFromNet(settings);
		if (friends == null) return; //Cant load the list -> nothing to update -> return

		String[] cachedFriends = null;
		if (settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) {
			cachedFriends = getFriendsUUIDsFromCache(dm);//Load the cache before overwriting
		}

		cacheFriendsUUIDs(dm, friends);

		if (!settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) return;

		if (isTheUserOnline())
			return; //The user will notice it -> no need to compare/show notifications
		if (cachedFriends == null) return;//The first time it runs will have no cache -> return

		compareFriendsListsAndSendNotification(eventsSaver, ignProvider, friends, cachedFriends);
	}

	@Override
	public int getName() {
		return R.string.showNotificationOnFriendRemoved_title;
	}

	/**
	 * Compare the two lists, find the missing object from the upToDateList and send notifications
	 * <p>
	 * After running {@link #doLoop(DataManager, EventsSaver, IgnProvider, SharedPreferences)}
	 * and granting the permission to send notifications
	 * {@link #doLoop(DataManager, EventsSaver, IgnProvider, SharedPreferences)} calls at the end
	 * to compare the lists and show changes as notifications.
	 * <p>
	 * This method has been created to divide {@link #doLoop(DataManager, EventsSaver, IgnProvider, SharedPreferences)} to ease the readability
	 *
	 * @param upToDateList the retrieved list from the server
	 * @param cachedList   the saved list
	 */
	private void compareFriendsListsAndSendNotification(@NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull String[] upToDateList, @NonNull String[] cachedList) {
		List<String> friendsFromNet = Arrays.asList(upToDateList);//Convert to list. so it supports #contains

		for (String cachedFriend : cachedList) {
			if (!friendsFromNet.contains(cachedFriend)) {
				String username = ignProvider.getUsername(cachedFriend, false);
				if (username == null || username.isEmpty()) username = cachedFriend;

				notificationFactory.notify(
						context.getString(R.string.friendsEventRemovedTitle),
						context.getString(R.string.friendsEventRemovedMessage, username)
				);

				EventsSaver.DataHolder dataHolder = new EventsSaver.DataHolder();

				dataHolder.provider = getName();
				dataHolder.title = R.string.friendsEventRemovedTitle;
				dataHolder.message = R.string.friendsEventRemovedMessage;
				dataHolder.args = new String[]{username};

				eventsSaver.register(dataHolder);
			}
		}
	}

	/**
	 * Retrieve the Up-To-Date friend list from the server
	 *
	 * @return Up-To-Date friends list associated by their UUIDs
	 */
	@Nullable
	private String[] getFriendsUUIDsFromNet(SharedPreferences sp) {
		try {
			return new HypixelAPI(context, sp)
					.getFriends(sp)
					.getFriends(
							sp.getString(Settings.userUUID.name(), null))
					.toArray(new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Save the retrieved friends list from the server
	 *
	 * @param friends The friend list, which is retrieved from the server
	 */
	private void cacheFriendsUUIDs(@NonNull DataManager dm, @NonNull String[] friends) {
		dm.setFriends(friends);
	}

	/**
	 * Retrieve the list of the friends from the cache
	 * <p>
	 * To test them against the real/updated list from the server
	 *
	 * @return Friends list associated by their UUID
	 */
	@Nullable
	private String[] getFriendsUUIDsFromCache(DataManager dm) {
		return dm.getFriends();
	}
}
