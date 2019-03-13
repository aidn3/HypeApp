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
	protected long dataFromCache = -1;

	protected AbstractedRequest(Context context) {
		super(context);

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

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected final boolean isValidUUID(String uuid) {
		return uuid != null && !uuid.isEmpty();
	}
}
