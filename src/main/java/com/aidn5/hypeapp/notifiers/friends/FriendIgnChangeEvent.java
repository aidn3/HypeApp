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


package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class FriendIgnChangeEvent extends NotifierFactory {
	private static final int CHUNK_SIZE = 5; //How many players does every thread has to work with

	public FriendIgnChangeEvent(@NonNull Context context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		boolean showNotification = settings.getBoolean(Settings.showNotificationOnFriendIgnChanged.name(), true);

		// We don't need to look up the usernames anymore,
		// since we won't notify the user anyways
		if (!showNotification) return;

		String[] playersUUID = getPlayersUUID(settings);
		if (playersUUID == null) return; //No-data -> nothing to do -> return

		startLoad(eventsSaver, ignProvider, playersUUID);
	}

	private void startLoad(@NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull String[] playersUUID) {
		// Split the data to make the look up faster by multi-threading
		// requesting data from the internet by speed of 5 kb/s (2 requests per second)
		// is too slow and takes lot of time for array bigger than 50
		// Using multi-threading can speed up the process
		// ThreadPoolExecutor is required
		// since we don't need to overwhelm the system
		// with too many threads running at the same time
		// also, This is why the thread ThreadPoolExecutor has maximumPoolSize of 5

		String[][] playersUUIDChunk = splitArray(playersUUID, CHUNK_SIZE);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(playersUUIDChunk.length + 1));

		for (String[] chunk : playersUUIDChunk) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					lookUpUUIDs(eventsSaver, ignProvider, chunk);
				}
			});
		}

		while (executor.getTaskCount() != executor.getCompletedTaskCount()) {
			Log.v(getClass().getSimpleName(), "count=" + executor.getTaskCount() + "," + executor.getCompletedTaskCount());
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}

		executor.shutdown();
	}

	@Override
	public int getName() {
		return R.string.showNotificationOnFriendIgnChanged_title;
	}

	private void lookUpUUIDs(@NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull String[] playersUUID) {
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

				EventsSaver.DataHolder dataHolder = new EventsSaver.DataHolder();

				dataHolder.provider = getName();
				dataHolder.title = R.string.friendsEventIgnChangedTitle;
				dataHolder.message = R.string.friendsEventIgnChangedMessage;
				dataHolder.args = new String[]{user1, user2};

				eventsSaver.register(dataHolder);
			}
		}
	}

	/**
	 * Split array to smaller arrays
	 * <p>
	 * <b>example to chunkSize:</b>
	 * <p>
	 * chunkSize = 3
	 * [1, 2, 3]
	 * [4, 5]
	 * <p>
	 * chunkSize = 4
	 * [1, 2, 3, 4]
	 * [5]
	 * <p>
	 * chunkSize = 5
	 * [1, 2, 3, 4, 5]
	 * <p>
	 * <p>
	 * https://stackoverflow.com/questions/27857011/how-to-split-a-string-array-into-small-chunk-arrays-in-java
	 *
	 * @param arrayToSplit the array to split
	 * @param chunkSize    how many items does every array has to have at max
	 * @return arrays of smaller arrays of the original array
	 */
	@SuppressWarnings("SameParameterValue")
	@NonNull
	private String[][] splitArray(@NonNull String[] arrayToSplit, int chunkSize) {
		chunkSize = Math.abs(chunkSize);

		// first we have to check if the array can be split in multiple
		// arrays of equal 'chunk' size
		int rest = arrayToSplit.length % chunkSize;  // if rest>0 then our last array will have less elements than the others
		// then we check in how many arrays we can split our input array
		int chunks = arrayToSplit.length / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for the 'rest'
		// now we know how many arrays we need and create our result array
		String[][] arrays = new String[chunks][];
		// we create our resulting arrays by copying the corresponding
		// part from the input array. If we have a rest (rest>0), then
		// the last array will have less elements than the others. This
		// needs to be handled separately, so we iterate 1 times less.
		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			// this copies 'chunk' times 'chunkSize' elements into a new array
			arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
		}
		if (rest > 0) { // only when we have a rest
			// we copy the remaining elements into the last chunk
			arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
		}
		return arrays; // that's it
	}

	/**
	 * Retrieve the Up-To-Date friend list from the server
	 *
	 * @return Up-To-Date friends list associated by their UUIDs
	 */
	@Nullable
	private String[] getPlayersUUID(SharedPreferences sp) {
		//TODO: [Feature] FriendIgnChangeEvent#getPlayersUUID: add include exclude to settings
		HypixelReplay hypixelReplay = new FriendsRequest(context).getFriendsByUserUUID(sp);
		return (String[]) hypixelReplay.value; // Either null or String[]
	}
}
