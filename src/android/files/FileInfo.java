package org.elastos.plugins.hive.files;

import org.json.JSONObject;

import java.util.Date;

public class FileInfo {
    String name = null;
    long size = -1;
    Date lastModified = null;
    FileType type = null;

    public FileInfo() {
    }

    public static FileInfo fromJsonObject(JSONObject jsonObject) throws Exception {
        FileInfo options = new FileInfo();

        if (jsonObject.has("name"))
            options.name = jsonObject.getString("name");

        if (jsonObject.has("size"))
            options.size = jsonObject.getLong("size");

        if (jsonObject.has("lastModified"))
            options.lastModified = new Date(jsonObject.getLong("lastModified"));

        if (jsonObject.has("type"))
            options.type =  FileType.fromId(jsonObject.getInt("type"));

        return options;
    }
}