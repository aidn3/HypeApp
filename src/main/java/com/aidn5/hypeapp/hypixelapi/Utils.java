package com.aidn5.hypeapp.hypixelapi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

final class Utils {
    static String netRequest(String url) throws IOException {
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

    static boolean isValidUUID(String uuid) {
        return uuid != null && !uuid.isEmpty();
    }

    static HypixelReplay checkForErrorsInResponse(JSONObject response, String fullResponse) {
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

}
