package com.aidn5.hypeapp.activities.main_activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.ServicesProvider;
import com.aidn5.hypeapp.activities.ActivityUI;
import com.aidn5.hypeapp.activities.settings.SettingsUIActivity;

import java.util.Timer;
import java.util.TimerTask;

import static com.aidn5.hypeapp.R.id.container;

public class MainActivity extends ActivityUI {
    private SyncViewer syncViewer;

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
        syncViewer = new SyncViewer(this, servicesProvider);
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

    public static class GuildView extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.select_players_rootview, container, false);
        }
    }

    public static class FriendsView extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.select_players_rootview, container, false);
        }
    }

    private class SyncViewer extends Timer {
        private final Activity activity;
        private final ServicesProvider servicesProvider;

        private final TextView nextSyncDateTX;
        private final TextView syncErrorMessage;
        private final View syncView;

        SyncViewer(Activity activity, ServicesProvider servicesProvider) {
            super(true);

            this.activity = activity;
            this.servicesProvider = servicesProvider;

            this.nextSyncDateTX = activity.findViewById(R.id.main_sync_time_date);
            this.syncErrorMessage = activity.findViewById(R.id.main_sync_error_message);
            this.syncView = activity.findViewById(R.id.main_sync_view);

            init();
        }

        private void init() {
            schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                syncNow(servicesProvider);
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }
            }, 1, 1000); // sync Every second
        }

        private void syncNow(ServicesProvider servicesProvider) {
            long nextSync = servicesProvider.shouldSyncNow();
            if (nextSync < 0) {
                this.syncView.setVisibility(View.GONE);
                return;
            }
            this.syncView.setVisibility(View.VISIBLE);

            nextSyncDateTX.setText(createDateOrMessage((int) nextSync));
        }

        private String createDateOrMessage(int timeInSeconds) {
            if (timeInSeconds == 0) return this.activity.getString(R.string.syncing);

            int seconds = timeInSeconds % 60;
            int minutes = timeInSeconds / 60;
            if (minutes >= 60) {
                int hours = minutes / 60;
                minutes %= 60;
                return this.activity.getString(R.string.time_in_minutes_and_hours, hours, minutes);
            }
            return activity.getString(R.string.time_in_minutes_and_seconds, minutes, seconds);
        }
    }

    private class syncNotifiers extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new GuildView();
            }

            return new Fragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Guild";
                case 1:
                    return "Forums";
                case 2:
                    return "Friends";
            }
            return null;
        }
    }
}
