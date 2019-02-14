package com.aidn5.hypeapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aidn5.hypeapp.notifiers.AppEventsNotifier;
import com.aidn5.hypeapp.notifiers.ForumsEventsNotifier;
import com.aidn5.hypeapp.notifiers.FriendsEventsNotifier;
import com.aidn5.hypeapp.notifiers.GuildEventsNotifier;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.services.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ServicesProvider extends Service {

    private final HashMap<Class, NotifierFactory> notifiers = new HashMap<>();
    private final IBinder binder = new LocalBinder();
    private final SyncProvider syncProvider = new SyncProvider();
    private final BroadcastReceiverScreenOn broadcastReceiverScreenOn = new BroadcastReceiverScreenOn();

    public SharedPreferences settings;

    @Override
    public void onCreate() {
        super.onCreate();

        //Create services and needed objects
        this.settings = PreferenceManager.getDefaultSharedPreferences(this);
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

    private void createNotifiers() {
        notifiers.put(AppEventsNotifier.class, new AppEventsNotifier(this));
        notifiers.put(ForumsEventsNotifier.class, new ForumsEventsNotifier(this));
        notifiers.put(FriendsEventsNotifier.class, new FriendsEventsNotifier(this));
        notifiers.put(GuildEventsNotifier.class, new GuildEventsNotifier(this));
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

    public void syncNotifiersNow() {
        syncProvider.doLoop();
    }

    /**
     * @return 0: to sync now. -1: disabled. Positive-Integer: n of seconds before the next sync
     */
    public long shouldSyncNow() {
        boolean isSyncEnabled = settings.getBoolean(Settings.showNotifications.name(), true);
        if (!isSyncEnabled) return -1;

        //notifiersSyncEvery: is String. Android's settings API is designed to accept only strings as options.
        long syncEvery = Long.parseLong(settings.getString(Settings.notifiersSyncEvery.name(), "21600"));
        long lastSync = settings.getLong(Settings.lastSync.name(), -1);
        long currentTime = (System.currentTimeMillis() / 1000L);

        if (syncEvery < 0) return -1;//It is disabled

        long time = (lastSync + syncEvery) - currentTime;
        if (time < 0) return 0;
        return time;
    }

    public class LocalBinder extends Binder {
        public ServicesProvider getService() {
            return ServicesProvider.this;
        }
    }

    private class BroadcastReceiverScreenOn extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            for (Map.Entry<Class, NotifierFactory> notifierFactory : notifiers.entrySet()) {
                notifierFactory.getValue().showNotifications();//its already synchronized
            }
        }
    }

    // It has to run at fixed rate: so scheduleAtFixedRate is used
    // it must NEVER run twice at the same: time synchronized is used
    private class SyncProvider {
        private final Timer timer = new Timer(true);

        private void start() {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        doLoop();
                    } catch (Exception e) {
                        // This is background process
                        // We don't need to annoy the user
                        //TODO: add notification on error occurs
                        e.printStackTrace();
                    }
                }
            }, 1, 60000); //try to sync Every minute
        }

        private synchronized void doLoop() {
            if (shouldSyncNow() != 0) return;

            Log.v(ServicesProvider.class.getSimpleName(), "syncing...");


            for (Map.Entry<Class, NotifierFactory> notifierFactory : notifiers.entrySet()) {
                try {//We don't need to let services interfere with each other
                    //notifierFactory.getValue().doLoop();
                } catch (Exception ignored) {
                }
            }

            settings.edit()
                    .putLong(Settings.lastSync.name(), (System.currentTimeMillis() / 1000L))
                    .commit();
        }
    }
}
