package com.aidn5.hypeapp.hypixelapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes;
import com.aidn5.hypeapp.hypixelapi.exception.HypixelApiException;
import com.aidn5.hypeapp.services.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class FriendsRequest extends AbstractedRequest {
	private static final String REQUEST_FRIENDS = HYPIXEL_URL + "friends?";
	private static final String CACHE_FRIENDS = "friends";

	public FriendsRequest(@NonNull Context context) {
		super(context);
	}

	@NonNull
	public HypixelReplay getFriendsByUserUUID(@NonNull SharedPreferences preferences) {
		String api = preferences.getString(Settings.hypixelAPI.name(), null);
		String userUUID = preferences.getString(Settings.userUUID.name(), null);

		return getFriendsByUserUUID(api, userUUID);
	}

	@NonNull
	public HypixelReplay getFriendsByUserUUID(String api, String userUUID) {
		initForNewRequest();

		if (!isValidUUID(api))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoHypixelApi), null);
		if (!isValidUUID(userUUID))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoUserUUID), null);


		String json;
		try {
			json = getDataFromNet(REQUEST_FRIENDS + "key=" + api + "&uuid=" + userUUID);
		} catch (IOException e) {
			DataHolder cacher = getCache(CACHE_FRIENDS);
			if (cacher == null)
				return new HypixelReplay(new HypixelApiException(ExceptionTypes.Internet, e), null);

			dataFromCache = cacher.dataOld;
			json = cacher.data;
		}

		try {
			JSONObject jsonObject = new JSONObject(json);

			HypixelReplay error = checkForErrorsInResponse(jsonObject, json);
			if (error != null) return error;

			saveCacheIfNeeded(CACHE_FRIENDS, json);

			JSONArray friendsJson = jsonObject.getJSONArray("records");

			String[] currentFriends = new String[friendsJson.length()];
			for (int i = 0; i < friendsJson.length(); i++) {
				JSONObject friend = friendsJson.getJSONObject(i);
				if (friend.getString("uuidSender").equals(userUUID)) {//This is the user's uuid. NOT a friend...
					currentFriends[i] = friend.getString("uuidReceiver");
				} else {
					currentFriends[i] = friend.getString("uuidSender");
				}
			}

			return new HypixelReplay(currentFriends, json, dataFromCache);
		} catch (JSONException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Parse, e), json);
		}
	}
}
