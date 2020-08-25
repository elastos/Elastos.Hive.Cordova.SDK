package org.elastos.trinity.plugins.hive.database;

import org.json.JSONObject;

public class DeleteResult {
    long updatedCount = -1;

    public JSONObject toJsonObject() throws Exception {
        JSONObject obj = new JSONObject();

        obj.put("updatedCount", updatedCount);

        return obj;
    }
}