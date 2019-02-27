package com.aidn5.hypeapp.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	private static final String TABLE_NAME = "data";
	private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ('provider' INTEGER, 'title' INTEGER, 'message' INTEGER,'args' TEXT, 'registerTime' INTEGER);";
	private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	private static final String QUERY_PUT = "INSERT INTO " + TABLE_NAME + " ('provider', 'title', 'message', 'args', 'registerTime') VALUES (?, ?, ?, ?, ?);";
	private static final String QUERY_GET = "SELECT * FROM " + TABLE_NAME + " where 1=1 LIMIT ? OFFSET ?";

	private final SQLiteDatabase sql;
	private final SQLiteStatement PREPARED_PUT;
	private final SQLiteStatement PREPARED_GET;
	/**
	 * on {@link #fetch(int)} how many items should a page has
	 *
	 * @see #fetch(int)
	 */
	public int pageRows = 25;

	public EventsSaver(@NonNull Context context) {
		this.sql = SQLiteDatabase.openOrCreateDatabase(getDatabaseDir(context), null);

		// Create table in case first time running
		createTable();

		// Compile the statements and save them
		// so, we don't need to recompile them with every request
		PREPARED_PUT = this.sql.compileStatement(QUERY_PUT);
		PREPARED_GET = this.sql.compileStatement(QUERY_GET);
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

		PREPARED_PUT.bindLong(5, System.currentTimeMillis());

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
			dataHolder.provider = cursor.getInt(0);
			dataHolder.title = cursor.getInt(1);
			dataHolder.message = cursor.getInt(2);
			dataHolder.stringToArgs(cursor.getString(3));
			dataHolder.registerTime = cursor.getInt(4);
		}

		cursor.close();
		return dataHolders;
	}

	/**
	 * Remove old rows which are older than n
	 * <p>
	 * A transaction will be made to speed up
	 * the process of removing every individual row
	 *
	 * @see #clearAllRows()
	 */
	public synchronized int cleanOldRows(long olderThan, @NonNull TimeUnit timeUnit) {
		this.sql.beginTransaction();

		long millis = System.currentTimeMillis() - timeUnit.toMillis(olderThan);
		int rows = this.sql.delete(TABLE_NAME, "registerTime < ?", new String[]{millis + ""});

		this.sql.setTransactionSuccessful();

		return rows;
	}

	/**
	 * Remove and clear the database from all the rows
	 * <p>
	 * A transaction will be made to speed up the process
	 *
	 * @see #cleanOldRows(long, TimeUnit)
	 */
	public synchronized void clearAllRows() {
		this.sql.beginTransaction();

		dropTable();
		createTable();

		this.sql.setTransactionSuccessful();
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
	public final class DataHolder {
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
			StringBuilder builder = new StringBuilder();
			for (String s : args) {
				builder.append(s).append("\r\n");
			}
			return builder.toString();
		}

		private void stringToArgs(@NonNull String args) {
			this.args = args.split("\r\n");
		}
	}
}