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

import com.aidn5.hypeapp.R;

import java.util.Objects;

abstract class BaseFragment extends Fragment {
	private final static byte TAP_TO_LOAD = 1;
	private final static byte LOADING = 2;
	private final static byte LOADED = 3;

	protected BaseAdapter listAdapter;

	private byte action = 0;

	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (autoLoad(getContext())) {
			refresh();
			return null;
		}

		if (action == TAP_TO_LOAD)
			return getTapToLoadView(inflater, container);
		else if (action == LOADING)
			return getLoadingView(inflater, container);

		return getTapToLoadView(inflater, container);
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

	private View getTapToLoadView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = inflater.inflate(R.layout.main_player_list_tap_to_load, container, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		return view;
	}

	private View getLoadingView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return inflater.inflate(R.layout.loading_progress, container, false);
	}

	protected abstract CharSequence getTitle(@NonNull Context context);

	protected abstract void refresh();

	@Nullable
	@Override
	public final Context getContext() {
		return super.getContext() != null ? super.getContext() : super.getActivity();
	}

	protected boolean autoLoad(@NonNull Context context) {
		return false;
	}
}
