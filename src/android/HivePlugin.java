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

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.cordova.CallbackContext;
import org.elastos.hive.Client;
import org.elastos.hive.database.CountOptions;
import org.elastos.hive.database.CreateCollectionOptions;
import org.elastos.hive.database.DeleteOptions;
import org.elastos.hive.database.FindOptions;
import org.elastos.hive.database.InsertOptions;
import org.elastos.hive.database.UpdateOptions;
import org.elastos.hive.exception.HiveException;
import org.elastos.hive.file.FileInfo;
import org.elastos.hive.scripting.Condition;
import org.elastos.hive.scripting.Executable;
import org.elastos.trinity.runtime.TrinityPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

public class HivePlugin extends TrinityPlugin {
    private HashMap<String, Client> clientMap = new HashMap<>();
    private HashMap<String, Reader> readerMap = new HashMap<>();
    private HashMap<String, Integer> readerOffsetsMap = new HashMap<>(); // Current read offset byte position for each active reader
    private HashMap<String, Writer> writerMap = new HashMap<>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "getVault":
                    this.getVault(args, callbackContext);
                    break;
                case "database_createCollection":
                    this.database_createCollection(args, callbackContext);
                    break;
                case "database_deleteCollection":
                    this.database_deleteCollection(args, callbackContext);
                    break;
                case "database_insertOne":
                    this.database_insertOne(args, callbackContext);
                    break;
                case "database_countDocuments":
                    this.database_countDocuments(args, callbackContext);
                    break;
                case "database_findOne":
                    this.database_findOne(args, callbackContext);
                    break;
                case "database_findMany":
                    this.database_findMany(args, callbackContext);
                    break;
                case "database_updateOne":
                    this.database_updateOne(args, callbackContext);
                    break;
                case "database_updateMany":
                    this.database_updateMany(args, callbackContext);
                    break;
                case "database_deleteOne":
                    this.database_deleteOne(args, callbackContext);
                    break;
                case "database_deleteMany":
                    this.database_deleteMany(args, callbackContext);
                    break;
                case "files_upload":
                    this.files_upload(args, callbackContext);
                    break;
                case "files_download":
                    this.files_download(args, callbackContext);
                    break;
                case "files_delete":
                    this.files_delete(args, callbackContext);
                    break;
                case "files_move":
                    this.files_move(args, callbackContext);
                    break;
                case "files_copy":
                    this.files_copy(args, callbackContext);
                    break;
                case "files_hash":
                    this.files_hash(args, callbackContext);
                    break;
                case "files_list":
                    this.files_list(args, callbackContext);
                    break;
                case "files_stat":
                    this.files_stat(args, callbackContext);
                    break;
                case "scripting_registerSubCondition":
                    this.scripting_registerSubCondition(args, callbackContext);
                    break;
                case "scripting_setScript":
                    this.scripting_setScript(args, callbackContext);
                    break;
                case "scripting_call":
                    this.scripting_call(args, callbackContext);
                    break;
                case "writer_write":
                    this.writer_write(args, callbackContext);
                    break;
                case "writer_flush":
                    this.writer_flush(args, callbackContext);
                    break;
                case "writer_close":
                    this.writer_close(args, callbackContext);
                    break;
                case "reader_read":
                    this.reader_read(args, callbackContext);
                    break;
                case "reader_readAll":
                    this.reader_readAll(args, callbackContext);
                    break;
                case "reader_close":
                    this.reader_close(args, callbackContext);
                    break;
                default:
                    return false;
            }
        } catch (JSONException e) {
            callbackContext.error(e.getLocalizedMessage());
        }
        return true;
    }

    private void getVault(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultProviderAddress = args.getString(0);
        String vaultOwnerDid = args.getString(1);

        try {
            // TODO: refactor client to vault
            Client client = Client.createInstance(new Client.Options() {
                @Override
                protected Client buildClient() {
                    return null;
                }
            });

            String clientId = ""+System.identityHashCode(client);
            clientMap.put(clientId, client);

            JSONObject ret = new JSONObject();
            ret.put("objectId", clientId);
            ret.put("vaultProviderAddress", vaultProviderAddress);
            ret.put("vaultOwnerDid", vaultOwnerDid);
            callbackContext.success(ret);
        }
        catch (HiveException e) {
            callbackContext.error(e.toString());
        }
    }

    private void database_createCollection(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject optionsJson = args.isNull(2) ? null : args.getJSONObject(2);

        CreateCollectionOptions options = new CreateCollectionOptions();

        try {
            if (optionsJson != null) {
                // Nothing to do, no option handle for now.
            }
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
        }

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().createCollection(collectionName, options).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("created", success);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_deleteCollection(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().deleteCollection(collectionName).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("deleted", success);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_insertOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject documentJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        InsertOptions options = new InsertOptions();

        try {
            if (optionsJson != null) {
                // Nothing to do, no option handle for now.
            }
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
        }

        JsonNode documentJsonNode = HivePluginHelper.jsonObjectToJsonNode(documentJson);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().insertOne(collectionName, documentJsonNode, options).thenAccept(insertResult -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("insertedIds", new JSONArray(insertResult.insertedIds()));
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_countDocuments(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject queryJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        JsonNode queryJsonNode = HivePluginHelper.jsonObjectToJsonNode(queryJson);

        CountOptions options = new CountOptions();

        try {
            if (optionsJson != null) {
                // Nothing to do, no option handle for now.
            }
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
        }

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().countDocuments(collectionName, queryJsonNode, options).thenAccept(count -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("count", count);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_findOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject queryJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        FindOptions options = HivePluginHelper.jsonFindOptionsToNative(optionsJson);

        JsonNode queryJsonNode = HivePluginHelper.jsonObjectToJsonNode(queryJson);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().findOne(collectionName, queryJsonNode, options).thenAccept(result -> {
                if (result == null)
                    callbackContext.success(); // No result
                else
                    callbackContext.success(HivePluginHelper.jsonNodeToJsonObject(result));
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_findMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject queryJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        FindOptions options = HivePluginHelper.jsonFindOptionsToNative(optionsJson);

        JsonNode queryJsonNode = HivePluginHelper.jsonObjectToJsonNode(queryJson);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().findMany(collectionName, queryJsonNode, options).thenAccept(results -> {
                JSONArray jsonArray = new JSONArray();
                for (JsonNode resultJson : results) {
                    jsonArray.put(HivePluginHelper.jsonNodeToJsonObject(resultJson));
                }
                callbackContext.success(jsonArray);
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_updateOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject filterJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject updatequeryJson = args.isNull(3) ? null : args.getJSONObject(3);
        JSONObject optionsJson = args.isNull(4) ? null : args.getJSONObject(4);

        UpdateOptions options = HivePluginHelper.jsonUpdateOptionsToNative(optionsJson);

        JsonNode filterJsonNode = HivePluginHelper.jsonObjectToJsonNode(filterJson);
        JsonNode updateQueryJsonNode = HivePluginHelper.jsonObjectToJsonNode(updatequeryJson);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().updateOne(collectionName, filterJsonNode, updateQueryJsonNode, options).thenAccept(result -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("matchedCount", result.matchedCount());
                    ret.put("modifiedCount", result.modifiedCount());
                    ret.put("upsertedCount", result.upsertedCount());
                    ret.put("upsertedId", result.upsertedId());
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_updateMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // For now, update one and update many seem to be totally identical on the client side.
        database_updateOne(args, callbackContext);
    }

    private void database_deleteOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject filterJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        DeleteOptions options = HivePluginHelper.jsonDeleteOptionsToNative(optionsJson);

        JsonNode filterJsonNode = HivePluginHelper.jsonObjectToJsonNode(filterJson);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getDatabase().deleteOne(collectionName, filterJsonNode, options).thenAccept(result -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("deletedCount", result.deletedCount());
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_deleteMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // For now, delete one and delete many seem to be totally identical on the client side.
        database_deleteOne(args, callbackContext);
    }

    private void files_upload(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().upload(srcPath).thenAccept(writer -> {
                try {
                    String objectId = "" + System.identityHashCode(writer);
                    writerMap.put(objectId, writer);

                    JSONObject ret = new JSONObject();
                    ret.put("objectId", objectId);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_download(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().download(srcPath).thenAccept(reader -> {
                try {
                    String objectId = "" + System.identityHashCode(reader);
                    readerMap.put(objectId, reader);
                    readerOffsetsMap.put(objectId, 0); // Current read offset is 0

                    JSONObject ret = new JSONObject();
                    ret.put("objectId", objectId);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_delete(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().delete(srcPath).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("success", success);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_move(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().move(srcPath, dstPath).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("success", success);
                    callbackContext.success(ret);
                }
                catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_copy(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().copy(srcPath, dstPath).thenAccept(v -> {
                JSONObject ret = new JSONObject();
                callbackContext.success(ret);
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_hash(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().hash(srcPath).thenAccept(hash -> {
                callbackContext.success(hash);
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_list(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().list(srcPath).thenAccept(fileInfos -> {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (FileInfo info : fileInfos) {
                        jsonArray.put(HivePluginHelper.hiveFileInfoToPluginJson(info));
                    }
                    callbackContext.success(jsonArray);
                }
                catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_stat(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getFiles().stat(srcPath).thenAccept(fileInfo -> {
                try {
                    JSONObject ret = HivePluginHelper.hiveFileInfoToPluginJson(fileInfo);
                    callbackContext.success(ret);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void scripting_registerSubCondition(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String conditionName = args.getString(1);
        JSONObject conditionJson = args.isNull(2) ? null : args.getJSONObject(2);

        // TODO: build real condition from TS json when api is ready, or use string condition
        Condition condition = new Condition("TestCondition", "fakeName") {
            @Override
            public Object getBody() {
                return null;
            }
        };

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getScripting().registerCondition(conditionName, condition).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("success", success);
                    callbackContext.success(ret);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void scripting_setScript(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String functionName = args.getString(1);
        JSONObject executionSequenceJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject accessConditionJson = args.isNull(3) ? null : args.getJSONObject(3);

        // TODO: build real condition from TS json when api is ready, or use string condition
        Condition condition = new Condition("TestCondition", "fakeName") {
            @Override
            public Object getBody() {
                return null;
            }
        };

        Executable fakeExecutable = new Executable("aaa","bbb") {
            @Override
            public Object getBody() {
                return null;
            }
        };

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getScripting().registerScript(functionName, condition, fakeExecutable).thenAccept(success -> {
                try {
                    JSONObject ret = new JSONObject();
                    ret.put("success", success);
                    callbackContext.success(ret);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void scripting_call(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String functionName = args.getString(1);
        JSONObject params = args.isNull(2) ? null : args.getJSONObject(2);

        try {
            Client client = clientMap.get(vaultObjectId);
            client.getScripting().call(functionName, HivePluginHelper.jsonObjectToJsonNode(params)).thenAccept(success -> {
                try {
                    // TODO: why do we get a Reader here, not a JSONObject?
                    JSONObject ret = new JSONObject();
                    ret.put("success", success);
                    callbackContext.success(ret);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            });
        }
        catch (HiveException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void writer_write(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);
        String blob = args.getString(1); // TODO: check type coming from JS Blob

        try {
            Writer writer = writerMap.get(writerObjectId);
            writer.write(blob);
            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void writer_flush(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);

        try {
            Writer writer = writerMap.get(writerObjectId);
            writer.flush();
            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void writer_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);

        try {
            Writer writer = writerMap.get(writerObjectId);
            writer.close();
            writerMap.remove(writerObjectId);
            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void reader_read(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);
        int bytesCount = args.getInt(1);

        try {
            char[] buffer = new char[bytesCount];
            Reader reader = readerMap.get(readerObjectId);

            // Resume reading at the previous read offset
            int currentReadOffset = readerOffsetsMap.get(readerObjectId);
            int readBytes = reader.read(buffer, currentReadOffset, bytesCount);

            // Move read offset to the next position
            readerOffsetsMap.put(readerObjectId, currentReadOffset+readBytes);

            callbackContext.success(new String(buffer)); // TODO: Probably probably the wrong type, not String...
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void reader_readAll(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        try {
            char[] buffer = new char[1024];
            Reader reader = readerMap.get(readerObjectId);
            String output = new String();

            int readBytes;
            do {
                readBytes = reader.read(buffer);
                output += new String(buffer); // TODO: right format/type
            }
            while (readBytes != -1);

            callbackContext.success(output);
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void reader_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        try {
            Reader reader = readerMap.get(readerObjectId);
            reader.close();
            readerMap.remove(readerObjectId);
            readerOffsetsMap.remove(readerObjectId);
            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }
}
