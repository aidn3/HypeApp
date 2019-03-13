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


package com.aidn5.hypeapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.evernote.android.job.JobRequest;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraNotification;
import org.acra.annotation.AcraScheduler;
import org.acra.sender.HttpSender;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraScheduler(
		requiresNetworkType = JobRequest.NetworkType.UNMETERED,
		requiresBatteryNotLow = true,
		requiresDeviceIdle = true
)
@AcraHttpSender(
		httpMethod = HttpSender.Method.POST,
		uri = "https://collector.tracepot.com/1666058e"
)
@AcraLimiter()
@AcraNotification(
		resTitle = R.string.app_name,
		resText = R.string.unhandled_exception_message,
		resChannelName = R.string.app_notification_channel
)
public class G extends Application {
	private IgnProvider ignProvider;
	private EventsSaver eventsSaver;
	private SharedPreferences settings;
	private DataManager dm = null;

	public final IgnProvider getIgnProvider() {
		return ignProvider != null ? ignProvider : (ignProvider = new IgnProvider(getApplicationContext()));
	}

	public final SharedPreferences getSettings() {
		return settings != null ? settings : (settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
	}

	public final EventsSaver getEventsSaver() {
		return eventsSaver != null ? eventsSaver : (eventsSaver = new EventsSaver(getApplicationContext()));
	}

	public final DataManager getDm() {
		return dm != null ? dm : (dm = new DataManager(getApplicationContext()));
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);

		ACRA.init(this);
	}
}