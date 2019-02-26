package com.aidn5.hypeapp.activities.main_activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.aidn5.hypeapp.R;

import java.util.Objects;

abstract class BaseFragment extends Fragment {
	protected final static byte PENDING = 1;
	protected final static byte LOADING = 2;
	protected final static byte LOADED = 3;
	protected final static byte FAILED = 4;

	protected BaseAdapter adapter;
	private byte action = PENDING;

	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (autoLoad(getContext())) {
			new Thread(this::refresh).start();
			return null;
		}

		switch (action) {
			case PENDING:
				return onTapToLoadEvent(inflater, container);
			case LOADING:
				return onLoadingEvent(inflater, container);
			case LOADED:
				return onLoadedEvent(inflater, container);
			case FAILED:
				return null;
			default:
				return onCustomEvent(inflater, container);
		}
	}

	protected final synchronized void setState(byte action) {
		this.action = action;

		if (!this.isDetached()) {
			Objects.requireNonNull(getFragmentManager()).beginTransaction()
					.detach(this)
					.attach(this)
					.commit();
		}
	}

	protected View onTapToLoadEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = inflater.inflate(R.layout.main_player_list_tap_to_load, container, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(BaseFragment.this::refresh).start();
			}
		});
		return view;
	}

	protected View onLoadingEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return inflater.inflate(R.layout.loading_progress, container, false);
	}

	protected View onLoadedEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = inflater.inflate(R.layout.main_player_list, container, false);

		ListView listView = view.findViewById(R.id.main_playerList_list_listView);
		listView.setAdapter(adapter);

		return view;
	}

	protected View onCustomEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		//Override me!
		return null;
	}

	@Nullable
	@Override
	public final Context getContext() {
		return super.getContext() != null ? super.getContext() : super.getActivity();
	}

	protected boolean autoLoad(@Nullable Context context) {
		if (context == null) return false;
		return false;
	}

	protected abstract CharSequence getTitle(@NonNull Context context);

	protected abstract void refresh();
}
