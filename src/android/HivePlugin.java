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

import android.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.elastos.did.DIDDocument;
import org.elastos.hive.AuthenticationHandler;
import org.elastos.hive.Callback;
import org.elastos.hive.Client;
import org.elastos.hive.Vault;
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
import org.elastos.hive.scripting.RawCondition;
import org.elastos.hive.scripting.RawExecutable;
import org.elastos.trinity.runtime.PreferenceManager;
import org.elastos.trinity.runtime.TrinityPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class HivePlugin extends TrinityPlugin {
    private HashMap<String, Client> clientMap = new HashMap<>();
    private HashMap<String, AuthenticationHandler> clientAuthHandlersMap = new HashMap<>();
    private HashMap<String, CallbackContext> clientAuthHandlerCallbackMap = new HashMap<>();
    private HashMap<String, CompletableFuture<String>> clientAuthHandlerCompletionMap = new HashMap<>();
    private HashMap<String, Vault> vaultMap = new HashMap<>();
    private HashMap<String, InputStream> readerMap = new HashMap<>();
    private HashMap<String, Integer> readerOffsetsMap = new HashMap<>(); // Current read offset byte position for each active reader
    private HashMap<String, OutputStream> writerMap = new HashMap<>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "getClient":
                    this.getClient(args, callbackContext);
                    break;
                case "client_getVaultAddress":
                    this.client_getVaultAddress(args, callbackContext);
                    break;
                case "client_setVaultAddress":
                    this.client_setVaultAddress(args, callbackContext);
                    break;
                case "client_setAuthHandlerChallengeCallback":
                    this.client_setAuthHandlerChallengeCallback(args, callbackContext);
                    break;
                case "client_sendAuthHandlerChallengeResponse":
                    this.client_sendAuthHandlerChallengeResponse(args, callbackContext);
                    break;
                case "client_getVault":
                    this.client_getVault(args, callbackContext);
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

    private String geDataDir() {
        return getDataPath();
    }

    private static String getDIDResolverUrl() {
        return PreferenceManager.getShareInstance().getDIDResolver();
    }

    private void getClient(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject optionsJson = args.isNull(0) ? null : args.getJSONObject(0);
        if (optionsJson == null) {
            callbackContext.error("Client creation options must be provided");
            return;
        }

        try {
            // final atomic reference as a way to pass our non final client Id to the auth handler.
            final AtomicReference<String> clientIdReference = new AtomicReference<>();

            Client.Options options = new Client.Options();
            options.setLocalDataPath(geDataDir());
            options.setDIDResolverUrl(getDIDResolverUrl());

            // Set the authentication DID document
            String authDIDDocumentJson = optionsJson.getString("authenticationDIDDocument");
            DIDDocument authenticationDIDDocument = DIDDocument.fromJson(authDIDDocumentJson);
            options.setAuthenticationDIDDocument(authenticationDIDDocument);

            // Create a authentication handler
            AuthenticationHandler authHandler = (challengeJwtToken) -> {
                CompletableFuture<String> future = new CompletableFuture<>();

                // Retrieve the JS side callback context to call it.
                // JS will call client_setAuthHandlerChallengeCallback() to send the response JWT
                CallbackContext authCallbackContext = clientAuthHandlerCallbackMap.get(clientIdReference.get());

                // Save the response callback ref to call it from client_sendAuthHandlerChallengeResponse
                clientAuthHandlerCompletionMap.put(clientIdReference.get(), future);

                // Call JS callback, so the dapp can start the auth flow.
                // Keep the callback active for future use.
                PluginResult result = new PluginResult(PluginResult.Status.OK, challengeJwtToken);
                result.setKeepCallback(true);
                authCallbackContext.sendPluginResult(result);

                return future;
            };
            options.setAuthenticationHandler(authHandler);

            Client client = Client.createInstance(options);
            String clientId = ""+System.identityHashCode(client);
            clientIdReference.set(clientId);
            clientMap.put(clientId, client);

            // Save the handler for later use
            clientAuthHandlersMap.put(clientId, authHandler);

            JSONObject ret = new JSONObject();
            ret.put("objectId", clientId);
            callbackContext.success(ret);
        }
        catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private void client_setVaultAddress(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String ownerDid = args.getString(0);
        String vaultAddress = args.getString(1);

        Client.setVaultProvider(ownerDid, vaultAddress);

        callbackContext.success();
    }

    private void client_getVaultAddress(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String ownerDid = args.getString(0);

        Client.getVaultProvider(ownerDid).thenAccept(address -> {
            callbackContext.success();
        });

        callbackContext.success((String)null);
    }

    private void client_setAuthHandlerChallengeCallback(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String clientObjectId = args.getString(0);

        // Save current callback content to be able to call it back when an authentication is requests by the hive SDK
        clientAuthHandlerCallbackMap.put(clientObjectId, callbackContext);

        // No immediate answer. Just keep the callback context reference for later use.
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void client_sendAuthHandlerChallengeResponse(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String clientObjectId = args.getString(0);
        String challengeResponseJwt = args.isNull(1) ? null : args.getString(1);

        if (challengeResponseJwt == null) {
            callbackContext.error("Empty challenge response given!");
            return;
        }

        // Retrieve the auth response callback and send the authentication JWT back to the hive SDK
        CompletableFuture<String> authResponseFuture = clientAuthHandlerCompletionMap.get(clientObjectId);
        authResponseFuture.complete(challengeResponseJwt);

        callbackContext.success();
    }

    private void client_getVault(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String clientObjectId = args.getString(0);
        String vaultOwnerDid = args.getString(1);

        try {
            Client client = clientMap.get(clientObjectId);
            client.getVault(vaultOwnerDid).thenAccept(vault -> {
                if (vault != null) {
                    String vaultId = "" + System.identityHashCode(vault);
                    vaultMap.put(vaultId, vault);

                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("objectId", vaultId);
                        ret.put("vaultProviderAddress", vault.getProviderAddress());
                        ret.put("vaultOwnerDid", vaultOwnerDid);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.toString());
                    }
                }
                else {
                    callbackContext.success((String)null);
                }
            }).exceptionally(e -> {
                callbackContext.error(e.getMessage());
                return null;
            });
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().createCollection(collectionName, options).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("created", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void database_deleteCollection(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().deleteCollection(collectionName).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("deleted", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().insertOne(collectionName, documentJsonNode, options).thenAccept(insertResult -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("insertedIds", new JSONArray(insertResult.insertedIds()));
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().countDocuments(collectionName, queryJsonNode, options).thenAccept(count -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("count", count);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().findOne(collectionName, queryJsonNode, options).thenAccept(result -> {
                    if (result == null || result.isNull())
                        callbackContext.success((String)null); // No result
                    else
                        callbackContext.success(HivePluginHelper.jsonNodeToJsonObject(result));
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().findMany(collectionName, queryJsonNode, options).thenAccept(results -> {
                    JSONArray jsonArray = new JSONArray();
                    for (JsonNode resultJson : results) {
                        jsonArray.put(HivePluginHelper.jsonNodeToJsonObject(resultJson));
                    }
                    callbackContext.success(jsonArray);
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().updateOne(collectionName, filterJsonNode, updateQueryJsonNode, options).thenAccept(result -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("matchedCount", result.matchedCount());
                        ret.put("modifiedCount", result.modifiedCount());
                        ret.put("upsertedCount", result.upsertedCount());
                        ret.put("upsertedId", result.upsertedId());
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getDatabase().deleteOne(collectionName, filterJsonNode, options).thenAccept(result -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("deletedCount", result.deletedCount());
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
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
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().upload(srcPath, OutputStream.class).thenAccept(stream -> {
                    try {
                        String objectId = "" + System.identityHashCode(stream);
                        writerMap.put(objectId, stream);
                        JSONObject ret = new JSONObject();
                        ret.put("objectId", objectId);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_download(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().download(srcPath, InputStream.class).thenAccept(reader -> {
                    try {
                        String objectId = "" + System.identityHashCode(reader);
                        readerMap.put(objectId, reader);
                        readerOffsetsMap.put(objectId, 0); // Current read offset is 0

                        JSONObject ret = new JSONObject();
                        ret.put("objectId", objectId);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_delete(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().delete(srcPath).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    if (e != null && e.getLocalizedMessage().contains("Item not found")) {
                        try {
                            JSONObject ret = new JSONObject();
                            ret.put("success", false);
                            callbackContext.success(ret);
                        }
                        catch (JSONException ex) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                    else {
                        callbackContext.error(e.getMessage());
                    }

                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_move(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().move(srcPath, dstPath).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_copy(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);
        String dstPath = args.getString(2);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().copy(srcPath, dstPath).thenAccept(v -> {
                    JSONObject ret = new JSONObject();
                    callbackContext.success(ret);
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_hash(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().hash(srcPath).thenAccept(hash -> {
                    callbackContext.success(hash);
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_list(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().list(srcPath).thenAccept(fileInfos -> {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (FileInfo info : fileInfos) {
                            jsonArray.put(HivePluginHelper.hiveFileInfoToPluginJson(info));
                        }
                        callbackContext.success(jsonArray);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void files_stat(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String srcPath = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getFiles().stat(srcPath).thenAccept(fileInfo -> {
                    try {
                        if (fileInfo != null) {
                            JSONObject ret = HivePluginHelper.hiveFileInfoToPluginJson(fileInfo);
                            callbackContext.success(ret);
                        }
                        else {
                            callbackContext.success((String)null);
                        }
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void scripting_setScript(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String functionName = args.getString(1);
        JSONObject executionSequenceJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject accessConditionJson = args.isNull(3) ? null : args.getJSONObject(3);

        RawCondition condition = accessConditionJson != null ? new RawCondition(accessConditionJson.toString()) : null;
        RawExecutable executable = new RawExecutable(executionSequenceJson.toString());

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getScripting().registerScript(functionName, condition, executable).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void scripting_call(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String functionName = args.getString(1);
        JSONObject params = args.isNull(2) ? null : args.getJSONObject(2);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getScripting().call(functionName, HivePluginHelper.jsonObjectToJsonNode(params), JsonNode.class).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    }
                }).exceptionally(e -> {
                    callbackContext.error(e.getMessage());
                    return null;
                });
            }
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void writer_write(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);
        String base64encodedFromUint8Array = args.getString(1);

        // TODO: get threading / looper from carrier file transfer

        try {
            OutputStream writer = writerMap.get(writerObjectId);

            // Cordova encodes UInt8Array in TS to base64 encoded in java.
            byte[] data = Base64.decode(base64encodedFromUint8Array, Base64.DEFAULT);
            writer.write(data);

            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void writer_flush(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);

        try {
            OutputStream writer = writerMap.get(writerObjectId);
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
            OutputStream writer = writerMap.get(writerObjectId);
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
            byte[] buffer = new byte[bytesCount];
            InputStream reader = readerMap.get(readerObjectId);

            // Resume reading at the previous read offset
            //int currentReadOffset = readerOffsetsMap.get(readerObjectId);
            int readBytes = reader.read(buffer, 0, bytesCount);

            if (readBytes != -1) {
                // Move read offset to the next position
                //readerOffsetsMap.put(readerObjectId, currentReadOffset + readBytes);
                callbackContext.success(Base64.encodeToString(buffer, 0, readBytes, 0));
            }
            else {
                callbackContext.success((String)null);
            }
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void reader_readAll(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        try {
            byte[] buffer = new byte[1024];
            InputStream reader = readerMap.get(readerObjectId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int readBytes;
            do {
                readBytes = reader.read(buffer);
                outputStream.write(buffer, 0, readBytes);
            }
            while (readBytes != -1);

            callbackContext.success(outputStream.toByteArray());
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void reader_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        try {
            InputStream reader = readerMap.get(readerObjectId);
            reader.close();
            readerMap.remove(readerObjectId);
            readerOffsetsMap.remove(readerObjectId);
            callbackContext.success();
        }
        catch (IOException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private boolean ensureValidVault(Vault vault, CallbackContext callbackContext) {
        if (vault == null) {
            callbackContext.error("The passed vault is a null object...");
            return false;
        }
        else
            return true;
    }
}
