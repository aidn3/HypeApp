package com.aidn5.hypeapp.notifiers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aidn5.hypeapp.services.IgnProvider;
import com.snappydb.DB;


public final class AppEventsNotifier extends NotifierFactory {
	public AppEventsNotifier(Context context, DB db, IgnProvider ignProvider, SharedPreferences settings) {
		super(context, db, ignProvider, settings);
	}

	@Override
	public final void doLoop() {
		//TODO: [Auto-Gen] Add feature AppEventsNotifier
	}
}
