package com.aidn5.hypeapp.activities.main_activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.GuildRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;
import com.aidn5.hypeapp.hypixelapi.models.Guild;
import com.aidn5.hypeapp.hypixelapi.models.GuildMember;
import com.aidn5.hypeapp.services.IgnProvider;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class GuildFragment extends BaseFragment {
	private static final byte NO_GUILD_FOUND = 5;

	@Override
	public CharSequence getTitle(@NonNull Context context) {
		return context.getText(R.string.guild);
	}

	@Override
	protected View onCustomEvent(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		// todo: [feature] GuildFragment: add indicator no guild found
		return null;
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
		HypixelReplay friendsRequest = new GuildRequest(context).getGuildMembersByMemberUUID(settings);

		// Check on errors
		if (!friendsRequest.isSuccess) {
			setState(EVENT_FAILED);
			return;
		}

		Guild guild = (Guild) friendsRequest.value;
		if (guild == null) {
			setState(NO_GUILD_FOUND);
			return;
		}

		// Create the adapter and set it
		this.adapter = new Adapter(
				getLayoutInflater(), context, new IgnProvider(context),
				guild.getDetailedGuildMembers());

		setState(EVENT_LOADED); //Send signal to use and display the adapter
	}

	private final class Adapter extends BaseAdapter {
		private final BlockingQueue<Runnable> executesQueue = new ArrayBlockingQueue<>(15000);
		private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.SECONDS, executesQueue);
		private final Picasso imageLoader;

		private final LayoutInflater inflater;
		private final List<GuildMember> guildMembers;

		Adapter(@NonNull LayoutInflater inflater, @NonNull Context context, @NonNull IgnProvider ignProvider, @NonNull List<GuildMember> guildMembers) {
			this.inflater = inflater;
			this.imageLoader = new Picasso.Builder(context).build();
			this.guildMembers = guildMembers;

			Collections.sort(guildMembers); // Sort from guild master to officer to ... to member...

			for (GuildMember guildMember : guildMembers) {
				poolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						//IgnProvider will either returns null or the result
						//the username is NULL anyways. So, don't check the value
						guildMember.username = ignProvider.getUsername(guildMember.uuid, false);
					}
				});
			}
		}

		@Override
		public int getCount() {
			return this.guildMembers.size();
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
			GuildMember guildMember = this.guildMembers.get(position);

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

			//todo: [feature] GuildFragment: Add color to tag/ranks of guildMembers
			holder.text1.setText(
					String.format(
							"[%s] %s",
							guildMember.rank,
							(guildMember.username != null) ? guildMember.username : guildMember.uuid));

			this.imageLoader
					.load("https://crafatar.com/avatars/" + guildMember.uuid + "?overlay&default=MHF_Alex")
					.placeholder(R.drawable.default_player_head)
					.into(holder.head);

			return view;
		}

		private final class Holder {
			private ImageView head;
			private TextView text1;
			private TextView text2;
		}
	}
}