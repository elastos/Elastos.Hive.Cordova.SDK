package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class CreateCollectionOptions {
    public CreateCollectionOptions() {
    }

    public static CreateCollectionOptions fromJsonObject(JSONObject jsonObject) throws Exception {
        CreateCollectionOptions options = new CreateCollectionOptions();
        return options;
    }
}