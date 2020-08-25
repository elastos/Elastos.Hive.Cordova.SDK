package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class UpdateOptions {
    public UpdateOptions() {
    }

    public static UpdateOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        UpdateOptions options = new UpdateOptions();
        return options;
    }
}