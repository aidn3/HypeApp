package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.snappydb.DB;

public final class ForumsEventsNotifier extends NotifierFactory {
	public ForumsEventsNotifier(@NonNull Context context, @NonNull DB db, @NonNull IgnProvider ignProvider, @NonNull SharedPreferences settings, @NonNull EventsSaver eventsSaver) {
		super(context, db, ignProvider, settings, eventsSaver);
	}

	@Override
	public final void doLoop() {
		//TODO: [Auto-Gen] Add feature AppEventsNotifier
	}

	@Override
	public int getName() {
		return R.string.showNotificationOnForumsNotifications_title;
	}
}
