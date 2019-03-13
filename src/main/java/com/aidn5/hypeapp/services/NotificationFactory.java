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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.aidn5.hypeapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manage notificationsFactories.
 * <p>
 * It create the notificationsFactories and save them for a trigger to fire them.
 * The trigger will get triggered when the user turn on the screen.
 * It's a way to not annoy the user with notificationsFactories while away from the phone
 * <p>
 * Besides it create a notification body.
 * So, all the notification have one design.
 * <p>
 * AND to make the migration to later android versions easier.
 * Newer method are deprecated in later. This class will work around this issue too
 */
@SuppressWarnings({"deprecation", "WeakerAccess", "unused"})
//Everything is safe. OLD android version MUST use OLD deprecated methods
public class NotificationFactory {
	private final String channelId;

	private final NotificationChannel channel;
	private final Context context;
	private final NotificationManager notificationManager;
	private final List<Notification> notifications = new ArrayList<>();

	public NotificationFactory(Context context, String channelId, String channelName) {
		this.context = context;
		this.channelId = channelId;

		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		} else {
			channel = null;
		}
	}

	/**
	 * Show all the pending notifications to the user
	 *
	 * @return the number of the notifications, which has been sent
	 */
	public final long showAll() {
		synchronized (this.notifications) {
			long n = notifications.size();
			for (Notification notification : notifications) {
				showNotify(notification);
			}
			notifications.clear();
			return n;
		}
	}

	public final void notify(Notification.Builder noBuilder) {
		synchronized (this.notifications) {
			notifications.add(build(noBuilder));
		}
	}

	public final void notify(String title, String message) {
		notify(buildNotification(title, message));
	}

	public final void notifyNow(Notification.Builder nBuilder) {
		showNotify(build(nBuilder));
	}

	private Notification build(Notification.Builder noBuilder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return noBuilder.build();
		} else {
			return noBuilder.getNotification();
		}
	}

	private Notification.Builder buildNotification(String title, String message) {
		Notification.Builder notification;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notification = new Notification.Builder(context, context.getResources().getString(R.string.app_name));
			notification.setChannelId(channel.getId());
		} else {
			notification = new Notification.Builder(this.context);
		}

		notification.setSmallIcon(R.mipmap.ic_launcher);
		notification.setContentTitle(context.getResources().getString(R.string.app_name) + " - " + title);
		notification.setContentText(message);

		return notification;
	}

	private void showNotify(Notification notification) {
		notificationManager.notify((int) (Math.random() * 10000), notification);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		if (!obj.getClass().equals(this.getClass())) return false;

		NotificationFactory nf = (NotificationFactory) obj;
		return nf.channelId.equals(this.channelId);
	}
}
