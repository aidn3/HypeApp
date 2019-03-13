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

import org.acra.ACRA;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Events logger. saves events for later use
 * in Event activity where all the events reviewed to the user
 * <p>
 * Must events come from com.aidn5.notifiers.* or other sources
 * <p>
 * Side-note: All events are pure saved as Integer in {@link SQLiteDatabase}
 * They are saved as Integer, so it can be used with {@link Context#getString(int, Object...)}
 * to retrieve them and show them with the current language.
 * {@link DataHolder#args} is provided to format {@link DataHolder#message} which why {@link DataHolder#getParsedMessage(Context)} is provided.
 * Why? Better performance, smaller file-size, faster look up and serves when on language changes :)
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class EventsSaver {
	private static final String TABLE_NAME = "'" + "data" + BuildConfig.VERSION_NAME.replace(".", "_") + "'";
	private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT ,'provider' INTEGER, 'title' INTEGER, 'message' INTEGER,'args' TEXT, 'registerTime' INTEGER);";
	private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	private static final String QUERY_PUT = "INSERT INTO " + TABLE_NAME + " ('provider', 'title', 'message', 'args', 'registerTime') VALUES (?, ?, ?, ?, ?);";
	private static final String QUERY_GET = "SELECT * FROM " + TABLE_NAME + " where 1=1 ORDER BY id DESC LIMIT ? OFFSET ?";

	private final SQLiteDatabase sql;

	/**
	 * Compiled statement of INSERT to speed up the process of inserting new data into the database
	 *
	 * @see #register(DataHolder)
	 */
	private final SQLiteStatement PREPARED_PUT;

	/**
	 * on {@link #fetch(int)} how many items should a page has
	 *
	 * @see #fetch(int)
	 */
	public int pageRows = 50;

	/**
	 * The maximum number of saved rows. Older rows will be deleted/not used
	 *
	 * @see #cleanOldRows()
	 */
	public int rowsLimit = 100;

	public EventsSaver(@NonNull Context context) {
		this.sql = SQLiteDatabase.openOrCreateDatabase(getDatabaseDir(context), null);

		// Create table in case first time running
		createTable();

		// Compile the statements and save them
		// so, we don't need to recompile them with every request
		PREPARED_PUT = this.sql.compileStatement(QUERY_PUT);
	}

	/**
	 * Register an event in the database
	 *
	 * @param dataHolder the data container which contains data to save
	 * @return the row ID of the last row inserted, if this insert is successful. -1 otherwise.
	 * @see #fetch(int)
	 */
	public synchronized long register(@NonNull DataHolder dataHolder) {
		PREPARED_PUT.clearBindings();

		PREPARED_PUT.bindLong(1, dataHolder.provider);

		PREPARED_PUT.bindLong(2, dataHolder.title);
		PREPARED_PUT.bindLong(3, dataHolder.message);
		PREPARED_PUT.bindString(4, dataHolder.argsToString());

		PREPARED_PUT.bindLong(5, System.currentTimeMillis() / 1000L);

		return PREPARED_PUT.executeInsert();
	}

	/**
	 * fetch data from the database and return them as {@link DataHolder}
	 * <p>
	 * page is provided in case older data is requested
	 * if page was 1, the first 25 rows will be fetched
	 * if page was 2, the first 25 will be skipped and the next 25 rows will be fetched
	 *
	 * @param page page number
	 * @return registered events
	 * @see #register(DataHolder)
	 */
	@NonNull
	public synchronized List<DataHolder> fetch(int page) {
		int limit = pageRows;
		int offset = (page * pageRows) - pageRows;
		Cursor cursor = this.sql.rawQuery(QUERY_GET, new String[]{limit + "", offset + ""});

		List<DataHolder> dataHolders = new ArrayList<>();

		while (cursor.moveToNext()) {
			DataHolder dataHolder = new DataHolder();
			dataHolders.add(dataHolder);

			dataHolder.provider = cursor.getInt(1);
			dataHolder.title = cursor.getInt(2);
			dataHolder.message = cursor.getInt(3);
			dataHolder.stringToArgs(cursor.getString(4));
			dataHolder.registerTime = cursor.getInt(5);
		}

		cursor.close();
		return dataHolders;
	}

	/**
	 * Remove old rows which will never be used again
	 *
	 * @return number of removed rows
	 * @see #clearAllRows()
	 */
	public synchronized int cleanOldRows() {
		return this.sql.delete(TABLE_NAME, "id not in (select id from " + TABLE_NAME + " order by id desc limit " + rowsLimit + ")", null);
	}

	/**
	 * Remove and clear the database from all the rows
	 * <p>
	 * A transaction will be made to speed up the process
	 *
	 * @see #cleanOldRows()
	 */
	public synchronized void clearAllRows() {
		this.sql.beginTransaction();

		dropTable();
		createTable();

		this.sql.setTransactionSuccessful();
		this.sql.endTransaction();
	}

	private void createTable() {
		this.sql.execSQL(QUERY_CREATE_TABLE);
	}

	private void dropTable() {
		this.sql.execSQL(QUERY_DROP_TABLE);
	}

	@NonNull
	private File getDatabaseDir(Context context) {
		return context.getDatabasePath(getClass().getSimpleName());
	}

	/**
	 * Data Holder holds the fetched data in container
	 *
	 * @see EventsSaver
	 */
	public static final class DataHolder {
		public int provider;
		public int title;
		public int message;
		public String[] args;
		public int registerTime = -1;

		public String getParsedMessage(@NonNull Context context) {
			// It meant to be used as arguments!!
			// NOT -> f(213, {a,b,c}) where
			// It meant to be -> f(213, a, b, c) !
			return context.getString(message, (Object[]) args);
		}

		public String getParsedTitle(@NonNull Context context) {
			return context.getString(title);
		}

		@NonNull
		private String argsToString() {
			if (args == null) return "";

			return new JSONArray(Arrays.asList(args)).toString();
		}

		private void stringToArgs(@NonNull String args) {
			if (args.isEmpty()) {
				this.args = null;
				return;
			}
			try {
				JSONArray jsonArray = new JSONArray(args);
				this.args = new String[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					String string = jsonArray.getString(i);
					this.args[i] = (string == null) ? "" : string;
				}
			} catch (JSONException e) {
				// never gonna happen ! since its gotten from #argsToString()
				e.printStackTrace();
				ACRA.getErrorReporter().handleException(e);
			}
		}
	}
}