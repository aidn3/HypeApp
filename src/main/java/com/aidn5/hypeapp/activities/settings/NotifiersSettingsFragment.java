package com.aidn5.hypeapp.activities.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.aidn5.hypeapp.R;

public class NotifiersSettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_notifications);
		SettingsUIActivity.bindPreferenceSummaryToValue(findPreference("notifiersSyncEvery"));
	}
}
