package com.aidn5.hypeapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.aidn5.hypeapp.notifiers.ForumsEventsNotifier;
import com.aidn5.hypeapp.notifiers.GuildEventsNotifier;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.notifiers.app.AppAnnouncementsEvent;
import com.aidn5.hypeapp.notifiers.friends.FriendIgnChangeEvent;
import com.aidn5.hypeapp.notifiers.friends.FriendRemovalEvent;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;
import com.snappydb.DB;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public final class ServicesProvider extends Service {
	public final SyncProvider syncProvider = new SyncProvider();
	private final List<NotifierFactory> notifiers = new ArrayList<>();
	private final IBinder binder = new LocalBinder();
	private final BroadcastReceiverScreenOn broadcastReceiverScreenOn = new BroadcastReceiverScreenOn();

	private G g;

	@Override
	public void onCreate() {
		super.onCreate();
		g = (G) getApplication();
		//Create services and needed objects
		createNotifiers();
		syncProvider.start();

		//Register the receiver to receive when to trigger the showNotifications
		//In the settings the user can switch the notifications
		//from notify me anytime to only when im using the phone
		//This receiver is activated when the user turn on the screen
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(broadcastReceiverScreenOn, screenStateFilter);
	}

	public SharedPreferences getSettings() {
		return ((G) getApplication()).getSettings();
	}

	private void createNotifiers() {
		DB db = g.getDB();
		IgnProvider ignProvider = g.getIgnProvider();
		SharedPreferences sp = g.getSettings();

		notifiers.add(new AppAnnouncementsEvent(this, db, ignProvider, sp));

		notifiers.add(new ForumsEventsNotifier(this, db, ignProvider, sp));

		notifiers.add(new FriendRemovalEvent(this, db, ignProvider, sp));
		notifiers.add(new FriendIgnChangeEvent(this, db, ignProvider, sp));

		notifiers.add(new GuildEventsNotifier(this, db, ignProvider, sp));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY; //Let the service live! :P
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiverScreenOn);
		syncProvider.timer.cancel();

		super.onDestroy();
	}

	public final class LocalBinder extends Binder {
		public ServicesProvider getService() {
			return ServicesProvider.this;
		}
	}

	private final class BroadcastReceiverScreenOn extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			for (NotifierFactory notifierFactory : notifiers) {
				notifierFactory.showNotifications();//its already synchronized
			}
		}
	}

	// It has to run at fixed rate: so scheduleAtFixedRate is used
	// it must NEVER run twice at the same: time synchronized is used
	public final class SyncProvider {
		private final Timer timer = new Timer(true);

		private void start() {
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						doLoop(false);
					} catch (Exception e) {
						// This is background process
						// We don't need to annoy the user
						//TODO: [Feature] add notification on error occurs
						e.printStackTrace();
						ACRA.getErrorReporter().handleSilentException(e);
					}
				}
			}, 1, 60000); //try to sync Every minute
		}

		private synchronized void doLoop(boolean forceSync) {
			if (!forceSync && shouldSyncNow() != 0) return;

			Log.v(this.getClass().getSimpleName(), "syncing...");

			EventsSaver eventsSaver = g.getEventsSaver();

			for (NotifierFactory notifierFactory : notifiers) {
				try {//We don't need to let services interfere with each other
					Log.v(this.getClass().getSimpleName(), "Syncing: " + notifierFactory.getClass().getSimpleName());
					notifierFactory.doLoop(eventsSaver);
				} catch (Exception e) {
					e.printStackTrace();
					ACRA.getErrorReporter().handleSilentException(e);
				}
			}

			getSettings().edit()
					.putLong(Settings.lastSync.name(), (System.currentTimeMillis() / 1000L))
					.commit();

			Log.v(this.getClass().getSimpleName(), "Done syncing");

			// After syncing, if the user did opt
			// to receive the notifications directly after it come
			// or did sync manually (forceSync) then just show it :)
			if (forceSync || !getSettings().getBoolean(Settings.showNotificationsOnlyNotAFK.name(), true)) {
				for (NotifierFactory notifierFactory : notifiers) {
					notifierFactory.showNotifications();
				}
			}
		}

		/**
		 * @return 0: to sync now. -1: disabled. Positive-Integer: n of seconds before the next sync
		 */
		public long shouldSyncNow() {
			boolean isSyncEnabled = getSettings().getBoolean(Settings.showNotifications.name(), true);
			if (!isSyncEnabled) return -1;

			//notifiersSyncEvery: is String. Android's settings API is designed to accept only strings as options.
			long syncEvery = Long.parseLong((Objects.requireNonNull(getSettings().getString(Settings.notifiersSyncEvery.name(), "21600"))));
			long lastSync = getSettings().getLong(Settings.lastSync.name(), -1);
			long currentTime = (System.currentTimeMillis() / 1000L);

			if (syncEvery < 0) return -1;//It is disabled

			long time = (lastSync + syncEvery) - currentTime;
			if (time < 0) return 0;
			return time;
		}

		public void syncNotifiersNow() {
			syncProvider.doLoop(true);
		}
	}
}
