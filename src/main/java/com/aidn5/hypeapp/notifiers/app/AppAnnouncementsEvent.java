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
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) throws Exception {
		String data;

		if (!dm.exists(SETTINGS_LAST_REQUEST)) { // On first run there is not data
			dm.put(SETTINGS_LAST_REQUEST, System.currentTimeMillis() / 1000L);
			data = fetchData(0);
		} else {
			data = fetchData(dm.get(SETTINGS_LAST_REQUEST, int.class));
		}

		if (data == null) return; // no data -> nothing to show/update -> return

		boolean showNotification = settings.getBoolean(Settings.showNotificationOnDeveloperAnnouncement.name(), true);

		doAnnouncements(eventsSaver, showNotification, data);

		// Save the current time to fetch the next data up this time
		dm.put(SETTINGS_LAST_REQUEST, System.currentTimeMillis() / 1000L);
	}

	/**
	 * Loop through the announcements, save them and show notification when necessary
	 *
	 * @param eventsSaver      the given object to save the events
	 * @param showNotification opt to show or not show the notification
	 * @param data             the data to parse with {@link JSONArray}
	 */
	private void doAnnouncements(@NonNull EventsSaver eventsSaver, boolean showNotification, @NonNull String data) throws JSONException {
		JSONArray announcements = new JSONArray(data);
		for (int i = 0; i < announcements.length(); i++) {

			JSONObject announce = announcements.getJSONObject(i);

			String message = announce.getString("message");

			// If a notification is REALLY important, then show it
			if (showNotification || announce.getBoolean("isImportant")) {
				notificationFactory.notify(
						context.getString(R.string.appEventAnnouncementTitle),
						message
				);
			}

			EventsSaver.DataHolder dataHolder = new EventsSaver.DataHolder();

			dataHolder.provider = getName();
			dataHolder.title = R.string.appEventAnnouncementTitle;
			dataHolder.message = R.string.stringContainer;
			dataHolder.args = new String[]{message};

			eventsSaver.register(dataHolder);
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
	private String fetchData(int timeStamp) {
		try {
			String url = URL_LINK + "?lastRequest=" + timeStamp;
			return netRequest(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
