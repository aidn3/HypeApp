package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.Arrays;
import java.util.List;

/**
 * Check friends list every while and save it.
 * Notify the user when someone removes the user
 * from their friends list while they being offline
 */
public final class FriendRemovalEvent extends NotifierFactory {
	private static final String SETTINGS_FRIENDS_UUID = FriendRemovalEvent.class.getSimpleName() + "_FriendsUUID";

	public FriendRemovalEvent(Context context, DB db, IgnProvider ignProvider, SharedPreferences settings) {
		super(context, db, ignProvider, settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void doLoop() {
		String[] friends = getFriendsUUIDsFromNet();
		if (friends == null) return; //Cant load the list -> nothing to update -> return

		String[] cachedFriends = null;
		if (settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) {
			cachedFriends = getFriendsUUIDsFromCache();//Load the cache before overwriting
		}

		cacheFriendsUUIDs(friends);

		if (!settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) return;

		if (isTheUserOnline())
			return; //The user will notice it -> no need to compare/show notifications
		if (cachedFriends == null) return;//The first time it runs will have no cache -> return

		compareFriendsListsAndSendNotification(friends, cachedFriends);
	}

	/**
	 * Compare the two lists, find the missing object from the upToDateList and send notifications
	 * <p>
	 * After running {@link #doLoop()}
	 * and granting the permission to send notifications
	 * {@link #doLoop()} calls at the end
	 * to compare the lists and show changes as notifications.
	 * <p>
	 * This method has been created to divide {@link #doLoop()} to ease the readability
	 *
	 * @param upToDateList the retrieved list from the server
	 * @param cachedList   the saved list
	 */
	private void compareFriendsListsAndSendNotification(@NonNull String[] upToDateList, @NonNull String[] cachedList) {
		List<String> friendsFromNet = Arrays.asList(upToDateList);//Convert to list. so it supports #contains

		for (String cachedFriend : cachedList) {
			if (!friendsFromNet.contains(cachedFriend)) {
				String username = ignProvider.getUsername(cachedFriend, false);
				if (username == null || username.isEmpty()) username = cachedFriend;

				notificationFactory.notify(
						context.getString(R.string.friendsEventRemovedTitle),
						context.getString(R.string.friendsEventRemovedMessage, username)
				);
			}
		}
	}

	/**
	 * Retrieve the Up-To-Date friend list from the server
	 *
	 * @return Up-To-Date friends list associated by their UUIDs
	 */
	@Nullable
	private String[] getFriendsUUIDsFromNet() {
		HypixelReplay hypixelReplay = new FriendsRequest(context).getFriendsByUserUUID(settings);
		return (String[]) hypixelReplay.value;
	}

	/**
	 * Save the retrieved friends list from the server
	 *
	 * @param friends The friend list, which is retrieved from the server
	 */
	private void cacheFriendsUUIDs(@NonNull String[] friends) {
		synchronized (db) {
			try {
				db.put(SETTINGS_FRIENDS_UUID, friends);
			} catch (SnappydbException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Retrieve the list of the friends from the cache
	 * <p>
	 * To test them against the real/updated list from the server
	 *
	 * @return Friends list associated by their UUID
	 */
	@Nullable
	private String[] getFriendsUUIDsFromCache() {
		synchronized (db) {

			try {
				return db.getObjectArray(SETTINGS_FRIENDS_UUID, String.class);
			} catch (SnappydbException e) {
				e.printStackTrace();
			}

			return null;
		}
	}
}
