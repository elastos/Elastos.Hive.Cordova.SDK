package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class DeleteOptions {
    public DeleteOptions() {
    }

    public static DeleteOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        DeleteOptions options = new DeleteOptions();
        return options;
    }
}