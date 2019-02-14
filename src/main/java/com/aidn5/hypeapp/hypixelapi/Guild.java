package com.aidn5.hypeapp.hypixelapi;

import org.json.JSONArray;
import org.json.JSONObject;

public class Guild {
    public final String guildId;
    public final String guildName;
    public final String guildTag;
    public final String guildDescription;

    private final JSONObject jsonObject;

    Guild(JSONObject json) {
        this.jsonObject = json.optJSONObject("guild");

        this.guildId = this.jsonObject.optString("_id");
        this.guildName = this.jsonObject.optString("name");
        this.guildTag = this.jsonObject.optString("tag");
        this.guildDescription = this.jsonObject.optString("description");
    }

    private void loadPlayers() throws Exception {
        JSONArray array = this.jsonObject.optJSONArray("members");
    }

    class Player implements Comparable {
        String uuid;
        String rank;
        int rankPriority;
        int joinedAt;

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }
}
