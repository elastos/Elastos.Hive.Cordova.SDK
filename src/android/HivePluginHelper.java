package org.elastos.trinity.plugins.hive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elastos.hive.file.FileInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class HivePluginHelper {
    public static JSONObject hiveFileInfoToPluginJson(FileInfo fileInfo) throws JSONException {
        JSONObject ret = new JSONObject();

        ret.put("name", fileInfo.getName());
        ret.put("size", fileInfo.getSize());
        ret.put("lastModified", fileInfo.getLastModified());

        switch (fileInfo.getType()) {
            case FOLDER:
                ret.put("type", 1);
                break;
            default:
                // File
                ret.put("type", 0);
        }
        return ret;
    }

    public static JsonNode jsonObjectToJsonNode(JSONObject jsonObject) {
        try {
            return new ObjectMapper().readTree(jsonObject.toString());
        }
        catch (Exception e) {
            return null;
        }
    }
}
