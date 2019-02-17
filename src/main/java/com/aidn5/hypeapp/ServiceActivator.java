package com.aidn5.hypeapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Wake the service up
 */
public class ServiceActivator extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (isValidAction(intent.getAction())) {
			Toast.makeText(context, "asd", Toast.LENGTH_LONG).show();
			context.startService(new Intent(context, ServicesProvider.class));
		}
	}

	private boolean isValidAction(String action) {
		if (action == null || action.isEmpty()) return false;

		return action.equals(Intent.ACTION_BOOT_COMPLETED);
	}
}
