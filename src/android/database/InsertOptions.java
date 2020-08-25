package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class InsertOptions {
    public InsertOptions() {
    }

    public static InsertOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        InsertOptions options = new InsertOptions();
        return options;
    }
}