package com.aidn5.hypeapp.hypixelapi;

import android.content.Context;

import com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes;
import com.aidn5.hypeapp.hypixelapi.exception.HypixelApiException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

abstract class AbstractedRequest extends Cacher {
	protected static final String HYPIXEL_URL = "https://api.hypixel.net/";
	private final Context context;
	protected long dataFromCache = -1;

	protected AbstractedRequest(Context context) {
		super(context);

		this.context = context;
		initForNewRequest();
	}

	protected final void initForNewRequest() {
		dataFromCache = -1;
	}

	protected final void saveCacheIfNeeded(String key, String data) {
		if (dataFromCache < 0) {
			saveCache(key, data);
		}
	}

	protected final HypixelReplay checkForErrorsInResponse(JSONObject response, String fullResponse) {
		try {
			if (response.optBoolean("throttle", false))
				return new HypixelReplay(new HypixelApiException(ExceptionTypes.Throttle), fullResponse);

			if (!response.getBoolean("success")) {
				new HypixelReplay(new HypixelApiException(ExceptionTypes.API_Error), response.getString("cause"));
			}
		} catch (JSONException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Parse, e), fullResponse);
		}

		return null;
	}

	protected final String getDataFromNet(String url) throws IOException {
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

	protected final boolean isValidUUID(String uuid) {
		return uuid != null && !uuid.isEmpty();
	}

	protected final Context getContext() {
		return this.context;
	}
}
