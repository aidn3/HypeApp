package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;

public final class ForumsEventsNotifier extends NotifierFactory {
	public ForumsEventsNotifier(@NonNull Context context) {
		super(context);
	}

	@Override
	public void doLoop(@NonNull DataManager dm, @NonNull EventsSaver eventsSaver, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings) {
		//TODO: [Auto-Gen] Add feature AppEventsNotifier
	}

	@Override
	public int getName() {
		return R.string.showNotificationOnForumsNotifications_title;
	}
}
