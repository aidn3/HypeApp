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


package com.aidn5.hypeapp.activities.main_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.ServicesProvider;
import com.aidn5.hypeapp.activities.BaseActivity;
import com.aidn5.hypeapp.activities.settings.SettingsUIActivity;

import java.util.ArrayList;
import java.util.List;

import static com.aidn5.hypeapp.R.id.container;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


		// Set up the ViewPager with the sections adapter.
		ViewPager mViewPager = findViewById(container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// Choose the TAB which the user chose to auto load on start
		for (int i = 0; i < mSectionsPagerAdapter.fragments.size(); i++) {
			BaseFragment fragment = mSectionsPagerAdapter.fragments.get(i);
			if (fragment.autoLoad(getApplicationContext())) {
				mViewPager.setCurrentItem(i);
			}
		}

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);


		// Make the first tab in the tabs smaller (width)
		LinearLayout layout = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(0));
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
		layoutParams.weight = 0.3f;
		layout.setLayoutParams(layoutParams);

		bindToLocalService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	protected void onServiceConnected_(ServicesProvider servicesProvider) {
		new SyncViewer(this, servicesProvider);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.main_menu_settings) {
			startActivity(new Intent(this, SettingsUIActivity.class));
			return true;
		}

		return false;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {
		private final List<BaseFragment> fragments = new ArrayList<>();

		private SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

			// add Instances of the pages
			fragments.add(new EventsFragment());
			fragments.add(new GuildFragment());
			fragments.add(new FriendsFragment());
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			// Show the total pages.
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Return the title of the page
			return fragments.get(position).getTitle(getApplicationContext());
		}
	}
}
