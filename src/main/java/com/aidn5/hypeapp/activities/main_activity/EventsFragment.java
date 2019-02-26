package com.aidn5.hypeapp.activities.main_activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.IgnProvider;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventsFragment extends BaseFragment {
	@Override
	protected CharSequence getTitle(@NonNull Context context) {
		return context.getString(R.string.eventsTitle);
	}

	@Override
	protected void refresh() {
		setState(LOADING); // show the loading indicator

		Context context = getContext();
		if (context == null) {
			setState(FAILED);
			return;
		}

		//EventsSaver eventsSaver = new EventsSaver(context);

	}

	@Override
	protected boolean autoLoad(@Nullable Context context) {
		return true;
	}

	//todo: [feature] FriendsFragment: load the data of best-friends
	private final class Adapter extends BaseAdapter {
		private final BlockingQueue<Runnable> executesQueue = new ArrayBlockingQueue<>(15000);
		private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.SECONDS, executesQueue);
		private final Picasso imageLoader;

		private final LayoutInflater inflater;
		private final Player[] players;

		Adapter(LayoutInflater inflater, Context context, IgnProvider ignProvider, String[] uuids) {
			this.inflater = inflater;
			this.imageLoader = new Picasso.Builder(context).build();
			this.players = new Player[uuids.length];

			for (int i = 0; i < uuids.length; i++) {

				Player player = (players[i] = new Player());
				player.uuid = uuids[i];

				poolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						//IgnProvider will either returns null or the result
						//the username is NULL anyways. So, don't check the value
						player.username = ignProvider.getUsername(player.uuid, false);
					}
				});
			}
		}

		@Override
		public int getCount() {
			return this.players.length;
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
			final Holder holder;
			Player player = this.players[position];

			if (convertView == null) {
				holder = new Holder();
				convertView = this.inflater.inflate(R.layout.players_list_adatper_item, null);

				holder.head = convertView.findViewById(R.id.SPL_item_image);
				holder.text1 = convertView.findViewById(R.id.SPL_item_text1);
				holder.text2 = convertView.findViewById(R.id.SPL_item_text2);

				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.text1.setText((player.username != null) ? player.username : player.uuid);

			this.imageLoader
					.load("https://crafatar.com/avatars/" + player.uuid + "?overlay&default=MHF_Alex")
					.placeholder(R.drawable.default_player_head)
					.into(holder.head);

			return convertView;
		}

		private final class Holder {
			ImageView head;
			TextView text1;
			TextView text2;
		}

		private final class Player {
			String uuid;
			String username;

			//String doing;
			//boolean isCached;
		}
	}
}
