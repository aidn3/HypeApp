package com.aidn5.hypeapp.notifiers;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.ServicesProvider;
import com.aidn5.hypeapp.services.Settings;
import com.snappydb.SnappydbException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.aidn5.hypeapp.services.Settings.hypixelAPI;
import static com.aidn5.hypeapp.services.Settings.userUUID;

//todo: give a proper doc
public final class FriendsEventsNotifier extends NotifierFactory {
	private static final String friendsUrl = "https://api.hypixel.net/friends?";

	public FriendsEventsNotifier(ServicesProvider servicesProvider) {
		super(servicesProvider);
	}

	@Override
	public final void doLoop() {
		checkFriendRemoval();
	}

	//TODO: test this
	private void checkFriendRemoval() {
		String[] friends = getFriendsFromNet();
		if (friends == null) return; //Cant load the list -> nothing to update

		String[] cachedFriends = null;
		if (settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) {
			cachedFriends = getFriendsFromCache();//Load the cache before overwriting
		}

		cacheFriends(friends);


		if (!settings.getBoolean(Settings.showNotificationOnFriendRemoved.name(), false)) return;

		if (cachedFriends == null) return;//The first time it runs will have no cache

		List<String> friendsFromNet = Arrays.asList(friends);//Convert to list. so it supports #contains

		for (String cachedFriend : cachedFriends) {
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

	private String[] getFriendsFromNet() {
		if (settings.getString(hypixelAPI.name(), null) == null || hypixelAPI.name().isEmpty())
			return null;
		if (userUUID.name() == null || userUUID.name().isEmpty()) return null;

		try {
			String json = netRequest(friendsUrl + "key=" + hypixelAPI.name() + "&uuid=" + userUUID.name());

			JSONObject callbackJson = new JSONObject(json);
			JSONArray friendsJson = callbackJson.getJSONArray("records");

			String[] currentFriends = new String[friendsJson.length()];
			for (int i = 0; i < friendsJson.length(); i++) {
				JSONObject friend = friendsJson.getJSONObject(i);
				if (friend.getString("uuidSender").equals(userUUID.name())) {//This is the user's uuid. NOT a friend...
					currentFriends[i] = friend.getString("uuidReceiver");
				} else {
					currentFriends[i] = friend.getString("uuidSender");
				}
			}

			return currentFriends;
		} catch (IOException | JSONException ignored) {
		}

		return null;
	}

	private void cacheFriends(String[] friends) {
		if (db == null) return;

		synchronized (db) {
			try {
				db.put("friends", friends);
			} catch (SnappydbException e) {
				e.printStackTrace();
			}
		}
	}

	private String[] getFriendsFromCache() {
		if (db == null) return null;

		synchronized (db) {

			try {
				return db.getObjectArray("friends", String.class);
			} catch (SnappydbException e) {
				e.printStackTrace();
			}

			return null;
		}
	}

}
