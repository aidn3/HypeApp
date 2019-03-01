package com.aidn5.hypeapp.activities.main_activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.services.IgnProvider;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class FriendsFragment extends BaseFragment {

	@Override
	public CharSequence getTitle(@NonNull Context context) {
		return context.getText(R.string.friends);
	}

	@Override
	public synchronized void refresh() {
		setState(EVENT_LOADING); // show the loading indicator

		Context context = getContext();
		if (context == null) {
			setState(EVENT_FAILED);
			return;
		}

		// Load data
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		HypixelReplay friendsRequest = new FriendsRequest(context).getFriendsByUserUUID(settings);

		String[] friends = (String[]) friendsRequest.value;

		// Check on errors
		if (!friendsRequest.isSuccess || friends == null) {
			setState(EVENT_FAILED);
			return;
		}

		// Create the adapter and set it
		this.adapter = new Adapter(getLayoutInflater(), context, new IgnProvider(context), friends);

		setState(EVENT_LOADED); //Send signal to use and display the adapter
	}

	//todo: [feature] FriendsFragment: load the data of best-friends
	private final class Adapter extends BaseAdapter {
		private final BlockingQueue<Runnable> executesQueue = new ArrayBlockingQueue<>(15000);
		private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.SECONDS, executesQueue);
		private final Picasso imageLoader;

		private final LayoutInflater inflater;
		private final Player[] players;

		Adapter(@NonNull LayoutInflater inflater, @NonNull Context context, @NonNull IgnProvider ignProvider, @NonNull String[] UUIDs) {
			this.inflater = inflater;
			this.imageLoader = new Picasso.Builder(context).build();
			this.players = new Player[UUIDs.length];

			for (int i = 0; i < UUIDs.length; i++) {

				Player player = (players[i] = new Player());
				player.uuid = UUIDs[i];

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
			View view = convertView;
			final Holder holder;
			Player player = this.players[position];

			if (view == null) {
				holder = new Holder();
				view = this.inflater.inflate(R.layout.players_list_adatper_item, null);

				holder.head = view.findViewById(R.id.SPL_item_image);
				holder.text1 = view.findViewById(R.id.SPL_item_text1);
				holder.text2 = view.findViewById(R.id.SPL_item_text2);

				view.setTag(holder);
			} else {
				holder = (Holder) view.getTag();
			}

			holder.text1.setText((player.username != null) ? player.username : player.uuid);

			this.imageLoader
					.load("https://crafatar.com/avatars/" + player.uuid + "?overlay&default=MHF_Alex")
					.placeholder(R.drawable.default_player_head)
					.into(holder.head);

			return view;
		}

		private final class Holder {
			private ImageView head;
			private TextView text1;
			private TextView text2;
		}

		private final class Player {
			private String uuid;
			private String username;

			//String doing;
			//boolean isCached;
		}
	}
}
