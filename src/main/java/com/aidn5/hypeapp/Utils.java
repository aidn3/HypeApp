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
