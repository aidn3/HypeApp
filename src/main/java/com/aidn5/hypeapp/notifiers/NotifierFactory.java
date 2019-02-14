package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aidn5.hypeapp.G;
import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.ServicesProvider;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.NotificationFactory;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class NotifierFactory {
    final Context context;
    final IgnProvider ignProvider;
    final NotificationFactory notificationFactory;
    final DB db;

    final SharedPreferences settings;

    NotifierFactory(ServicesProvider servicesProvider) {
        this.ignProvider = G.getIgnProvider(servicesProvider);
        context = servicesProvider.getApplicationContext();
        settings = servicesProvider.settings;

        this.notificationFactory = new NotificationFactory(
                servicesProvider.getApplicationContext(),
                this.getClass().getSimpleName(),
                servicesProvider.getApplicationContext().getString(R.string.app_name)
        );


        DB db;
        try {
            db = DBFactory.open(servicesProvider.getApplicationContext(), this.getClass().getSimpleName());
        } catch (SnappydbException ignored) {
            db = null;
        }
        this.db = db;

    }

    public final void showNotifications() {
        notificationFactory.showAll();
    }

    final String netRequest(String url) throws IOException {

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

    public abstract void doLoop();
}
