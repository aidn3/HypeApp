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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.Settings;

import java.util.Objects;

/**
 * ListView adapter and helper uses thread to fetch the data and provide a variable to set the ListViewAdapter on it
 * <p>
 * It's not possible to change the view from another thread.
 * It's only possible with recreating or reassigning
 * the fragment to the {@link android.support.v4.app.FragmentManager}.
 * This Class helps to change the view and to interact with the GUI easily by a series of events.
 * <p>
 * On requesting the view on {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
 * a thread will be fired and run {@link #refresh()}
 * from {@link #refresh()}
 * <p>
 * it's possible to interact with the adapter
 * by calling {@link #setState(byte)} and using either {@link #EVENT_PENDING}, {@link #EVENT_LOADING}, {@link #EVENT_LOADED},...
 * to run their event {@link #onLoadingEvent(LayoutInflater, ViewGroup)}, {@link #onLoadedEvent(LayoutInflater, ViewGroup)},...
 * or using custom events {@link #onCustomEvent(LayoutInflater, ViewGroup)}
 */
@SuppressWarnings({"WeakerAccess", "unused", "SameReturnValue"})
abstract class BaseFragment extends Fragment {
	public final static byte EVENT_PENDING = 1;
	public final static byte EVENT_LOADING = 2;
	public final static byte EVENT_LOADED = 3;
	public final static byte EVENT_FAILED = 4;

	protected BaseAdapter adapter;
	private byte action = EVENT_PENDING;

	/**
	 * Switcher to choose which event to run on the <b>main-thread</b>
	 */
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (action == EVENT_PENDING && autoLoad(getContext())) {
			action = EVENT_LOADING;
			new Thread(this::refresh).start();
		}

		switch (action) {
			case EVENT_PENDING:
				return onTapToLoadEvent(inflater, container);
			case EVENT_LOADING:
				return onLoadingEvent(inflater, container);
			case EVENT_LOADED:
				return onLoadedEvent(inflater, container);
			case EVENT_FAILED:
				return null;
			default:
				return onCustomEvent(inflater, container);
		}
	}

	/**
	 * Set state to run an event either custom event or inherited event
	 *
	 * @param action the action to the event to run e.g. {@link #EVENT_LOADING}
	 */
	protected final synchronized void setState(byte action) {
		this.action = action;

		if (!this.isDetached()) {
			Objects.requireNonNull(getFragmentManager()).beginTransaction()
					.detach(this)
					.attach(this)
					.commit();
		}
	}

	/**
	 * Show view on the very start to let the user choose
	 * whether to run {@link #refresh()} and whether to make this default.
	 * <p>
	 * get triggered by {@link #EVENT_PENDING}
	 */
	protected View onTapToLoadEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = inflater.inflate(R.layout.main_player_list_tap_to_load, container, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(BaseFragment.this::refresh).start();
			}
		});

		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				setAutoLoad();

				Context context = getContext();
				if (context != null) {
					Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
					vibe.vibrate(100);
				}

				setState(EVENT_PENDING);
				return false;
			}
		});
		return view;
	}

	/**
	 * show Loading progress to the user to let them know something IS going on
	 * <p>
	 * get triggered by {@link #EVENT_LOADING}
	 */
	protected View onLoadingEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return inflater.inflate(R.layout.loading_progress, container, false);
	}

	/**
	 * Create and show the main GUI with the {@link ListView}
	 * and set the {@link #adapter} with {@link ListView#setAdapter(ListAdapter)}
	 * <p>
	 * get triggered by {@link #EVENT_LOADED}
	 */
	protected View onLoadedEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = inflater.inflate(R.layout.main_player_list, container, false);

		FrameLayout frame = view.findViewById(R.id.main_playerList_frame);

		ListView listView = view.findViewById(R.id.main_playerList_list_listView);
		listView.setAdapter(adapter);

		onListViewLoad(listView, frame); //Fire up the event

		return view;
	}

	/**
	 * This will get triggered when using {@link #setState(byte)} with undefined event
	 * <p>
	 * Override it and use it
	 */
	protected View onCustomEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		//Override me!
		return null;
	}

	/**
	 * Get triggered after {@link #onLoadedEvent(LayoutInflater, ViewGroup)}
	 * <p>
	 * provides {@link ListView} and {@link FrameLayout} from the main GUI
	 * to helps changing their properties
	 *
	 * <b>Override it!</b>
	 */
	protected void onListViewLoad(@NonNull ListView listView, @NonNull FrameLayout frameLayout) {
		// Override me!
	}

	/**
	 * Check whether this page is the default one
	 * and must run without waiting for the user to interact with it
	 *
	 * @return whether it must run or not
	 */
	protected boolean autoLoad(@Nullable Context context) {
		if (context == null) return false;

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String className = sp.getString(Settings.defaultEventView.name(), null);

		if (className == null || className.isEmpty()) return false;

		return (className.equals(getClass().getName()));
	}

	@Nullable
	@Override
	public final Context getContext() {
		return super.getContext() != null ? super.getContext() : super.getActivity();
	}

	public final byte getStatus() {
		return this.action;
	}

	/**
	 * Set this fragment as the default one
	 * to load without waiting for the user to interact with it
	 */
	protected final void setAutoLoad() {
		Context context = getContext();
		if (context == null) return;

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(Settings.defaultEventView.name(), getClass().getName());
		editor.apply();
	}

	/**
	 * When viewing the fragments on the gui.
	 * A title is needed for the {@link android.support.design.widget.TabLayout} to address the page
	 *
	 * @return The title for the fragment
	 */
	protected abstract CharSequence getTitle(@NonNull Context context);

	/**
	 * Use this to load the data and fetch them
	 */
	protected abstract void refresh();
}
