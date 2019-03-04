package com.aidn5.hypeapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.evernote.android.job.JobRequest;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

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
	private DB db = null;

	public final IgnProvider getIgnProvider() {
		return ignProvider != null ? ignProvider : (ignProvider = new IgnProvider(getApplicationContext()));
	}

	public final SharedPreferences getSettings() {
		return settings != null ? settings : (settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
	}

	public final EventsSaver getEventsSaver() {
		return eventsSaver != null ? eventsSaver : (eventsSaver = new EventsSaver(getApplicationContext()));
	}

	public final DB getDB() {
		try {
			return db != null ? db : (db = DBFactory.open(getApplicationContext()));
		} catch (SnappydbException e) {
			e.printStackTrace();
			return db;
		}
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);

		ACRA.init(this);
	}
}