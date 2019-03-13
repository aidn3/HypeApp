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

/**
 * provides a direct method to get the data from the Hypixel API
 * <p>
 * since the original source code uses high level methods
 * this alternative class made to take its place
 *
 * @see GuildRequest
 */
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
