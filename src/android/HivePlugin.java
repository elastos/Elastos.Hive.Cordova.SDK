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
                case "putStringByFiles":
                    this.putStringByFiles(args, callbackContext);
                    break;
                case "getStringByFiles":
                    this.getStringByFiles(args, callbackContext);
                    break;
                case "getSizeByFiles":
                    this.getSizeByFiles(args, callbackContext);
                    break;
                case "deleteFileByFiles":
                    this.deleteFileByFiles(args, callbackContext);
                    break;
                case "listFilesByFiles":
                    this.listFilesByFiles(args, callbackContext);
                    break;
                case "putStringByIPFS":
                    this.putStringByIPFS(args, callbackContext);
                    break;
                case "getStringByIPFS":
                    this.getStringByIPFS(args, callbackContext);
                    break;
                case "getSizeByIPFS":
                    this.getSizeByIPFS(args, callbackContext);
                    break;
                case "putValueByKV":
                    this.putValueByKV(args, callbackContext);
                    break;
                case "setValueByKV":
                    this.setValueByKV(args, callbackContext);
                    break;
                case "getValuesByKV":
                    this.getValuesByKV(args, callbackContext);
                    break;
                case "deleteKeyByKV":
                    this.deleteKeyByKV(args, callbackContext);
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
        String options = args.getString(1);
        int  handlerId = args.getInt(2);

        java.io.File dirFile = new java.io.File(dataDir);
        if (!dirFile.exists())
            dirFile.mkdirs();

        try {
            Client client = ClientBuilder.createClient(dataPath, options, this, new LoginHandler(handlerId, loginCallbackCtxt));
            int  clientId = System.identityHashCode(client);
            hiveClientMap.put(clientId, client);

            JSONObject ret = new JSONObject();
            ret.put("clientId", clientId);

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
                callbackContext.error(e.getLocalizedMessage());
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
        int ipfsId = System.identityHashCode(ipfs);
        ipfsMap.put(ipfsId,ipfs);

        JSONObject ret = new JSONObject();
        ret.put("ipfsId", ipfsId);
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


    private void putStringByFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);
        String data = args.getString(2);
        int handlerId = args.getInt(3);

        Files api = filesMap.get(filesId);
        api.put(data, remoteFile, createResultHandler(handlerId, ResultHandler.Type.Void));
    }

    private void getStringByFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);
        int handlerId = args.getInt(2);

        Files api = filesMap.get(filesId);
        api.getAsString(remoteFile, createResultHandler(handlerId, ResultHandler.Type.Content));
    }

    private void getSizeByFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);
        int handlerId = args.getInt(2);

        Files api = filesMap.get(filesId);
        api.size(remoteFile, createResultHandler(handlerId, ResultHandler.Type.Length));
    }

    private void deleteFileByFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        String remoteFile = args.getString(1);
        int handlerId = args.getInt(2);

        Files api = filesMap.get(filesId);
        api.delete(remoteFile, createResultHandler(handlerId, ResultHandler.Type.Void));
    }

    private void listFilesByFiles(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int filesId = args.getInt(0);
        int handlerId = args.getInt(1);

        Files api = filesMap.get(filesId);
        api.list(createResultHandler(handlerId, ResultHandler.Type.FileList));
    }

    private void putStringByIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String data = args.getString(1);
        int handlerId = args.getInt(2);

        IPFS api = ipfsMap.get(ipfsId);
        api.put(data, createResultHandler(handlerId, ResultHandler.Type.CID));
    }

    private void getStringByIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String cid = args.getString(1);
        int handlerId = args.getInt(2);

        IPFS api = ipfsMap.get(ipfsId);
        api.getAsString(cid, createResultHandler(handlerId, ResultHandler.Type.Content));
    }

    private void getSizeByIPFS(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int ipfsId = args.getInt(0);
        String cid = args.getString(1);
        int handlerId = args.getInt(2);

        IPFS api = ipfsMap.get(ipfsId);
        api.size(cid, createResultHandler(handlerId, ResultHandler.Type.Length));
    }

    private void putValueByKV(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        String value = args.getString(2);
        int handlerId = args.getInt(3);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.putValue(key, value, createResultHandler(handlerId, ResultHandler.Type.Void));
    }

    private void setValueByKV(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        String value = args.getString(2);
        int handlerId = args.getInt(3);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.setValue(key, value, createResultHandler(handlerId, ResultHandler.Type.Void));
    }

    private void getValuesByKV(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        int handlerId = args.getInt(2);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.getValues(key, createResultHandler(handlerId, ResultHandler.Type.ValueList));
    }

    private void deleteKeyByKV(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int keyValuesId = args.getInt(0);
        String key = args.getString(1);
        int handlerId = args.getInt(2);

        KeyValues api = keyValuesMap.get(keyValuesId);
        api.deleteKey(key, createResultHandler(handlerId, ResultHandler.Type.Void));
    }

    private ResultHandler createResultHandler(int handlerId, ResultHandler.Type type) {
        return new ResultHandler(handlerId, type, resultCallbackCtxt);
    }
}
