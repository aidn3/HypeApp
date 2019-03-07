package com.aidn5.hypeapp.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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
@SuppressWarnings("unused")
public final class IgnProvider {
	private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS 'data' ('uuid' TEXT PRIMARY KEY, 'hypixelAPI' TEXT, modifyTime INTEGER);";
	private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS data;";
	private static final String QUERY_PUT = "INSERT OR REPLACE INTO 'data' (uuid, hypixelAPI, modifyTime) VALUES (?, ?, ?);";

	private static final int TimeController = 7 * 24 * 60 * 60 * 1000; //Discard usernames older than 7 days

	private final SQLiteStatement PREPARED_PUT;
	private final SQLiteDatabase db;

	public IgnProvider(Context context) {
		this.db = SQLiteDatabase.openDatabase(
				context.getDatabasePath(this.getClass().getSimpleName()).getAbsolutePath(),
				null,
				SQLiteDatabase.CREATE_IF_NECESSARY);

		this.db.execSQL(QUERY_CREATE_TABLE);

		this.PREPARED_PUT = this.db.compileStatement(QUERY_PUT);
	}

	@Nullable
	public String getUsername(@NonNull String uuid, boolean ignoreCache) {
		String username;

		if (!ignoreCache) {
			username = getUsernameFromDB(uuid);
			if (username != null) return username;
		}

		username = getUsernameFromNet(uuid);
		if (username != null) putUserIntoDB(uuid, username); //Save the new one from the internet

		return username;
	}

	@Nullable
	public String getUUID(@NonNull String username, boolean ignoreCache) {
		String uuid;

		if (!ignoreCache) {
			uuid = getUUIDFromDB(username);
			if (uuid != null) return uuid;
		}

		uuid = getUUIDFromNet(username);
		if (uuid != null) putUserIntoDB(uuid, username); //Save the new one from the internet

		return uuid;
	}

	@Nullable
	private String getUUIDFromNet(@NonNull String username) {
		try {
			String uuid = getString("https://api.mojang.com/users/profiles/minecraft/" + username);
			JSONObject jsonObject = new JSONObject(uuid);
			return jsonObject.getString("id");
		} catch (Exception ignored) {
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
		} catch (Exception ignored) {
		}
		return null;
	}

	@Nullable
	private synchronized String getUsernameFromDB(@NonNull String uuid) {
		Cursor cursor = this.db.rawQuery(
				"SELECT hypixelAPI from data where uuid = ? AND modifyTime > ?;",
				new String[]{uuid, (System.currentTimeMillis() - TimeController) + ""}
		);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		String username = cursor.getString(cursor.getColumnIndex("hypixelAPI"));

		cursor.close();
		return username;
	}

	@Nullable
	private synchronized String getUUIDFromDB(@NonNull String username) {
		Cursor cursor = this.db.rawQuery(
				"SELECT uuid from data where hypixelAPI = ? AND modifyTime > ?;",
				new String[]{username, (System.currentTimeMillis() - TimeController) + ""}
		);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		String uuid = cursor.getString(cursor.getColumnIndex("uuid"));

		cursor.close();
		return uuid;
	}

	@SuppressWarnings("UnusedReturnValue")
	private synchronized long putUserIntoDB(@NonNull String uuid, @NonNull String username) {
		PREPARED_PUT.clearBindings();

		PREPARED_PUT.bindString(1, uuid);
		PREPARED_PUT.bindString(2, username);
		PREPARED_PUT.bindLong(3, System.currentTimeMillis());
		return PREPARED_PUT.executeInsert();
	}

	/**
	 * clean the database from old outdated data
	 */
	public synchronized void cleanDB() {
		this.db.execSQL("DELETE FROM data WHERE modifyTime < " + (System.currentTimeMillis() - TimeController));
	}

	/**
	 * delete all the cached data
	 */
	public synchronized void clearDB() {
		this.db.beginTransaction();

		this.db.execSQL(QUERY_DROP_TABLE);
		this.db.execSQL(QUERY_CREATE_TABLE);

		this.db.setTransactionSuccessful();
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

	public class DataHolder {
		public String username;
		public String uuid;
		public boolean isUpToDate;
	}
}
