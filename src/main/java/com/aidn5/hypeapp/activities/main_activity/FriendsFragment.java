package com.aidn5.hypeapp.activities.main_activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.hypixelapi.FriendsRequest;
import com.aidn5.hypeapp.hypixelapi.HypixelReplay;

//TODO: [code] Fix FriendsFragment
public class FriendsFragment extends BaseFragment {
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public CharSequence getTitle(@NonNull Context context) {
		return context.getText(R.string.friends);
	}

	@Override
	public synchronized void refresh() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				HypixelReplay friendsRequest = new FriendsRequest(
						getContext()
				).getFriendsByUserUUID(
						//G.getSettings(getContext())
						"asd", "asd"
				);

				if (!friendsRequest.isSuccess)
					throw new RuntimeException(friendsRequest.exception);


				ListView listView = getView().findViewById(R.id.main_playerList_list_listView);

				String[] friends = (String[]) friendsRequest.value;
				Log.v("MEEE", friends[0]);
			}
		}).start();
	}

	private static class loadFriends extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			return null;
		}
	}
}
