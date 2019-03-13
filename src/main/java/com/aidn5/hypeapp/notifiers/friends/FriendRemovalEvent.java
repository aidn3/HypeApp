package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import java.util.Arrays;
import java.util.List;

/**
 * Check friends list every while and save it.
 * Notify the user when someone removes the user
 * from their friends list while they being offline
 */
public final class FriendRemovalEvent extends NotifierFactory {
	private static final String SETTINGS_FRIENDS_UUID = FriendRemovalEvent.class.getSimpleName() + "_FriendsUUID";

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
		HypixelReplay hypixelReplay = new FriendsRequest(context).getFriendsByUserUUID(sp);
		return (String[]) hypixelReplay.value;
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
