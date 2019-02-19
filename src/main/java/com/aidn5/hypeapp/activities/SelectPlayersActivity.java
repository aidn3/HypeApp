package com.aidn5.hypeapp.activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aidn5.hypeapp.G;
import com.aidn5.hypeapp.R;
import com.aidn5.hypeapp.services.IgnProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SelectPlayersActivity extends BaseActivity {
	public static final String PLAYERS_UUIDs = "playersUuid";
	public static final String SELECTED_PLAYERS = "selectedPlayers";
	//// TODO: add feature Max Players to SelectPlayersActivity
	public static final String MAX_PLAYERS = "maxPlayers";
	public static final String TITLE = "title";

	private final BlockingQueue<Runnable> executesQueue = new ArrayBlockingQueue<>(15000);
	private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.MINUTES, executesQueue);

	private final ArrayList<Player> playersList = new ArrayList<>();
	private Picasso imageLoader;
	private IgnProvider ignProvider;
	private long maxPlayers = Long.MAX_VALUE;

	private playersSelectAdapter adapter1;
	/**
	 * on the list changed by clicking on one of its checkboxes
	 */
	private boolean isListChanged = false;

	//Start servicesProvider binding
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ignProvider = G.getIgnProvider(this);

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(TITLE));
		maxPlayers = intent.getLongExtra(MAX_PLAYERS, maxPlayers);
		String[] UUIDs = intent.getStringArrayExtra(PLAYERS_UUIDs);

		for (final String uuid : UUIDs) {
			final Player player = new Player();
			playersList.add(player);

			player.uuid = uuid;

			poolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					//IgnProvider will either returns null or the result
					//the username is NULL anyways. So, don't check the value
					player.username = ignProvider.getUsername(uuid, false);
				}
			});
		}

		initialize();
	}

	@Override
	public void onBackPressed() {
		if (!isListChanged) {
			SelectPlayersActivity.this.setResult(RESULT_CANCELED);
			SelectPlayersActivity.this.finish();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.select_players_save_message));
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				ArrayList<String> UUIDs = new ArrayList<>();
				for (Player player : playersList) {
					if (player.isChecked) UUIDs.add(player.uuid);
				}
				SelectPlayersActivity.this.setResult(RESULT_OK, new Intent().putExtra(SELECTED_PLAYERS, UUIDs.toArray(new String[0])));
				SelectPlayersActivity.this.finish();
			}
		});
		builder.setNeutralButton(android.R.string.cancel, null);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				SelectPlayersActivity.this.setResult(RESULT_CANCELED);
				SelectPlayersActivity.this.finish();
			}
		});
		builder.create().show();
	}

	//execute when when playerList is initialized
	private void initialize() {
		setContentView(R.layout.select_players_rootview);

		EditText etSearch = findViewById(R.id.bflP_search);
		ListView lvPlayers = findViewById(R.id.bflP_List);

		this.imageLoader = new Picasso.Builder(this).build();

		adapter1 = new playersSelectAdapter(this, playersList);
		lvPlayers.setAdapter(adapter1);

		// Add Text Change Listener to EditText
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Call back the Adapter with current character to Filter
				adapter1.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// We don't need this method. We only need #onTextChanged
			}

			@Override
			public void afterTextChanged(Editable s) {
				// We don't need this method. We only need #onTextChanged
			}
		});
	}

	//View playerList after been initialized
	private class playersSelectAdapter extends BaseAdapter implements Filterable {

		private final LayoutInflater inflater;
		private final ArrayList<Player> mOriginalValues; // Original Values
		private ArrayList<Player> mDisplayedValues;    // Values to be displayed

		private playersSelectAdapter(Context context, ArrayList<Player> mProductArrayList) {
			this.mOriginalValues = mProductArrayList;
			this.mDisplayedValues = mProductArrayList;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mDisplayedValues.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View finalView = convertView;
			final ViewHolder holder;
			final Player player = mDisplayedValues.get(position);

			if (finalView == null) {
				holder = new ViewHolder();

				finalView = inflater.inflate(R.layout.select_players_list_item, null);

				holder.dataHolder = finalView.findViewById(R.id.bflP_item_1);
				holder.imageView = finalView.findViewById(R.id.bflP_item_image);
				holder.text1 = finalView.findViewById(R.id.bflP_item_text1);
				holder.text2 = finalView.findViewById(R.id.bflP_item_text2);
				holder.selected = finalView.findViewById(R.id.bflP_item_checkbox);

				finalView.setTag(holder);
			} else {
				holder = (ViewHolder) finalView.getTag();
			}

			holder.dataHolder.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					holder.selected.setChecked(!holder.selected.isChecked());
				}
			});
			holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					isListChanged = true;
					player.isChecked = b;
				}
			});

			holder.selected.setChecked(player.isChecked);

			if (player.username != null) {
				holder.text1.setText(player.username);
				holder.text2.setText(player.uuid);
			} else {
				holder.text1.setText(player.uuid);
				holder.text2.setText(null);
			}

			imageLoader
					.load("https://crafatar.com/avatars/" + player.uuid + "?overlay&default=MHF_Alex")
					.placeholder(R.drawable.default_player_head)
					.into(holder.imageView);

			return finalView;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {

					mDisplayedValues = (ArrayList<Player>) results.values; // has the filtered values
					notifyDataSetChanged();  // notifies the data with new filtered values
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults(); // Holds the results of a filtering operation in values
					ArrayList<Player> FilteredArrList = new ArrayList<>();


					// If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
					// else does the Filtering and returns FilteredArrList(Filtered)
					if (constraint == null || constraint.length() == 0) {

						// set the Original result to return
						results.count = mOriginalValues.size();
						results.values = mOriginalValues;
					} else {
						String searchedPlayer = constraint.toString().toLowerCase();
						for (int i = 0; i < mOriginalValues.size(); i++) {
							Player player = mOriginalValues.get(i);
							if (player.username != null) {
								if (player.username.toLowerCase().startsWith(searchedPlayer)) {
									FilteredArrList.add(player);
								}
							} else {
								if (player.uuid.toLowerCase().startsWith(searchedPlayer)) {
									FilteredArrList.add(player);
								}
							}
						}
						// set the Filtered result to return
						results.count = FilteredArrList.size();
						results.values = FilteredArrList;
					}
					return results;
				}
			};
		}

		private class ViewHolder {
			private CheckBox selected;
			private View dataHolder;
			private ImageView imageView;
			private TextView text1;
			private TextView text2;
		}
	}

	private class Player {
		private Bitmap headPlayer;

		private boolean isChecked;
		private String username;
		private String uuid;
	}
}
