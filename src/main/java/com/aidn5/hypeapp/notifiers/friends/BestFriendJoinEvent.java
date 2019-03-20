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

package com.aidn5.hypeapp.notifiers.friends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BestFriendJoinEvent extends NotifierFactory {
	private static final String SETTINGS_WAS_SHOW_NOTIFICATION_ON = BestFriendJoinEvent.class.getSimpleName() + "_wasShowNotificationOn";
	private static final String SETTINGS_FRIENDS_WERE_ONLINE = BestFriendJoinEvent.class.getSimpleName() + "_friendsWereOnline";
	private static final int CHUNK_SIZE = 2;

	public BestFriendJoinEvent(@NonNull Context context) {
		super(context);
	}

	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) throws Exception {
		boolean showNotification = settings.getBoolean(Settings.showNotificationOnFriendJoined.name(), true);

		dm.put(SETTINGS_WAS_SHOW_NOTIFICATION_ON, showNotification);
		if (!showNotification) return;

		String[] bestFriends = dm.getBestFriends();
		if (bestFriends == null || bestFriends.length == 0) return; //no-data -> nothing to process

		String[] friendsWereOnline = dm.getArray(SETTINGS_FRIENDS_WERE_ONLINE, String.class);

		final List<String> onlineFriends = new ArrayList<>(5);

		String[][] playersUUIDChunk = splitUuid(bestFriends);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(playersUUIDChunk.length + 1));

		for (String[] chunk : playersUUIDChunk) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					for (String uuid : chunk) {

						synchronized (onlineFriends) {
							onlineFriends.add(uuid);
						}
					}
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

	@NonNull
	private String[][] splitUuid(@NonNull String[] uuidToSplit) {

		// first we have to check if the array can be split in multiple
		// arrays of equal 'chunk' size
		int rest = uuidToSplit.length % BestFriendJoinEvent.CHUNK_SIZE;  // if rest>0 then our last array will have less elements than the others
		// then we check in how many arrays we can split our input array
		int chunks = uuidToSplit.length / BestFriendJoinEvent.CHUNK_SIZE + (rest > 0 ? 1 : 0); // we may have to add an additional array for the 'rest'
		// now we know how many arrays we need and create our result array
		String[][] arrays = new String[chunks][];
		// we create our resulting arrays by copying the corresponding
		// part from the input array. If we have a rest (rest>0), then
		// the last array will have less elements than the others. This
		// needs to be handled separately, so we iterate 1 times less.
		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			// this copies 'chunk' times 'chunkSize' elements into a new array
			arrays[i] = Arrays.copyOfRange(uuidToSplit, i * BestFriendJoinEvent.CHUNK_SIZE, i * BestFriendJoinEvent.CHUNK_SIZE + BestFriendJoinEvent.CHUNK_SIZE);
		}
		if (rest > 0) { // only when we have a rest
			// we copy the remaining elements into the last chunk
			arrays[chunks - 1] = Arrays.copyOfRange(uuidToSplit, (chunks - 1) * BestFriendJoinEvent.CHUNK_SIZE, (chunks - 1) * BestFriendJoinEvent.CHUNK_SIZE + rest);
		}
		return arrays; // that's it
	}

	@Override
	public int getName() {
		return 0;
	}
}
