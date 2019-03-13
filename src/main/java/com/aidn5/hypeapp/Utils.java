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


package com.aidn5.hypeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

public class Utils {
	@NonNull
	public static String createDateOrMessage(@NonNull Context context, int timeInSeconds) {
		if (timeInSeconds <= 60) return context.getString(R.string.time_just_now);

		int seconds = timeInSeconds % 60;
		int minutes = timeInSeconds / 60;
		if (minutes >= 60) {
			int hours = minutes / 60;
			minutes %= 60;

			if (hours >= 24) {
				int days = hours / 24;
				hours %= 24;

				return context.getString(R.string.time_in_hours_and_days_ago, days, hours);
			}
			return context.getString(R.string.time_in_minutes_and_hours_ago, hours, minutes);
		}
		return context.getString(R.string.time_in_minutes_and_seconds_ago, minutes, seconds);
	}

	public static void createErrorLogDialog(final Activity context, String message, String errorLogs, boolean sendToDev) {
		//Create objects
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);

		//Inflater new layout to show
		final View layoutView = context.getLayoutInflater().inflate(R.layout.error_dialog_layout, null);

		//Declare the views of the layout
		final TextView messageTX = layoutView.findViewById(R.id.error_dialog_message);
		final TextView errorLogsTX = layoutView.findViewById(R.id.error_dialog_log);
		final View errorLogsView = layoutView.findViewById(R.id.error_dialog_logsView);

		//Set the attributes of the views
		messageTX.setText(message);
		errorLogsTX.setText(errorLogs);
		errorLogsView.setVisibility(View.GONE);

		//set attributes of the alert
		builder.setTitle(R.string.error);
		builder.setView(layoutView);


		builder.setNeutralButton(R.string.error_dialog_view_error, null);

		//Set buttons to interact with the user
		builder.setNegativeButton(android.R.string.ok, null);

		//If the error was internal/unknown make it possible to report it
		if (sendToDev) {
			builder.setPositiveButton(R.string.report_to_dev, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
		}

		//Build and create the alert
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				errorLogsView.setVisibility(View.VISIBLE);
				view.setVisibility(View.GONE);
			}
		});
	}
}
