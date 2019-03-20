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

package net.hypixel.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.aidn5.hypeapp.services.AbstractedCacher;
import com.aidn5.hypeapp.services.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.hypixel.api.adapters.BoostersTypeAdapterFactory;
import net.hypixel.api.adapters.GameTypeTypeAdapter;
import net.hypixel.api.adapters.UUIDTypeAdapter;
import net.hypixel.api.exceptions.APIThrottleException;
import net.hypixel.api.exceptions.HypixelAPIException;
import net.hypixel.api.reply.AbstractReply;
import net.hypixel.api.reply.BoostersReply;
import net.hypixel.api.reply.FindGuildReply;
import net.hypixel.api.reply.FriendsReply;
import net.hypixel.api.reply.GameCountsReply;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.KeyReply;
import net.hypixel.api.reply.LeaderboardsReply;
import net.hypixel.api.reply.PlayerCountReply;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.SessionReply;
import net.hypixel.api.reply.WatchdogStatsReply;
import net.hypixel.api.util.GameType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"deprecation", "WeakerAccess", "unused"})
public class HypixelAPI {
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
			.registerTypeAdapter(GameType.class, new GameTypeTypeAdapter())

			.registerTypeAdapterFactory(new BoostersTypeAdapterFactory<>(BoostersReply.Booster.class))

			.create();

	private static final String BASE_URL = "https://api.hypixel.net/";
	/**
	 * We can't create new Instance every time we want to
	 * #Cacher uses {@link android.database.sqlite.SQLiteDatabase}
	 * There would be memory-leak if we keep creating new instances
	 */
	private static Cacher cacher;

	private String apiKey;
	private long timeCache = TimeUnit.MINUTES.toMillis(10);

	public HypixelAPI(Context context, SharedPreferences sp) {
		this(context, sp.getString(Settings.hypixelAPI.name(), null));
	}

	public HypixelAPI(Context context, String apiKey) {
		if (cacher == null) cacher = new Cacher(context);
		this.apiKey = apiKey;
	}

	public HypixelAPI setTimeCache(long timeCache, TimeUnit timeUnit) {
		this.timeCache = timeUnit.toMillis(timeCache);
		return this;
	}

	public BoostersReply getBoosters() {
		return get(BoostersReply.class, "boosters");
	}

	public LeaderboardsReply getLeaderboards() {
		return get(LeaderboardsReply.class, "leaderboards");
	}

	public WatchdogStatsReply getWatchdogStats() {
		return get(WatchdogStatsReply.class, "watchdogStats");
	}

	/**
	 * This is now included inside {@link #getGameCounts()}
	 */
	@Deprecated
	public PlayerCountReply getPlayerCount() {
		return get(PlayerCountReply.class, "playerCount");
	}

	/**
	 * Session endpoint is bound to be removed at some point,
	 * data is mainly internal and highly inaccurate for online checking
	 */
	@Deprecated
	public SessionReply getSessionByUuid(String player) {
		return get(SessionReply.class, "session", "uuid", player);
	}

	public PlayerReply getPlayerByUuid(String player) {
		return get(PlayerReply.class, "player", "uuid", player);
	}

	@Deprecated
	public PlayerReply getPlayerByName(String player) {
		return get(PlayerReply.class, "player", "name", player);
	}

	public FriendsReply getFriends(SharedPreferences sp) {
		return getFriends(sp.getString(Settings.userUUID.name(), null));
	}

	public FriendsReply getFriends(String player) {
		return get(FriendsReply.class, "friends", "uuid", player);
	}

	public GuildReply getGuildByPlayer(SharedPreferences sp) {
		return getGuildByPlayer(sp.getString(Settings.userUUID.name(), null));
	}

	public GuildReply getGuildByPlayer(String player) {
		return get(GuildReply.class, "guild", "player", player);
	}

	public GuildReply getGuildByName(String name) {
		return get(GuildReply.class, "guild", "name", name);
	}

	/**
	 * @param id mongo id hex string
	 * @return the future
	 */
	public GuildReply getGuildById(String id) {
		return get(GuildReply.class, "guild", "id", id);
	}

	/**
	 * You can directly get the guild using {@link #getGuildByPlayer(String)}
	 */
	@Deprecated
	public FindGuildReply findGuildByPlayer(String player) {
		return get(FindGuildReply.class, "findGuild", "byUuid", player);
	}

	/**
	 * You can directly get the guild using {@link #getGuildByName(String)})}
	 */
	@Deprecated
	public GuildReply findGuildByName(String name) {
		return get(GuildReply.class, "findGuild", "byName", name);
	}

	public KeyReply getKey() {
		return get(KeyReply.class, "key");
	}

	public GameCountsReply getGameCounts() {
		return get(GameCountsReply.class, "gameCounts");
	}

	/**
	 * Create the url, request the data from the url
	 * and convert it (json string) to Object
	 *
	 * @param request Request to get
	 */
	private <R extends AbstractReply> R get(Class<R> clazz, String request, Object... params) {

		if (params.length % 2 != 0)
			throw new IllegalArgumentException("Need both key and value for parameters");

		String content = null;
		R response = null;
		try {
			AbstractedCacher.CachedData cachedData = cacher.get(clazz, params);
			if (cachedData.getTime() < System.currentTimeMillis() - timeCache)
				throw new RuntimeException("data are old");

			response = GSON.fromJson(cachedData.getValue(), clazz);

			checkReply(response);
			response.timeCache = cachedData.getTime();

			return response;
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				StringBuilder url = new StringBuilder(BASE_URL);

				url.append(request);
				url.append("?key=").append(apiKey);

				for (int i = 0; i < params.length - 1; i += 2) {
					url.append("&").append(params[i]).append("=").append(params[i + 1]);
				}

				content = httpRequest(url.toString());
				response = GSON.fromJson(content, clazz);

				checkReply(response);
				cacher.save(clazz, content, params);
				return response;

			} catch (IOException e2) {
				e2.printStackTrace();
				throw new RuntimeException(e2);
			}
		}
	}

	/**
	 * Checks reply and throws appropriate exceptions based on it's content
	 *
	 * @param reply The reply to check
	 * @param <T>   The class of the reply
	 */
	private <T extends AbstractReply> void checkReply(T reply) {
		if (reply != null) {
			if (reply.isThrottle()) {
				throw new APIThrottleException();
			} else if (!reply.isSuccess()) {
				throw new HypixelAPIException(reply.getCause());
			}
		}
	}

	/**
	 * open the url and retrieve the data
	 */
	private String httpRequest(String url) throws IOException {
		URL URL = new URL(url);

		URLConnection urlConnection = URL.openConnection();
		InputStream inputStream = urlConnection.getInputStream();

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}

		inputStream.close();
		return result.toString("UTF-8");

	}
}
