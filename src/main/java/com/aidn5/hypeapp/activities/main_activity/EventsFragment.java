/*
 * MIT License
 *
 * Copyright (c) [2019] [aidn5]
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.Utils;
import com.aidn5.hypeapp.services.EventsSaver;

import java.util.List;

public final class EventsFragment extends BaseFragment {

	@Override
	protected CharSequence getTitle(@NonNull Context context) {
		return context.getString(R.string.eventsTitle);
	}

	@Override
	protected void refresh() {
		setState(EVENT_LOADING); // show the loading indicator

		Context context = getContext();
		if (context == null) {
			setState(EVENT_FAILED);
			return;
		}

		// open the database DIRECTLY!! (NOT THREAD-SAFE) and prepare it to fetch the data
		EventsSaver eventsSaver = new EventsSaver(context);
		eventsSaver.cleanOldRows(); // Clean the "mess" before requesting the data :)

		// fetch the data
		List<EventsSaver.DataHolder> dataHolders = eventsSaver.fetch(1);
		Log.v(getClass().getSimpleName(), "entries size: " + dataHolders.size());

		// Create the adapter, set it and send signal to use it
		this.adapter = new Adapter(getLayoutInflater(), context, dataHolders);
		setState(EVENT_LOADED);
	}

	@Override
	protected boolean autoLoad(@Nullable Context context) {
		return true; // Events must always show for the best user experience
	}

	@Override
	protected void onListViewLoad(@NonNull ListView listView, @NonNull FrameLayout frameLayout) {
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.setSelector(android.R.drawable.screen_background_light_transparent);
	}

	private final class Adapter extends BaseAdapter {
		private final LayoutInflater inflater;
		private final Context context;
		private final List<EventsSaver.DataHolder> dataHolders;

		Adapter(@NonNull LayoutInflater inflater, @NonNull Context context, @NonNull List<EventsSaver.DataHolder> dataHolders) {
			this.inflater = inflater;
			this.context = context;
			this.dataHolders = dataHolders;
		}

		@Override
		public int getCount() {
			return this.dataHolders.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Holder holder;
			EventsSaver.DataHolder dataHolder = this.dataHolders.get(position);

			if (view == null) {
				holder = new Holder();
				view = this.inflater.inflate(R.layout.event_item, null);

				holder.text1 = view.findViewById(R.id.event_item_text1);
				holder.text2 = view.findViewById(R.id.event_item_text2);
				holder.time = view.findViewById(R.id.event_item_time);

				view.setTag(holder);
			} else {
				holder = (Holder) view.getTag();
			}

			holder.text1.setText(this.context.getString(dataHolder.title));
			holder.text2.setText(Html.fromHtml(this.context.getString(dataHolder.message, (Object[]) dataHolder.args)));
			holder.time.setText(Utils.createDateOrMessage(this.context, (int) (System.currentTimeMillis() / 1000L) - dataHolder.registerTime));

			return view;
		}

		private final class Holder {
			private TextView text1;
			private TextView text2;
			private TextView time;
		}
	}
}
