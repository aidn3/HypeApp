package com.aidn5.hypeapp;

import android.content.Context;
import android.support.annotation.NonNull;

public class Utils {
	@NonNull
	public static String createDateOrMessage(@NonNull Context context, int timeInSeconds) {
		if (timeInSeconds <= 60) return context.getString(R.string.time_just_now);

		int seconds = timeInSeconds % 60;
		int minutes = timeInSeconds / 60;
		if (minutes >= 60) {
			int hours = minutes / 60;
			minutes %= 60;
			return context.getString(R.string.time_in_minutes_and_hours_ago, hours, minutes);
		}
		return context.getString(R.string.time_in_minutes_and_seconds_ago, minutes, seconds);
	}
}
