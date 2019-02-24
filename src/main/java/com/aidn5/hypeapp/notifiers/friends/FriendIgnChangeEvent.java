package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;
import com.snappydb.DB;

public final class FriendIgnChangeEvent extends NotifierFactory {
	public FriendIgnChangeEvent(Context context, DB db, IgnProvider ignProvider, SharedPreferences settings) {
		super(context, db, ignProvider, settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLoop() {
		String[] playersUUID = getPlayersUUID();
		if (playersUUID == null) return; //No-data -> nothing to do -> return

		boolean showNotification = settings.getBoolean(Settings.showNotificationOnFriendIgnChanged.name(), true);

		// We don't need to look up the usernames anymore,
		// since we won't notify the user anyways
		if (!showNotification) return;

		for (String uuid : playersUUID) {
			ignProvider.cleanDB(); // clean the database to make the look-up faster

			//looked up usernames will automatically be saved (cached)
			//todo: [Performance] make username look up more efficient by retrieving whether it's the look-up is from cache or not
			String user1 = ignProvider.getUsername(uuid, false);
			if (user1 == null) continue;

			String user2 = ignProvider.getUsername(uuid, true);
			if (user2 == null) continue;

			if (!user1.equals(user2)) {
				notificationFactory.notify(
						context.getString(R.string.friendsEventIgnChangedTitle),
						context.getString(R.string.friendsEventIgnChangedMessage, user1, user2)
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
	private String[] getPlayersUUID() {
		//TODO: [Feature] FriendIgnChangeEvent#getPlayersUUID: add include exclude to settings
		HypixelReplay hypixelReplay = new FriendsRequest(context).getFriendsByUserUUID(settings);
		return (String[]) hypixelReplay.value; // Either null or String[]
	}
}
