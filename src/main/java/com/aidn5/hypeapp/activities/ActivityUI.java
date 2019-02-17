package com.aidn5.hypeapp.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.aidn5.hypeapp.ServicesProvider;

@SuppressLint("Registered")
public class ActivityUI extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(getApplicationContext(), ServicesProvider.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	protected final void bindToLocalService() {
		ServiceConnection mConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				ServicesProvider.LocalBinder binder = (ServicesProvider.LocalBinder) service;
				onServiceConnected_(binder.getService());
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
			}
		};

		Intent intent = new Intent(this, ServicesProvider.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	protected void onServiceConnected_(ServicesProvider servicesProvider) {
	}
}
