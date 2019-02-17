package com.aidn5.hypeapp.activities.main_activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.ServicesProvider;

import java.util.Timer;
import java.util.TimerTask;

final class SyncViewer extends Timer {
	private final Activity activity;
	private final ServicesProvider servicesProvider;

	private final TextView nextSyncDateTX;
	private final TextView syncErrorMessage;
	private final View syncView;

	SyncViewer(Activity activity, ServicesProvider servicesProvider) {
		super(true);

		this.activity = activity;
		this.servicesProvider = servicesProvider;

		this.nextSyncDateTX = activity.findViewById(R.id.main_sync_time_date);
		this.syncErrorMessage = activity.findViewById(R.id.main_sync_error_message);
		this.syncView = activity.findViewById(R.id.main_sync_view);

		this.syncView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("MEEE", "Tap Sync");
				new SynceDialog().execute();
			}
		});

		init();
	}

	private void init() {
		schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.v("MEEE", "sync refresh: start");
							setStatus();
							Log.v("MEEE", "sync refresh: end");
						}
					});
				} catch (Exception ignored) {
				}
			}
		}, 1, 500); // sync Every half second
	}

	private void setStatus() {
		long nextSync = servicesProvider.syncProvider.shouldSyncNow();
		if (nextSync < 0) {
			this.syncView.setVisibility(View.GONE);
			return;
		}
		this.syncView.setVisibility(View.VISIBLE);

		nextSyncDateTX.setText(createDateOrMessage((int) nextSync));
	}

	private String createDateOrMessage(int timeInSeconds) {
		if (timeInSeconds == 0) return this.activity.getString(R.string.syncing);

		int seconds = timeInSeconds % 60;
		int minutes = timeInSeconds / 60;
		if (minutes >= 60) {
			int hours = minutes / 60;
			minutes %= 60;
			return this.activity.getString(R.string.time_in_minutes_and_hours, hours, minutes);
		}
		return activity.getString(R.string.time_in_minutes_and_seconds, minutes, seconds);
	}

	@SuppressLint("StaticFieldLeak")
	private final class SynceDialog extends AsyncTask<Void, Void, String> {
		private final Dialog dialog;

		private SynceDialog() {
			this.dialog = new Dialog(activity);
			this.dialog.setContentView(R.layout.loading_progress);
			this.dialog.setCancelable(true);
			this.dialog.setTitle(R.string.loading);
		}

		@Override
		protected void onPreExecute() {
			this.dialog.show();

			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String statusMessage) {
			if (statusMessage != null)
				Toast.makeText(activity, statusMessage, Toast.LENGTH_LONG).show();

			this.dialog.dismiss();
		}

		@Override
		protected String doInBackground(Void... voids) {
			servicesProvider.syncProvider.syncNotifiersNow();
			return null;
		}
	}
}