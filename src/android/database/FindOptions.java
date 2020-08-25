package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class FindOptions {
    long limit = -1;
    long skip = -1;
    JSONObject sort = null;
    JSONObject projection = null;

    public FindOptions() {
    }

    public static FindOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        FindOptions options = new FindOptions();

        if (jsonObject.has("limit"))
            options.limit = jsonObject.getLong("limit");

        if (jsonObject.has("skip"))
            options.skip = jsonObject.getLong("skip");

        if (jsonObject.has("sort"))
            options.sort = jsonObject.getJSONObject("sort");

        if (jsonObject.has("projection"))
            options.projection = jsonObject.getJSONObject("projection");

        return options;
    }
}