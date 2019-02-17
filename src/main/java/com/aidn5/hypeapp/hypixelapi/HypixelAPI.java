package com.aidn5.hypeapp.hypixelapi;

/**
 * provides a direct method to get the data from the Hypixel API
 * <p>
 * since the original source code uses high level methods
 * this alternative class made to take its place
 */
public class HypixelAPI {
	private static final String HYPIXEL_URL = "https://api.hypixel.net/";
	private static final String FRIENDS_REQUEST = HYPIXEL_URL + "friends?";
	private static final String FIND_GUILD_REQUEST = HYPIXEL_URL + "findGuild?";
	private static final String GET_GUILD = HYPIXEL_URL + "guild?";
/*
	*//**
	 * get all player's friends
	 *
	 * @param api      the Hypixel API
	 * @param userUUID user ID
	 * @return {@link HypixelReplay} with the data type of String[] (players uuid) within it
	 *//*
	public static HypixelReplay getFriends(String api, String userUUID) {
		if (Utils.isValidUUID(api))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoHypixelApi), null);
		if (Utils.isValidUUID(userUUID))
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.NoUserUUID), null);


		String json;
		try {
			json = netRequest(FRIENDS_REQUEST + "key=" + api + "&uuid=" + userUUID);
		} catch (IOException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Internet, e), null);
		}

		try {
			JSONObject jsonObject = new JSONObject(json);

			HypixelReplay error = Utils.checkForErrorsInResponse(jsonObject, json);
			if (error != null) return error;

			JSONArray friendsJson = jsonObject.getJSONArray("records");

			String[] currentFriends = new String[friendsJson.length()];
			for (int i = 0; i < friendsJson.length(); i++) {
				JSONObject friend = friendsJson.getJSONObject(i);
				if (friend.getString("uuidSender").equals(userUUID)) {//This is the user's uuid. NOT a friend...
					currentFriends[i] = friend.getString("uuidReceiver");
				} else {
					currentFriends[i] = friend.getString("uuidSender");
				}
			}

			return new HypixelReplay(currentFriends, json, -1);
		} catch (JSONException e) {
			return new HypixelReplay(new HypixelApiException(ExceptionTypes.Parse, e), json);
		}
	}*/
}
