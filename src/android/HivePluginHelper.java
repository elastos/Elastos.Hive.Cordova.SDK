package org.elastos.essentials.plugins.hive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elastos.hive.database.DeleteOptions;
import org.elastos.hive.database.FindOptions;
import org.elastos.hive.database.Index;
import org.elastos.hive.database.UpdateOptions;
import org.elastos.hive.files.FileInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HivePluginHelper {
    /**
     * Creates a FileInfo TS JSONObject from native FileInfo
     */
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

    /**
     * Converts TS sort field (ex: FindOptions) to native sort index array
     */
    public static Index[] jsonSortFieldsToNative(JSONObject jsonSort) throws JSONException {
        Index[] indexes = new Index[jsonSort.length()];

        int i = 0;
        Iterator<String> it = jsonSort.keys();
        while (it.hasNext()) {
            String fieldName = it.next();
            Integer order = jsonSort.getInt(fieldName);

            if (order == 1)
                indexes[i] = new Index(fieldName, Index.Order.ASCENDING);
            else if (order == -1)
                indexes[i] = new Index(fieldName, Index.Order.DESCENDING);

            i++;
        }
        return indexes;
    }

    /**
     * Converts TS FindOptions type to native FindOptions.
     */
    public static FindOptions jsonFindOptionsToNative(JSONObject optionsJson) {
        if (optionsJson == null)
            return new FindOptions();

        try {
            FindOptions options = new FindOptions();

            if (optionsJson.has("limit"))
                options.limit(optionsJson.getLong("limit"));

            if (optionsJson.has("skip"))
                options.skip(optionsJson.getLong("skip"));

            if (optionsJson.has("sort"))
                options.sort(HivePluginHelper.jsonSortFieldsToNative(optionsJson.getJSONObject("sort")));

            if (optionsJson.has("projection"))
                options.projection(HivePluginHelper.jsonObjectToJsonNode(optionsJson.getJSONObject("projection")));

            return options;
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
            return new FindOptions();
        }
    }

    /**
     * Converts TS UpdateOptions type to native UpdateOptions.
     */
    public static UpdateOptions jsonUpdateOptionsToNative(JSONObject optionsJson) {
        if (optionsJson == null)
            return new UpdateOptions();

        try {
            UpdateOptions options = new UpdateOptions();

            // We don't handle any field for now.

            return options;
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
            return new UpdateOptions();
        }
    }

    /**
     * Converts TS UpdateOptions type to native UpdateOptions.
     */
    public static DeleteOptions jsonDeleteOptionsToNative(JSONObject optionsJson) {
        if (optionsJson == null)
            return new DeleteOptions();

        try {
            DeleteOptions options = new DeleteOptions();

            // We don't handle any field for now.

            return options;
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
            return new DeleteOptions();
        }
    }

    /**
     * Converts a JsonNode object to a JSONObject
     */
    public static JsonNode jsonObjectToJsonNode(JSONObject jsonObject) {
        try {
            return new ObjectMapper().readTree(jsonObject.toString());
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a JSONObject to a JsonNode object
     */
    public static JSONObject jsonNodeToJsonObject(JsonNode jsonNode) {
        try {
            return new JSONObject(new ObjectMapper().writeValueAsString(jsonNode));
        }
        catch (Exception e) {
            return null;
        }
    }

    public static JSONArray listToJSONArray(List list) {
        JSONArray array = new JSONArray();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            array.put(it.next());
        }
        return array;
    }

    public static <T> List<T> JSONArrayToList(JSONArray array) throws JSONException {
        ArrayList<T> list = new ArrayList<T>();
        for (int i=0; i<array.length(); i++) {
            list.add((T)array.get(i));
        }
        return list;
    }
}
