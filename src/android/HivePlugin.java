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
import org.elastos.hive.interfaces.Files;
import org.elastos.hive.interfaces.IPFS;
import org.elastos.hive.interfaces.KeyValues;
import org.elastos.trinity.runtime.TrinityPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class


HivePlugin extends TrinityPlugin {
    private static final int LOGIN = 1;
    private static final int RESULT = 2;

    private HashMap<Integer, Client> hiveClientMap = new HashMap<>();
    private HashMap<Integer, IPFS> ipfsMap = new HashMap<>();
    private HashMap<Integer, Files> filesMap = new HashMap<>();
    private HashMap<Integer, KeyValues> keyValuesMap = new HashMap<>();


    private static final String SUCCESS = "Success!";
    private static final String INVALID_ID = "Id invalid!";

    private CallbackContext loginCallbackCtxt = null;
    private CallbackContext resultCallbackCtxt = null;

    private int resultId = 0;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "getVersion":
                    this.getVersion(args, callbackContext);
                    break;
                case "setListener":
                    this.setListener(args, callbackContext);
                    break;
                case "createClient":
                    this.createClient(args, callbackContext);
                case "connect":
                    this.connect(args, callbackContext);
                    break;
                case "disconnect":
                    this.disConnect(args, callbackContext);
                    break;
                case "isConnected":
                    this.isConnected(args, callbackContext);
                    break;
                case "getIPFS":
                    this.getIPFS(args, callbackContext);
                    break;
                case "getFiles":
                    this.getFiles(args, callbackContext);
                    break;
                case "getKeyValues":
                    this.getKeyValues(args, callbackContext);
                    break;
                case "putStringForFiles":
                    this.putStringForFiles(args, callbackContext);
                    break;
                case "getAsStringForFiles":
                    this.getAsStringForFiles(args, callbackContext);
                    break;
                case "sizeForFiles":
                    this.sizeForFiles(args, callbackContext);
                    break;
                case "deleteForFiles":
                    this.deleteForFiles(args, callbackContext);
                    break;
                case "listForFiles":
                    this.listForFiles(args, callbackContext);
                    break;
                case "putStringIPFS":
                    this.putStringIPFS(args, callbackContext);
                    break;
                case "getAsStringIPFS":
                    this.getAsStringIPFS(args, callbackContext);
                    break;
                case "getSizeIPFS":
                    this.getSizeIPFS(args, callbackContext);
                    break;
                case "putValue":
                    this.putValue(args, callbackContext);
                    break;
                case "setValue":
                    this.setValue(args, callbackContext);
                    break;
                case "getValues":
                    this.getValues(args, callbackContext);
                    break;
                case "deleteKey":
                    this.deleteKey(args, callbackContext);
                    break;
                default:
                    return false;
            }
        } catch (JSONException e) {
            callbackContext.error(e.getLocalizedMessage());
        }
        return true;
    }

    private void getVersion(JSONArray args, CallbackContext callbackContext) {
        String version = "ElastosHiveSDK-v0.2";
        callbackContext.success(version);
    }

    private void setListener(JSONArray args, CallbackContext callbackContext) throws JSONException {
        Integer type = args.getInt(0);

        switch (type) {
            case LOGIN:
                loginCallbackCtxt = callbackContext;
                break;

            case RESULT:
                resultCallbackCtxt = callbackContext;
                break;
        }

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void createClient(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String dataDir = cordova.getActivity().getFilesDir() + "/data/hive/" + args.getString(0);
        String options = args.getString(0);
        int handlerId = args.getInt(1);
        java.io.File dirFile = new java.io.File(dataDir);
        if (!dirFile.exists()) dirFile.mkdirs();
        try {
            Client client = ClientBuilder.createClient(dataPath, options, new LoginHandler(handlerId, loginCallbackCtxt));
            int clientObjId = System.identityHashCode(client);
            hiveClientMap.put(clientObjId, client);
            JSONObject ret = new JSONObject();
            ret.put("clientId", clientObjId);

            callbackContext.success(ret);
        } catch (Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void isConnected(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);
        boolean isConnect = client.isConnected();
        JSONObject ret = new JSONObject();
        ret.put("isConnect", isConnect);
        callbackContext.success(ret);
    }

    private void connect(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);
        new Thread(() -> {
            try {
                client.connect();
                JSONObject ret = new JSONObject();
                ret.put("status","success");
                callbackContext.success(ret);
            } catch (Exception e) {
                callbackContext.success(e.getLocalizedMessage());
            }
        }).start();
    }

    private void disConnect(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);
        client.disconnect();
        JSONObject ret = new JSONObject();
        ret.put("status","success");
        callbackContext.success(ret);
    }

    private void getIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);

        IPFS ipfs = client.getIPFS();

        int ipfsObjId = System.identityHashCode(ipfs);

        ipfsMap.put(ipfsObjId,ipfs);

        JSONObject ret = new JSONObject();
        ret.put("ipfsId", ipfsObjId);
        callbackContext.success(ret);
    }

    private void getFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);
        Files files = client.getFiles();
        int filesObjId = System.identityHashCode(files);

        filesMap.put(filesObjId,files);

        JSONObject ret = new JSONObject();
        ret.put("filesId", filesObjId);
        callbackContext.success(ret);
    }

    private void getKeyValues(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int clientId = args.getInt(0);
        Client client = hiveClientMap.get(clientId);
        KeyValues keyValues = client.getKeyValues();
        int keyValuesObjId = System.identityHashCode(keyValues);


        keyValuesMap.put(keyValuesObjId,keyValues);

        JSONObject ret = new JSONObject();
        ret.put("keyValuesId", keyValuesObjId);
        callbackContext.success(ret);
    }


    private void putStringForFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);
        String data = args.getString(2);

        Files api = filesMap.get(filesId);
        api.put(data, remoteFile, crateResultHandler(ResultHandler.Type.Void));
    }

    private void getAsStringForFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);

        Files api = filesMap.get(filesId);
        api.getAsString(remoteFile, crateResultHandler(ResultHandler.Type.Content));
    }

    private void sizeForFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);

        Files api = filesMap.get(filesId);
        api.size(remoteFile, crateResultHandler(ResultHandler.Type.Length));
    }

    private void deleteForFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);

        Files api = filesMap.get(filesId);
        api.delete(remoteFile, crateResultHandler(ResultHandler.Type.Void));
    }

    private void listForFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);

        Files api = filesMap.get(filesId);
        api.list(crateResultHandler(ResultHandler.Type.FileList));
    }

    private void putStringIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String data = args.getString(1);

        IPFS api = ipfsMap.get(ipfsId);
        api.put(data, crateResultHandler(ResultHandler.Type.CID));
    }

    private void getAsStringIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String cid = args.getString(1);

        IPFS api = ipfsMap.get(ipfsId);
        api.getAsString(cid, crateResultHandler(ResultHandler.Type.Content));
    }

    private void getSizeIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String cid = args.getString(1);

        IPFS api = ipfsMap.get(ipfsId);
        api.size(cid, crateResultHandler(ResultHandler.Type.Length));
    }

    private void putValue(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        String value = args.getString(2);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.putValue(key, value, crateResultHandler(ResultHandler.Type.Void));
    }

    private void setValue(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        String value = args.getString(2);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.setValue(key, value, crateResultHandler(ResultHandler.Type.Void));
    }

    private void getValues(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.getValues(key, crateResultHandler(ResultHandler.Type.ValueList));
    }

    private void deleteKey(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.deleteKey(key, crateResultHandler(ResultHandler.Type.Void));
    }

    private ResultHandler crateResultHandler(ResultHandler.Type type) {
        resultId ++;
        return new ResultHandler(resultId, type, resultCallbackCtxt);
    }
}
