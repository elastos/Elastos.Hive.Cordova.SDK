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
import org.elastos.did.exception.MalformedDocumentException;
import org.elastos.hive.AuthenticationHandler;
import org.elastos.hive.Client;
import org.elastos.hive.HiveContext;
import org.elastos.hive.Vault;
import org.elastos.hive.database.CountOptions;
import org.elastos.hive.database.CreateCollectionOptions;
import org.elastos.hive.database.DeleteOptions;
import org.elastos.hive.database.DeleteResult;
import org.elastos.hive.database.FindOptions;
import org.elastos.hive.database.InsertOptions;
import org.elastos.hive.database.UpdateOptions;
import org.elastos.hive.database.UpdateResult;
import org.elastos.hive.exception.FileNotFoundException;
import org.elastos.hive.exception.HiveException;
import org.elastos.hive.exception.ProviderNotSetException;
import org.elastos.hive.exception.VaultAlreadyExistException;
import org.elastos.hive.exception.VaultNotFoundException;
import org.elastos.hive.files.FileInfo;
import org.elastos.hive.payment.Order;
import org.elastos.hive.scripting.RawCondition;
import org.elastos.hive.scripting.RawExecutable;
import org.elastos.trinity.runtime.PreferenceManager;
import org.elastos.trinity.runtime.TrinityPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class HivePlugin extends TrinityPlugin {
    private HashMap<String, Client> clientMap = new HashMap<>();
    private HashMap<String, CallbackContext> clientAuthHandlerCallbackMap = new HashMap<>();
    private HashMap<String, CompletableFuture<String>> clientAuthHandlerCompletionMap = new HashMap<>();
    private HashMap<String, Vault> vaultMap = new HashMap<>();
    private HashMap<String, InputStream> readerMap = new HashMap<>();
    private HashMap<String, Integer> readerOffsetsMap = new HashMap<>(); // Current read offset byte position for each active reader
    private HashMap<String, OutputStream> writerMap = new HashMap<>();

    private static boolean didResolverInitialized = false;

    private enum EnhancedErrorCodes {
        // Vault errors - range -1 ~ -999
        VAULT_NOT_FOUND(-1),
        PROVIDER_NOT_PUBLISHED(-2),
        DID_NOT_PUBLISHED(-3),

        // Database errors - range -1000 ~ -1999
        COLLECTION_NOT_FOUND(-1000),

        // File errors - range -2000 ~ -2999
        FILE_NOT_FOUND(-2000),

        UNSPECIFIED(-9999);

        public int mValue;

        EnhancedErrorCodes(int value) {
            mValue = value;
        }

        public static EnhancedErrorCodes fromValue(int value) {
            for(EnhancedErrorCodes t : values()) {
                if (t.mValue == value) {
                    return t;
                }
            }
            return UNSPECIFIED;
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "getClient":
                    this.getClient(args, callbackContext);
                    break;
                case "client_setAuthHandlerChallengeCallback":
                    this.client_setAuthHandlerChallengeCallback(args, callbackContext);
                    break;
                case "client_sendAuthHandlerChallengeResponse":
                    this.client_sendAuthHandlerChallengeResponse(args, callbackContext);
                    break;
                case "client_createVault":
                    this.client_createVault(args, callbackContext);
                    break;
                case "client_getVault":
                    this.client_getVault(args, callbackContext);
                    break;
                case "vault_getNodeVersion":
                    this.vault_getNodeVersion(args, callbackContext);
                    break;
                case "vault_revokeAccessToken":
                    this.vault_revokeAccessToken(args, callbackContext);
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
                case "database_insertMany":
                    this.database_insertMany(args, callbackContext);
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
                case "scripting_downloadFile":
                    this.scripting_downloadFile(args, callbackContext);
                    break;
                case "scripting_uploadFile":
                    this.scripting_uploadFile(args, callbackContext);
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
                case "payment_getPricingInfo":
                    this.payment_getPricingInfo(args, callbackContext);
                    break;
                case "payment_getPricingPlan":
                    this.payment_getPricingPlan(args, callbackContext);
                    break;
                case "payment_placeOrder":
                    this.payment_placeOrder(args, callbackContext);
                    break;
                case "payment_payOrder":
                    this.payment_payOrder(args, callbackContext);
                    break;
                case "payment_getOrder":
                    this.payment_getOrder(args, callbackContext);
                    break;
                case "payment_getAllOrders":
                    this.payment_getAllOrders(args, callbackContext);
                    break;
                case "payment_getActivePricingPlan":
                    this.payment_getActivePricingPlan(args, callbackContext);
                    break;
                case "payment_getPaymentVersion":
                    this.payment_getPaymentVersion(args, callbackContext);
                    break;
                default:
                    return false;
            }
        } catch (JSONException e) {
            callbackContext.error(e.getLocalizedMessage());
        }
        return true;
    }

    private JSONObject createEnhancedError(EnhancedErrorCodes code, String message) {
        JSONObject error = new JSONObject();

        try {
            error.put("code", code.mValue);
            error.put("message", message);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return error;
    }

    /**
     * Returns the passed error as a JSON object with a clear error code if we are able to know it. Otherwise,
     * the error description is returned as a string.
     */
    private void enhancedError(CallbackContext callbackContext, Throwable exception) {
        String errorMessage = exception.getMessage();
        PluginResult result = null;

        String hiveErrorMessage = exception.getMessage();
        if (hiveErrorMessage != null && hiveErrorMessage.contains("collection not exist")) {
            result = new PluginResult(PluginResult.Status.ERROR, Objects.requireNonNull(createEnhancedError(EnhancedErrorCodes.COLLECTION_NOT_FOUND, hiveErrorMessage)));
        }
        else if (exception instanceof VaultNotFoundException) {
            result = new PluginResult(PluginResult.Status.ERROR, Objects.requireNonNull(createEnhancedError(EnhancedErrorCodes.VAULT_NOT_FOUND,
                    "Vault does not exist. It has to be created by calling createVault()")));
        }
        else if (exception instanceof FileNotFoundException) {
            result = new PluginResult(PluginResult.Status.ERROR, Objects.requireNonNull(createEnhancedError(EnhancedErrorCodes.FILE_NOT_FOUND, hiveErrorMessage)));
        }

        if (result == null) {
            result = new PluginResult(PluginResult.Status.ERROR, errorMessage);
        }

        callbackContext.sendPluginResult(result);
    }

    private String getDataDir() {
        return getDataPath();
    }

    private static String getDIDResolverUrl() {
        return PreferenceManager.getShareInstance().getDIDResolver();
    }

    private void setupDIDResolver() throws HiveException {
        if (didResolverInitialized)
            return;

        // NOTE: Static way to set the DID resolver. This means we'd better hope that every app in trinity uses
        // the same network/resolver, otherwise one overwrites the other. The Hive SDK only provides such static method
        // for now...
        Client.setupResolver(getDIDResolverUrl(),  appManager.activity.getCacheDir()+"/didCache");

        didResolverInitialized = true;
    }

    private void getClient(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject optionsJson = args.isNull(0) ? null : args.getJSONObject(0);
        if (optionsJson == null) {
            callbackContext.error("Client creation options must be provided");
            return;
        }

        try {
            setupDIDResolver();

            // final atomic reference as a way to pass our non final client Id to the auth handler.
            final AtomicReference<String> clientIdReference = new AtomicReference<>();
            String authDIDDocumentJson = optionsJson.getString("authenticationDIDDocument");

            HiveContext context = new HiveContext() {
                @Override
                public String getLocalDataDir() {
                    return null;
                }

                @Override
                public DIDDocument getAppInstanceDocument() {
                    try {
                        DIDDocument authenticationDIDDocument = DIDDocument.fromJson(authDIDDocumentJson);
                        return authenticationDIDDocument;
                    } catch (MalformedDocumentException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public CompletableFuture<String> getAuthorization(String jwtToken) {
                    CompletableFuture<String> future = new CompletableFuture<>();

                    // Retrieve the JS side callback context to call it.
                    // JS will call client_setAuthHandlerChallengeCallback() to send the response JWT
                    CallbackContext authCallbackContext = clientAuthHandlerCallbackMap.get(clientIdReference.get());

                    // Save the response callback ref to call it from client_sendAuthHandlerChallengeResponse
                    clientAuthHandlerCompletionMap.put(clientIdReference.get(), future);

                    // Call JS callback, so the dapp can start the auth flow.
                    // Keep the callback active for future use.
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jwtToken);
                    result.setKeepCallback(true);
                    authCallbackContext.sendPluginResult(result);

                    return future;
                }
            };

            Client client = Client.createInstance(context);
            String clientId = ""+System.identityHashCode(client);
            clientIdReference.set(clientId);
            clientMap.put(clientId, client);

            JSONObject ret = new JSONObject();
            ret.put("objectId", clientId);
            callbackContext.success(ret);
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    /*private void client_setVaultAddress(JSONArray args, CallbackContext callbackContext) throws JSONException {
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
    }*/

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

        /*if (challengeResponseJwt == null) {
            callbackContext.error("Empty challenge response given!");
            return;
        }*/

        // Retrieve the auth response callback and send the authentication JWT back to the hive SDK
        CompletableFuture<String> authResponseFuture = clientAuthHandlerCompletionMap.get(clientObjectId);
        authResponseFuture.complete(challengeResponseJwt);

        callbackContext.success();
    }

    private void client_createVault(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String clientObjectId = args.getString(0);
        String vaultOwnerDid = args.isNull(1) ? null : args.getString(1);
        String vaultProviderAddress = args.isNull(2) ? null : args.getString(2);

        if (vaultOwnerDid == null) {
            callbackContext.error("createVault() cannot be called with a null string as vault owner DID");
            return;
        }

        if (vaultProviderAddress == null) {
            callbackContext.error("createVault() cannot be called with a null string as vault provider address");
            return;
        }

        try {
            Client client = clientMap.get(clientObjectId);
            client.createVault(vaultOwnerDid, vaultProviderAddress).thenAccept(vault -> {
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
                        enhancedError(callbackContext, e);
                    }
                }
                else {
                    callbackContext.success((String)null);
                }
            }).exceptionally(e -> {
                Throwable cause = e.getCause();
                if (cause instanceof ProviderNotSetException) {
                    callbackContext.success((String)null);
                } else if (cause instanceof VaultAlreadyExistException) {
                    // Vault already exists, return null, not an error.
                    callbackContext.success((String)null);
                } else {
                    enhancedError(callbackContext, e.getCause());
                }
                return null;
            });
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void client_getVault(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String clientObjectId = args.getString(0);
        String vaultOwnerDid = args.isNull(1) ? null : args.getString(1);

        if (vaultOwnerDid == null) {
            callbackContext.error("getVault() cannot be called with a null string as vault owner DID");
            return;
        }

        try {
            Client client = clientMap.get(clientObjectId);
            client.getVault(vaultOwnerDid, null).thenAccept(vault -> {
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
                        enhancedError(callbackContext, e);
                    }
                }
                else {
                    callbackContext.success((String)null);
                }
            }).exceptionally(e -> {
                Throwable cause = e.getCause();
                if (cause instanceof ProviderNotSetException) {
                    callbackContext.success((String)null);
                } else if (cause instanceof VaultNotFoundException) {
                    enhancedError(callbackContext, cause);
                } else {
                    callbackContext.error("client_getVault error: "+e.getMessage());
                }
                return null;
            });
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void vault_getNodeVersion(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getNodeVersion().thenAccept(version -> {
                    callbackContext.success(version);
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void vault_revokeAccessToken(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.revokeAccessToken();
                callbackContext.success();
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                new Thread(()->{
                    vault.getDatabase().createCollection(collectionName, options).thenAccept(success -> {
                        try {
                            JSONObject ret = new JSONObject();
                            ret.put("created", success);
                            callbackContext.success(ret);
                        } catch (JSONException e) {
                            enhancedError(callbackContext, e);
                        }
                    }).exceptionally(e -> {
                        enhancedError(callbackContext, e.getCause());
                        return null;
                    });

                }).start();
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        ret.put("insertedId", insertResult.insertedId());
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void database_insertMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONArray documentsJson = args.isNull(2) ? null : args.getJSONArray(2);
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



        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                // Create the list of documents
                ArrayList<JsonNode> documentsJsonNodes = new ArrayList<>();
                for (int i=0; i<documentsJson.length(); i++) {
                    JsonNode documentJsonNode = HivePluginHelper.jsonObjectToJsonNode(documentsJson.getJSONObject(i));
                    documentsJsonNodes.add(documentJsonNode);
                }

                vault.getDatabase().insertMany(collectionName, documentsJsonNodes, options).thenAccept(insertResult -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("insertedIds", HivePluginHelper.listToJSONArray(insertResult.insertedIds()));
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void database_update(JSONArray args, CallbackContext callbackContext, boolean onlyUpdateOne) throws JSONException {
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
                CompletableFuture<UpdateResult> completableResult = null;
                if (onlyUpdateOne) {
                    // UPDATE ONE
                    completableResult = vault.getDatabase().updateOne(collectionName, filterJsonNode, updateQueryJsonNode, options);
                }
                else {
                    // UPDATE MANY
                    completableResult = vault.getDatabase().updateMany(collectionName, filterJsonNode, updateQueryJsonNode, options);
                }

                completableResult.thenAccept(result -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("matchedCount", result.matchedCount());
                        ret.put("modifiedCount", result.modifiedCount());
                        ret.put("upsertedCount", result.upsertedCount());
                        ret.put("upsertedId", result.upsertedId());
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void database_updateOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        database_update(args, callbackContext, true);
    }

    private void database_updateMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        database_update(args, callbackContext, false);
    }

    private void database_delete(JSONArray args, CallbackContext callbackContext, boolean onlyDeleteOne) throws JSONException {
        String vaultObjectId = args.getString(0);
        String collectionName = args.getString(1);
        JSONObject filterJson = args.isNull(2) ? null : args.getJSONObject(2);
        JSONObject optionsJson = args.isNull(3) ? null : args.getJSONObject(3);

        DeleteOptions options = HivePluginHelper.jsonDeleteOptionsToNative(optionsJson);

        JsonNode filterJsonNode = HivePluginHelper.jsonObjectToJsonNode(filterJson);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                CompletableFuture<DeleteResult> completableResult = null;
                if (onlyDeleteOne) {
                    // DELETE ONE
                    completableResult = vault.getDatabase().deleteOne(collectionName, filterJsonNode, options);
                }
                else {
                    // DELETE MANY
                    completableResult = vault.getDatabase().deleteMany(collectionName, filterJsonNode, options);
                }

                completableResult.thenAccept(result -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("deletedCount", result.deletedCount());
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void database_deleteOne(JSONArray args, CallbackContext callbackContext) throws JSONException {
        database_delete(args, callbackContext, true);
    }

    private void database_deleteMany(JSONArray args, CallbackContext callbackContext) throws JSONException {
        database_delete(args, callbackContext, false);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    if (e != null && e.getLocalizedMessage().contains("Item not found")) {
                        try {
                            JSONObject ret = new JSONObject();
                            ret.put("success", false);
                            callbackContext.success(ret);
                        }
                        catch (JSONException ex) {
                            enhancedError(callbackContext, e);
                        }
                    }
                    else {
                        enhancedError(callbackContext, e.getCause());
                    }

                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void scripting_call(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String functionName = args.getString(1);

        JSONObject params = null;
        String appDID = null;
        if (!args.isNull(2)) {
            params = args.getJSONObject(2);
            if (!args.isNull(3)) {
                appDID = args.getString(3);
            }
        }

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getScripting().callScript(functionName, HivePluginHelper.jsonObjectToJsonNode(params), appDID, JsonNode.class).thenAccept(scriptResult -> {
                    callbackContext.success(HivePluginHelper.jsonNodeToJsonObject(scriptResult));
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void scripting_downloadFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String transactionId = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getScripting().downloadFile(transactionId, InputStream.class).thenAccept(reader -> {
                    // Same implementation as for files_download()
                    try {
                        String objectId = "" + System.identityHashCode(reader);
                        readerMap.put(objectId, reader);
                        readerOffsetsMap.put(objectId, 0); // Current read offset is 0

                        JSONObject ret = new JSONObject();
                        ret.put("objectId", objectId);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e.getCause());
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e.getCause());
        }
    }

    private void scripting_uploadFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String transactionId = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getScripting().uploadFile(transactionId, OutputStream.class).thenAccept(writer -> {
                    // Same implementation as for files_upload()
                    try {
                        String objectId = "" + System.identityHashCode(writer);
                        writerMap.put(objectId, writer);

                        JSONObject ret = new JSONObject();
                        ret.put("objectId", objectId);
                        callbackContext.success(ret);
                    } catch (JSONException e) {
                        enhancedError(callbackContext, e.getCause());
                    }
                }).exceptionally(e -> {
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e.getCause());
        }
    }

    private void writer_write(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);
        String base64encodedFromUint8Array = args.getString(1);

        new Thread(() -> {
            try {
                OutputStream writer = writerMap.get(writerObjectId);

                // Cordova encodes UInt8Array in TS to base64 encoded in java.
                byte[] data = Base64.decode(base64encodedFromUint8Array, Base64.DEFAULT);
                writer.write(data);

                callbackContext.success();
            }
            catch (IOException e) {
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void writer_flush(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);

        new Thread(() -> {
            try {
                OutputStream writer = writerMap.get(writerObjectId);
                writer.flush();
                callbackContext.success();
            }
            catch (IOException e) {
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void writer_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String writerObjectId = args.getString(0);

        new Thread(() -> {
            try {
                OutputStream writer = writerMap.get(writerObjectId);
                writer.close();
                writerMap.remove(writerObjectId);
                callbackContext.success();
            }
            catch (IOException e) {
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void reader_read(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);
        int bytesCount = args.getInt(1);

        new Thread(() -> {
            try {
                byte[] buffer = new byte[bytesCount];
                InputStream reader = readerMap.get(readerObjectId);

                // Resume reading at the previous read offset
                //int currentReadOffset = readerOffsetsMap.get(readerObjectId);
                int readBytes = reader.read(buffer, 0, bytesCount);

                if (readBytes != -1) {
                    // Move read offset to the next position
                    //readerOffsetsMap.put(readerObjectId, currentReadOffset + readBytes);
                    callbackContext.success(Base64.encodeToString(buffer, 0, readBytes, Base64.NO_WRAP));
                } else {
                    callbackContext.success((String) null);
                }

            } catch (IOException e) {
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void reader_readAll(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        new Thread(() -> {
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
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void reader_close(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String readerObjectId = args.getString(0);

        new Thread(() -> {
            try {
                InputStream reader = readerMap.get(readerObjectId);
                reader.close();
                readerMap.remove(readerObjectId);
                readerOffsetsMap.remove(readerObjectId);
                callbackContext.success();
            }
            catch (IOException e) {
                enhancedError(callbackContext, e);
            }
        }).start();
    }

    private void payment_getPricingInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getPaymentInfo().thenAccept(pricingInfo -> {
                    try {
                        callbackContext.success(new JSONObject(pricingInfo.serialize()));
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_getPricingPlan(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String pricingPlanName = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getPricingPlan(pricingPlanName).thenAccept(pricingPlan -> {
                    try {
                        callbackContext.success(new JSONObject(pricingPlan.serialize()));
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_placeOrder(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String pricingPlanName = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().placeOrder(pricingPlanName).thenAccept(orderId -> {
                    try {
                        callbackContext.success(orderId);
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_payOrder(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String orderId = args.getString(1);
        JSONArray transactionIDsJson = args.getJSONArray(2);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().payOrder(orderId, HivePluginHelper.JSONArrayToList(transactionIDsJson)).thenAccept(success -> {
                    try {
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        callbackContext.success(orderId);
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_getOrder(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);
        String orderId = args.getString(1);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getOrder(orderId).thenAccept(order -> {
                    try {
                        callbackContext.success(new JSONObject(order.serialize()));
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_getAllOrders(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getAllOrders().thenAccept(orders -> {
                    try {
                        JSONArray array = new JSONArray();
                        Iterator<Order> it = orders.iterator();
                        while (it.hasNext()) {
                            array.put(new JSONObject(it.next().serialize()));
                        }
                        callbackContext.success(array);
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_getActivePricingPlan(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getUsingPricePlan().thenAccept(activePlan -> {
                    try {
                        callbackContext.success(new JSONObject(activePlan.serialize()));
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
        }
    }

    private void payment_getPaymentVersion(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String vaultObjectId = args.getString(0);

        try {
            Vault vault = vaultMap.get(vaultObjectId);
            if (ensureValidVault(vault, callbackContext)) {
                vault.getPayment().getPaymentVersion().thenAccept(version -> {
                    try {
                        callbackContext.success(version);
                    }
                    catch (Exception e) {
                        enhancedError(callbackContext, e);
                    }
                }).exceptionally(e->{
                    enhancedError(callbackContext, e.getCause());
                    return null;
                });
            }
        }
        catch (Exception e) {
            enhancedError(callbackContext, e);
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
