package org.elastos.trinity.plugins.hive.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class InsertResult {
    long insertedCount = -1;
    ArrayList<String> insertedIds;

    public JSONObject toJsonObject() throws Exception {
        JSONObject obj = new JSONObject();

        obj.put("insertedCount", insertedCount);

        JSONArray ids = new JSONArray();
        for (String id : insertedIds) {
            ids.put(id);
        }
        obj.put("insertedIds", ids);

        return obj;
    }
}