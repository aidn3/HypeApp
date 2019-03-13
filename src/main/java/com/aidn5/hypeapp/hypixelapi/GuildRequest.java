/*
 * MIT License
 *
 * Copyright (c) [2019] [aidn5]
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
import com.aidn5.hypeapp.hypixelapi.models.Guild;
import com.aidn5.hypeapp.services.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * provides a direct method to get the data from the Hypixel API
 * <p>
 * since the original source code uses high level methods
 * this alternative class made to take its place
 *
 * @see FriendsRequest
 */
public final class GuildRequest extends AbstractedRequest {
	private static final String REQUEST_GET_GUILD = HYPIXEL_URL + "guild?";
	private static final String REQUEST_FIND_GUILD = HYPIXEL_URL + "findGuild?";

	private static final String CACHE_GET_GUILD = "getGuild";
	private static final String CACHE_FIND_GUILD = "findGuild";

	public GuildRequest(Context context) {
		super(context);
	}

	/**
	 * Get {@link Guild} of the user
	 *
	 * @see #getGuildMembersByMemberUUID(String, String)
	 */
	@NonNull
	public HypixelReplay getGuildMembersByMemberUUID(@NonNull SharedPreferences preferences) {
		String api = preferences.getString(Settings.hypixelAPI.name(), null);
		String userUUID = preferences.getString(Settings.userUUID.name(), null);

		return getGuildMembersByMemberUUID(api, userUUID);
	}

	/**
	 * Get {@link Guild} of the user
	 *
	 * @param api            the key to authorize with the server
	 * @param guidMemberUUID a Member's UUID of the Guild
	 * @return {@link HypixelReplay} with the {@link HypixelReplay#value} of {@link Guild} if in guild
	 * otherwise {@link HypixelReplay#value} is NULL
	 */
	@NonNull
	public HypixelReplay getGuildMembersByMemberUUID(String api, String guidMemberUUID) {
		initForNewRequest();

		if (!isValidUUID(api))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoHypixelApi), null);
		if (!isValidUUID(guidMemberUUID))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoUserUUID), null);

		String json;
		try {
			HypixelReplay guildID = findGuildIdByMemberUuid(api, guidMemberUUID);
			if (!guildID.isSuccess) return guildID;

			json = getDataFromNet(REQUEST_GET_GUILD + "key=" + api + "&id=" + guildID.value);
		} catch (IOException e) {
			DataHolder cacher = getCache(CACHE_GET_GUILD);
			if (cacher == null)
				return new HypixelReplay(new HypixelApiException(ExceptionTypes.Internet, e), null);

			dataFromCache = cacher.dataOld;
			json = cacher.data;
		}

		try {
			JSONObject jsonObject = new JSONObject(json);

			HypixelReplay error = checkForErrorsInResponse(jsonObject, json);
			if (error != null) return error;

			saveCacheIfNeeded(CACHE_GET_GUILD, json);

			JSONObject guildJSON = jsonObject.optJSONObject("guild");

			if (guildJSON == null)
				return new HypixelReplay(null, json, dataFromCache); // no guild found
			return new HypixelReplay(new Guild(guildJSON), json, dataFromCache);

		} catch (JSONException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Parse, e), json);
		}
	}

	/**
	 * get Guild's id from Hypixel Network
	 *
	 * @param api        the key to authorize with the server
	 * @param memberUuid a Member's UUID of the Guild
	 * @return HypixelReplay with the {@link HypixelReplay#value} of {@link String}
	 */
	@NonNull
	private HypixelReplay findGuildIdByMemberUuid(@NonNull String api, @NonNull String memberUuid) {
		String json;

		try {
			json = getDataFromNet(REQUEST_FIND_GUILD + "key=" + api + "&byUuid=" + memberUuid);
		} catch (IOException e) {
			DataHolder cacher = getCache(CACHE_FIND_GUILD);
			if (cacher == null)
				return new HypixelReplay(new HypixelApiException(ExceptionTypes.Internet, e), null);

			dataFromCache = cacher.dataOld;
			json = cacher.data;
		}
		try {
			JSONObject jsonObject = new JSONObject(json);

			HypixelReplay error = checkForErrorsInResponse(jsonObject, json);
			if (error != null) return error;

			saveCacheIfNeeded(CACHE_FIND_GUILD, json);
			return new HypixelReplay(jsonObject.getString("guild"), json, -1);
		} catch (JSONException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Parse, e), json);
		}
	}
}