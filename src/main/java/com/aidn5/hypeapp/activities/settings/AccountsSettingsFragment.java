package com.aidn5.hypeapp.activities.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.aidn5.hypeapp.R;

public class AccountsSettingsFragment extends PreferenceFragment {
	Preference usernameP;
	Preference hypixelAPI;
	Preference forumsAccountAddedP;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_accounts);

		usernameP = findPreference("username");
		hypixelAPI = findPreference("hypixelAPI");
		forumsAccountAddedP = findPreference("forumsAccountAdded");

		SettingsUIActivity.bindPreferenceSummaryToValue(findPreference("username"));
		SettingsUIActivity.bindPreferenceSummaryToValue(findPreference("hypixelAPI"));
		SettingsUIActivity.bindPreferenceSummaryToValue(findPreference("forumsAccountAdded"));
	}

	@Override
	public void onResume() {
		super.onResume();

		SettingsUIActivity.triggerBindPreferenceSummaryToValue(usernameP);
		SettingsUIActivity.triggerBindPreferenceSummaryToValue(hypixelAPI);
		SettingsUIActivity.triggerBindPreferenceSummaryToValue(forumsAccountAddedP);
	}
}
