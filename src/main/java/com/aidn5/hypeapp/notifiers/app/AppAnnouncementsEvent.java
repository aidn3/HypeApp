package com.aidn5.hypeapp.notifiers.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aidn5.hypeapp.BuildConfig;
import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import org.acra.ACRA;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Developers Announcements:
 * When the developers of this APP gives an announcement about something.
 * <p>
 * This class fetch the data from the server and show the announcements as notifications
 */
public final class AppAnnouncementsEvent extends NotifierFactory {
	private static final String SETTINGS_LAST_REQUEST = AppAnnouncementsEvent.class.getSimpleName() + "_LastRequest";

	/**
	 * With the up-coming versions of this class,
	 * more features will be add and probably refactor of the whole class will happen.
	 * This helps the server to determine how to create the response.
	 * <p>
	 * So, all versions of the app (earliest and oldest version)
	 * can receive and handle the data from the server without crashes
	 */
	private static final String HANDLER_VERSION = "1.0";
	private static final String URL_LINK = "https://aidn55.000webhostapp.com/anouncements.php?id=" + BuildConfig.APPLICATION_ID + "&version=" + HANDLER_VERSION;

	public AppAnnouncementsEvent(@NonNull Context context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		String data = fetchData(dm);
		if (data == null) return; // no data -> nothing to show/update -> return

		boolean showNotification = settings.getBoolean(Settings.showNotificationOnDeveloperAnnouncement.name(), true);
		try {
			JSONArray announcements = new JSONArray(data);
			for (int i = 0; i < announcements.length(); i++) {
				try {
					JSONObject announce = announcements.getJSONObject(i);

					// If a notification is REALLY important, then show it
					if (showNotification || announce.getBoolean("isImportant")) {
						String message = announce.getString("message");

						notificationFactory.notify(
								context.getString(R.string.appEventAnnouncementTitle),
								message
						);

						EventsSaver.DataHolder dataHolder = new EventsSaver.DataHolder();

						dataHolder.provider = getName();
						dataHolder.title = R.string.appEventAnnouncementTitle;
						dataHolder.message = R.string.stringContainer;
						dataHolder.args = new String[]{message};

						eventsSaver.register(dataHolder);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					ACRA.getErrorReporter().handleSilentException(e);
				}
			}
		} catch (Exception e) {
			// This should NEVER happen since the class should match the server.
			// Throwing exception means there is a conflict between the server and the app
			// We need to report the error and fix it from the server-side
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		} finally {
			dm.put(SETTINGS_LAST_REQUEST, System.currentTimeMillis() / 1000L);
		}
	}

	@Override
	public int getName() {
		return R.string.showNotificationOnDeveloperAnnouncement_title;
	}

	/**
	 * fetch the data from the server and return
	 * <p>
	 * the announcements, which can be later used with {@link JSONArray}.
	 * Every index of {@link JSONArray} has {@link JSONObject} (one announcement).
	 * Every Announcement contains the properties of the announcement
	 *
	 * @return the announcements
	 */
	@Nullable
	private String fetchData(DataManager dm) {
		try {
			String url = URL_LINK + "?lastRequest=" + dm.get(SETTINGS_LAST_REQUEST, int.class, 0);
			return netRequest(url);
		} catch (IOException ignored) {
			return null;
		}
	}
}
