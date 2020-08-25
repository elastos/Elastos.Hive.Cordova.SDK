package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class CountOptions {
    long limit = -1;
    long skip = -1;

    public CountOptions() {
    }

    public static CountOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        CountOptions options = new CountOptions();

        if (jsonObject.has("limit"))
            options.limit = jsonObject.getLong("limit");

        if (jsonObject.has("skip"))
            options.skip = jsonObject.getLong("skip");

        return options;
    }
}