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


package com.aidn5.hypeapp.activities.settings;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aidn5.hypeapp.G;
import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.activities.BaseActivity;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.Settings;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SetApiActivity extends BaseActivity {
	private EditText apiEditText;
	private boolean isDestroyed = false;
	private boolean isApiSet = false;

	@Override
	protected void onDestroy() {
		isDestroyed = true;

		Intent returnIntent = new Intent();
		setResult(
				isApiSet ? Activity.RESULT_OK : Activity.RESULT_CANCELED,
				returnIntent);

		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.set_api);

		apiEditText = findViewById(R.id.set_api_txt);
		findViewById(R.id.set_api_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new HypixelAPIChecker(SetApiActivity.this).execute(apiEditText.getText().toString());
			}
		});
	}


	@SuppressLint("StaticFieldLeak")
	private class HypixelAPIChecker extends AsyncTask<String, Void, String> {
		private final Dialog dialog;
		private final SharedPreferences sp;
		private final IgnProvider ignProvider;
		private final Activity activity;

		private String usernameMessage;

		HypixelAPIChecker(Activity activity) {
			G g = (G) activity.getApplication();

			this.activity = activity;
			this.sp = g.getSettings();
			this.ignProvider = g.getIgnProvider();

			this.dialog = new Dialog(activity);
			this.dialog.setContentView(R.layout.loading_progress);
			this.dialog.setCancelable(false);
			this.dialog.setTitle(R.string.loading);
		}

		@Override
		protected String doInBackground(String... strings) {
			String api = strings[0];
			try {
				if (api.isEmpty()) {
					return getString(R.string.hypixel_api_not_typed);
				}


				String data = netRequest("https://api.hypixel.net/key?key=" + api);
				if (data.isEmpty()) {
					return getString(R.string.the_server_did_not_response);
				}

				JSONObject jsonObject = new JSONObject(data);

				if (!jsonObject.getBoolean("success")) {
					return getString(R.string.invalid_api);
				}


				SharedPreferences.Editor ed = sp.edit();

				ed.putString(Settings.hypixelAPI.name(), api);
				ed.commit();
				isApiSet = true;

				String uuid = jsonObject.getJSONObject("record").getString("ownerUuid");
				String username = this.ignProvider.getUsername(uuid, false);

				ed.putString(Settings.username.name(), username);
				ed.putString(Settings.userUUID.name(), uuid);
				ed.apply();

				usernameMessage = username;

			} catch (Exception ignored) {
				return getString(R.string.something_went_wrong);
			}

			return getString(R.string.api_has_been_added);
		}

		@Override
		protected void onPreExecute() {
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(String statusMessage) {

			Toast.makeText(this.activity, statusMessage, Toast.LENGTH_LONG).show();

			this.dialog.dismiss();
			if (usernameMessage != null) showUsername(usernameMessage);
		}

		private void showUsername(String username) {
			AlertDialog.Builder msg = new AlertDialog.Builder(this.activity);
			msg
					.setMessage(activity.getString(R.string.username_detected, username))
					.setCancelable(false)
					.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							Handler handler = new Handler();
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									activity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!isDestroyed) activity.onBackPressed();
										}
									});
								}
							}, 1000);
						}
					})
					.create().show();
		}

		private String netRequest(String url) throws IOException {

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
	}
}