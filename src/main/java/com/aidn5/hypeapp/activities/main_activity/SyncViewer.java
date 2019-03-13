/*
 * MIT License
 *
 * Copyright (c) [2019] [aidn5]
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


package com.aidn5.hypeapp.activities.main_activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
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
	//private final TextView syncErrorMessage;
	private final View syncView;

	SyncViewer(Activity activity, ServicesProvider servicesProvider) {
		super(true);

		this.activity = activity;
		this.servicesProvider = servicesProvider;

		this.nextSyncDateTX = activity.findViewById(R.id.main_sync_time_date);
		//this.syncErrorMessage = activity.findViewById(R.id.main_sync_error_message);
		this.syncView = activity.findViewById(R.id.main_sync_view);

		this.syncView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SyncDialog().execute();
			}
		});

		init();
	}

	private void init() {
		schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					activity.runOnUiThread(SyncViewer.this::setStatus);
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
	private final class SyncDialog extends AsyncTask<Void, Void, String> {
		private final Dialog dialog;

		private SyncDialog() {
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