/*
 * Copyright (c) 2019 Elastos Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.elastos.trinity.plugins.hive;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.elastos.hive.*;
import org.elastos.hive.exceptions.HiveException;

class ObjectMap<T> {
    static ObjectMap clientMap = new ClientMap();
    static ObjectMap driveMap  = new DriveMap();
    static ObjectMap dirMap    = new DirectoryMap();
    static ObjectMap fileMap   = new FileMap();

    static final int CLIENT = 1;
    static final int DRIVE  = 2;
    static final int DIR    = 3;
    static final int FILE   = 4;

    private HashMap<Integer, T> objMap;

    ObjectMap() {
        objMap = new HashMap<Integer, T>();
    }

    T get(String objId) {
        return objMap.get(objId);
    }

    void put(String objId, T obj) {
        objMap.put(objid,  obj);
    }

    static ObjectMap acquire(String mapId) {
        ObjectMap objMap;

        switch (mapId) {
        case CLIENT:
            objMap = clientMap;
            break;
        case DRIVE:
            objMap = driveMap;
            break;
        case DIR:
            objMap = directroyMap;
            break;
        case FILE:
            objMap = fileMap;
            break;
        }
        return objMap;
    }

    static class ClientMap extends ObjectMap<Client> {}
    static class DriveMap  extends ObjectMap<Drive> {}
    static class DirMap    extends ObjectMap<Directory> {}
    static class FileMap   extends ObjectMap<File> {}

    static boolean isClientMap(ObjectMap map) {
        return (map instanceof ClientMap);
    }

    static boolean isDriveMap(ObjectMap map) {
        return (map instanceof DriveMap);
    }

    static boolean isDirMap(ObjectMap map) {
        return (map instanceof DirectoryMap);
    }

    static boolean isFileMap(ObjectMap map) {
        return (map instanceof FileMap);
    }

    static ClientMap toClientMap(ObjectMap map) {
        return (isClientMap(map) ? (ClientMap)map : null);
    }

    static DriveMap toDriveMap(ObjectMap map) {
        return (isDriveMap(map) ? (DriveMap)map : null);
    }

    static DirectoryMap toDirMap(ObjectMap map) {
        return (isDirMap(map) ? (DirectoryMap)map : null);
    }

    static FileMap toFileMap(ObjectMap map) {
        return (isFileMap(map) ? (FileMap)map : null);
    }
}
