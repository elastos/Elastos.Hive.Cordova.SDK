package org.elastos.trinity.plugins.hive.database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdateResult {
    long updatedCount = -1;

    public JSONObject toJsonObject() throws Exception {
        JSONObject obj = new JSONObject();

        obj.put("updatedCount", updatedCount);

        return obj;
    }
}