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
import com.aidn5.hypeapp.services.IgnProvider;
import com.squareup.picasso.Picasso;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.GuildReply;

import java.util.Collections;
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

		try {
			// Load data
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			GuildReply.Guild guild = new HypixelAPI(context, settings)
					.getGuildByPlayer(settings)
					.getGuild();

			if (guild == null) {
				setState(NO_GUILD_FOUND);
				return;
			}

			// Create the adapter and set it
			this.adapter = new Adapter(
					getLayoutInflater(), context, new IgnProvider(context),
					guild);

			setState(EVENT_LOADED); //Send signal to use and display the adapter
		} catch (Exception e) {
			setState(EVENT_FAILED);
		}


	}

	private final class Adapter extends BaseAdapter {
		private final BlockingQueue<Runnable> executesQueue = new ArrayBlockingQueue<>(15000);
		private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.SECONDS, executesQueue);
		private final Picasso imageLoader;

		private final LayoutInflater inflater;
		private final GuildReply.Guild guild;

		Adapter(@NonNull LayoutInflater inflater, @NonNull Context context, @NonNull IgnProvider ignProvider, @NonNull GuildReply.Guild guild) {
			this.inflater = inflater;
			this.imageLoader = new Picasso.Builder(context).build();
			this.guild = guild;

			Collections.sort(guild.getMembers()); // Sort from guild master to officer to ... to member...

			for (GuildReply.Guild.Member guildMember : guild.getMembers()) {
				poolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						//IgnProvider will either returns null or the result
						//the username is NULL anyways. So, don't check the value
						guildMember.username = ignProvider.getUsername(guildMember.getUuid(), false);
					}
				});
			}
		}

		@Override
		public int getCount() {
			return this.guild.getMembers().size();
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
			GuildReply.Guild.Member guildMember = this.guild.getMembers().get(position);

			if (view == null) {
				holder = new Holder();
				view = this.inflater.inflate(R.layout.players_list_adatper_item, null);

				holder.head = view.findViewById(R.id.SPL_item_image);
				holder.text1 = view.findViewById(R.id.SPL_item_text1);
				holder.text2 = view.findViewById(R.id.SPL_item_text2);
				holder.tag = view.findViewById(R.id.SPL_item_tag);

				view.setTag(holder);
			} else {
				holder = (Holder) view.getTag();
			}

			holder.text1.setText(
					String.format(
							"[%s] %s",
							guildMember.getRank().getName(),
							(guildMember.username != null) ? guildMember.username : guildMember.getUuid()));

			if (guildMember.getRank().getTag() != null && !guildMember.getRank().getTag().isEmpty()) {
				holder.tag.setVisibility(View.VISIBLE);

				holder.tag.setText(guildMember.getRank().getTag());
				holder.tag.setTextColor(ChatColors.GOLD.getSecondaryColor());
				holder.tag.setBackgroundColor(ChatColors.GOLD.getPrimaryColor());
			} else {
				holder.tag.setVisibility(View.GONE);
			}


			this.imageLoader
					.load("https://crafatar.com/avatars/" + guildMember.getUuid() + "?overlay&default=MHF_Alex")
					.placeholder(R.drawable.default_player_head)
					.into(holder.head);

			return view;
		}

		private final class Holder {
			private ImageView head;
			private TextView tag;
			private TextView text1;
			private TextView text2;
		}
	}
}