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


package com.aidn5.hypeapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aidn5.hypeapp.G;
import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.Utils;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;

//The code is executed in this way
// onCreate(): bind to the background service
// onServiceConnected_(): callback on bind the service, then create and run PlayersLoader{}
// PlayersLoader{}: Load friends and add username getter to the poolExecutor and then as callback run initialize()
// initialize(): Set views and their settings and use playersSelectAdapter{}
// playersSelectAdapter{}: create view to the list
public class BestFriendsList extends BaseActivity {
	private static final int SELECT_PLAYER_CODE = 1;
	private SharedPreferences settings;
	private String[] friends;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = ((G) getApplication()).getSettings();

		new PlayersLoader(this).execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_PLAYER_CODE) {
			if (resultCode == RESULT_OK) {
				String[] UUIDs = data.getStringArrayExtra(SelectPlayersActivity.SELECTED_PLAYERS);

			} else {
				Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
			}
		}
	}

	//Initialize playerList and add username providers to poolExecutor
	@SuppressLint("StaticFieldLeak")
	private class PlayersLoader extends AsyncTask<Void, Void, String> {
		private final Dialog dialog;
		private final Activity activity;
		private String showMessageOnFinish = null;

		private String errorDialog = null;
		private boolean sendToDev = false;
		private String stacktrace = null;

		private boolean onSuccess = false;

		private PlayersLoader(Activity activity) {
			this.activity = activity;

			this.dialog = new Dialog(activity);
			this.dialog.setContentView(R.layout.loading_progress);
			this.dialog.setCancelable(false);
			this.dialog.setTitle(R.string.loading);
		}

		@Override
		protected void onPreExecute() {
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(String statusMessage) {

			Toast.makeText(this.activity, statusMessage, Toast.LENGTH_LONG).show();

			this.dialog.dismiss();
			if (this.showMessageOnFinish != null)
				Toast.makeText(this.activity, this.showMessageOnFinish, Toast.LENGTH_LONG).show();

			if (this.errorDialog != null) {
				Utils.createErrorLogDialog(activity, errorDialog, stacktrace, sendToDev);
			}

			if (onSuccess) initialize();
		}

		private void initialize() {
			Intent intent = new Intent(getApplicationContext(), SelectPlayersActivity.class);
			intent.putExtra(SelectPlayersActivity.TITLE, getTitle());
			intent.putExtra(SelectPlayersActivity.PLAYERS_UUIDs, friends);
			startActivityForResult(intent, 1);
		}

		@Override
		protected String doInBackground(Void... voids) {
			HypixelReplay hypixelReplay = new FriendsRequest(activity).getFriendsByUserUUID(settings);
			if (!hypixelReplay.isSuccess) {
				hypixelReplay.exception.getMessage();
				errorDialog = hypixelReplay.exception.getMessage();

				String typeErrorMessage = hypixelReplay.exception.getErrorTypeMessage(activity);
				if (typeErrorMessage != null) errorDialog = typeErrorMessage;

				stacktrace = hypixelReplay.exception.getMessage() +
						"\r\n\r\n" + Log.getStackTraceString(hypixelReplay.exception) +
						"\r\n\r\n" + hypixelReplay.fullResponse;
				return errorDialog;
			}

			friends = (String[]) hypixelReplay.value;

			onSuccess = true;

			return null;
		}
	}
}