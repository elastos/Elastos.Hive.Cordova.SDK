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
import org.elastos.hive.Client;
import org.elastos.hive.exception.HiveException;
import org.elastos.trinity.plugins.hive.database.CountOptions;
import org.elastos.trinity.plugins.hive.database.CreateCollectionOptions;
import org.elastos.trinity.runtime.TrinityPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HivePlugin extends TrinityPlugin {
    private HashMap<String, Client> clientMap = new HashMap<>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "connectToVault":
                    this.connectToVault(args, callbackContext);
                    break;
                case "database_createCollection":
                    this.database_createCollection(args, callbackContext);
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
                case "files_createFolder":
                    this.files_createFolder(args, callbackContext);
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

    private void connectToVault(JSONArray args, CallbackContext callbackContext) throws JSONException {
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
        CreateCollectionOptions options = null;

        try {
            if (optionsJson != null)
                options = CreateCollectionOptions.fromJsonObject(optionsJson);
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
        }

        if (options == null) {
            options = new CreateCollectionOptions(); // default options
        }

        // Retrieve the vault
        Client client = clientMap.get(vaultObjectId);
        client.getDatabase().createCol(collectionName, null).thenAccept(v -> {
            System.out.println("COLLECTION CREATED");

            JSONObject ret = new JSONObject();
            callbackContext.success(ret);
        });

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void database_insertOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_countDocuments(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject queryJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        CountOptions options = null;

        try {
            if (optionsJson != null)
                options = CountOptions.fromJsonObject(optionsJson);
        }
        catch (Exception e) {
            // Invalid options passed? We'll use default options
        }

        if (options == null) {
            options = new CountOptions(); // default options
        }

        // Retrieve the vault
        Client client = clientMap.get(vaultObjectId);
        // TODO: wait for java api added

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void database_findOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_findMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_updateOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_updateMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_deleteOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void database_deleteMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for non-eve api style
    }

    private void files_upload(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api with writer
    }

    private void files_download(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api with reader
    }

    private void files_delete(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        // TODO: handle failure case

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().deleteFile(srcPath).thenAccept(v -> {
            callbackContext.success();
        });
    }

    private void files_createFolder(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        // TODO: handle failure case

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().createFolder(srcPath).thenAccept(v -> {
            callbackContext.success();
        });
    }

    private void files_move(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        // TODO: handle failure case

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().move(srcPath, dstPath).thenAccept(v -> {
                JSONObject ret = new JSONObject();
                callbackContext.success(ret);
        });
    }

    private void files_copy(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().copy(srcPath, dstPath).thenAccept(v -> {
            JSONObject ret = new JSONObject();
            callbackContext.success(ret);
        });
    }

    private void files_hash(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().hash(srcPath).thenAccept(hash -> {
           // TODO: uncomment when hash() return type is String - callbackContext.success(hash);
        });
    }

    private void files_list(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        Client client = clientMap.get(vaultObjectId);
        client.getVaultFiles().list(srcPath).thenAccept(fileInfos -> {
            // TODO: when list returns fileInfo, not paths
        });
    }

    private void files_stat(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: when stat() is available
    }

    private void scripting_registerSubCondition(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait to java api added
    }

    private void scripting_setScript(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait to java api added
    }

    private void scripting_call(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait to java api added
    }

    private void writer_write(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }

    private void writer_flush(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }

    private void writer_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }

    private void reader_read(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }

    private void reader_readAll(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }

    private void reader_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO: wait for new java api
    }
}
