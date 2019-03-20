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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Service provides a lookup-er to usernames and UUIDs for minecraft.
 * <p>
 * It uses http://api.mojang.com to look for the usernames and UUIDs up
 * and cache them by {@link SQLiteDatabase} for later uses
 */
public final class IgnProvider extends AbstractedCacher {
	public IgnProvider(Context context) {
		super(context);
	}

	@Nullable
	public String getUsername(@NonNull String uuid, boolean ignoreCache) {
		String username;

		if (!ignoreCache) {
			username = getByKeyFromDB(uuid).getValue();
			if (username != null) return username;
		}

		username = getUsernameFromNet(uuid);
		if (username != null) insertIntoDB(uuid, username); //Save the new one from the internet

		return username;
	}

	@Nullable
	public String getUUID(@NonNull String username, boolean ignoreCache) {
		String uuid;

		if (!ignoreCache) {
			uuid = getByValueFromDB(username).getKey();
			if (uuid != null) return uuid;
		}

		uuid = getUUIDFromNet(username);
		if (uuid != null) insertIntoDB(uuid, username); //Save the new one from the internet

		return uuid;
	}

	@Nullable
	private String getUUIDFromNet(@NonNull String username) {
		try {
			String uuid = getString("https://api.mojang.com/users/profiles/minecraft/" + username);
			JSONObject jsonObject = new JSONObject(uuid);
			return jsonObject.getString("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Nullable
	private String getUsernameFromNet(@NonNull String UUID) {
		try {
			String dataJson = getString("https://api.mojang.com/user/profiles/" + UUID + "/names");

			JSONArray playerProfiles = new JSONArray(dataJson);
			JSONObject playerProfile = playerProfiles.getJSONObject(playerProfiles.length() - 1);
			return playerProfile.getString("name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Open stream to the associated url, fetch the data and return it as {@link String}
	 *
	 * @param url the requested url
	 * @return the fetched data from the url
	 * @throws IOException if an I/O exception occurs.
	 */
	@NonNull
	private String getString(@NonNull String url) throws IOException {

		URL URL = new URL(url);

		URLConnection urlConnection = URL.openConnection();
		InputStream inputStream = urlConnection.getInputStream();

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}

		inputStream.close();
		return result.toString("UTF-8");
	}
}
