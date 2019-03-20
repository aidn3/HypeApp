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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.BuildConfig;

public abstract class AbstractedCacher {
	private static final String TABLE_NAME = "data_" + BuildConfig.VERSION_NAME.replace(".", "_");
	private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "' ('key' TEXT PRIMARY KEY, 'value' TEXT, 'modifyTime' INTEGER);";
	private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
	private static final String QUERY_PUT = "INSERT OR REPLACE INTO '" + TABLE_NAME + "' ('key', 'value', 'modifyTime') VALUES (?, ?, ?);";

	protected final long cacheController;
	private final SQLiteStatement PREPARED_PUT;
	private final SQLiteDatabase db;

	protected AbstractedCacher(@NonNull Context context) {
		this(context, 7 * 24 * 60 * 60 * 1000);// 7 days
	}

	/**
	 * @param cacheController Discard data older than n milliseconds
	 */
	protected AbstractedCacher(@NonNull Context context, long cacheController) {

		this.cacheController = cacheController;

		this.db = SQLiteDatabase.openDatabase(
				context.getDatabasePath(this.getClass().getSimpleName()).getAbsolutePath(),
				null,
				SQLiteDatabase.CREATE_IF_NECESSARY);

		this.db.execSQL(QUERY_CREATE_TABLE);

		this.PREPARED_PUT = this.db.compileStatement(QUERY_PUT);
	}

	@NonNull
	protected final synchronized CachedData getByKeyFromDB(@NonNull String key) {
		Cursor cursor = this.db.rawQuery(
				"SELECT * from " + TABLE_NAME + " where key = ? AND modifyTime > ?;",
				new String[]{key, (System.currentTimeMillis() - cacheController) + ""}
		);

		return fetchData(cursor);
	}

	@NonNull
	protected final synchronized CachedData getByValueFromDB(@NonNull String value) {
		Cursor cursor = this.db.rawQuery(
				"SELECT * from " + TABLE_NAME + " where value = ? AND modifyTime > ?;",
				new String[]{value, (System.currentTimeMillis() - cacheController) + ""}
		);

		return fetchData(cursor);
	}

	@SuppressWarnings("UnusedReturnValue")
	protected final synchronized long insertIntoDB(@NonNull String key, @NonNull String value) {
		PREPARED_PUT.clearBindings();

		PREPARED_PUT.bindString(1, key);
		PREPARED_PUT.bindString(2, value);
		PREPARED_PUT.bindLong(3, System.currentTimeMillis());
		return PREPARED_PUT.executeInsert();
	}

	/**
	 * clean the database from old outdated data
	 */
	public final synchronized void cleanDB() {
		this.db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE modifyTime < " + (System.currentTimeMillis() - cacheController));
	}

	/**
	 * delete all the cached data
	 */
	public final synchronized void clearDB() {
		this.db.beginTransaction();

		this.db.execSQL(QUERY_DROP_TABLE);
		this.db.execSQL(QUERY_CREATE_TABLE);

		this.db.setTransactionSuccessful();
		this.db.endTransaction();
	}

	@NonNull
	private CachedData fetchData(@NonNull Cursor cursor) {
		if (!cursor.moveToFirst()) {
			cursor.close();
			return new CachedData(null, null, -1);
		}

		CachedData cachedData = new CachedData(
				cursor.getString(0),
				cursor.getString(1),
				cursor.getInt(2));

		cursor.close();
		return cachedData;
	}

	public static final class CachedData {
		private final String key;
		private final String value;
		private final long time;

		private CachedData(String key, String value, long time) {
			this.key = key;
			this.value = value;
			this.time = time;
		}

		public long getTime() {
			return time;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}

}
