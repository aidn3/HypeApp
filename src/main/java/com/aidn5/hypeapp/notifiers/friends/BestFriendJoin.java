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

import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;

public class BestFriendJoin extends NotifierFactory {
	protected BestFriendJoin(@NonNull Context context) {
		super(context);
	}

	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		/*boolean showNotification = settings.getBoolean(Settings.showNotificationOnFriendJoined.name(), true);

		if (!showNotification) return;

		String[] bestFriends = dm.getBestFriends();
		if (bestFriends == null || bestFriends.length == 0) return; //no-data -> nothing to process

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

		executor.shutdown();*/
	}

	@Override
	public int getName() {
		return 0;
	}
}
