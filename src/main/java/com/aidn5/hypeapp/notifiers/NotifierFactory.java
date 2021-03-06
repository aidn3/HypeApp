package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.NotificationFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class NotifierFactory {
	protected final Context context;
	protected final NotificationFactory notificationFactory;

	protected NotifierFactory(@NonNull Context context) {
		this.context = context;

		this.notificationFactory = new NotificationFactory(
				context,
				this.getClass().getSimpleName(),
				context.getString(R.string.app_name)
		);
	}

	public final void showNotifications() {
		notificationFactory.showAll();
	}

	protected final String netRequest(@NonNull String url) throws IOException {
		URL URL = new URL(url);

		URLConnection urlConnection = URL.openConnection();
		InputStream inputStream = urlConnection.getInputStream();

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}

		inputStream.close();
		return result.toString("UTF-8");
	}

	/**
	 * Indicates whether the user is online on hypixel network or not
	 * <p>
	 * If the user is online there is no need to show notifications at all,
	 * since the user will get notified by the server itself
	 *
	 * @return TRUE if the user online on hypixel network. FALSE if not
	 */
	@SuppressWarnings("SameReturnValue")
	protected boolean isTheUserOnline() {
		//TODO: [Auto-Gen] code the method FriendRemovalEvent#isTheUserOnline()
		return false;
	}

	/**
	 * The interface, which will be called from
	 * {@link com.aidn5.hypeapp.ServicesProvider.SyncProvider#doLoop(boolean)}
	 *
	 * @param eventsSaver Instance to save the events
	 */
	public abstract void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) throws Exception;

	/**
	 * Resource refers to the title of the event in {@link R.string}
	 *
	 * @return The title of this event as a resource from {@link R.string}
	 */
	public abstract int getName();
}
