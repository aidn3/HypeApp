package com.aidn5.hypeapp.activities.main_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;

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

		SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.main_swiperefresh);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				swipeRefreshLayout.setRefreshing(false);
			}
		});


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		ViewPager mViewPager = findViewById(container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

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
			fragments.add(new GuildFragment());
			fragments.add(new FriendsFragment());
		}

		@Override
		public Fragment getItem(int position) {
			return (Fragment) fragments.get(position);
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
