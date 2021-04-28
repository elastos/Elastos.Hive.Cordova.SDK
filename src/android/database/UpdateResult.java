package org.elastos.plugins.hive.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdateResult {
    long matchedCount;
    long modifiedCount;
    long upsertedCount;
    String upsertedId;

    public JSONObject toJsonObject() throws Exception {
        JSONObject obj = new JSONObject();

        obj.put("matchedCount", matchedCount);
        obj.put("modifiedCount", modifiedCount);
        obj.put("upsertedCount", upsertedCount);
        obj.put("upsertedId", upsertedId);

        return obj;
    }
}