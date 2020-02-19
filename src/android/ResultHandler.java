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
import org.elastos.hive.exception.HiveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.elastos.hive.*;

import java.util.ArrayList;

class ResultHandler<T> implements Callback<T> {
    private final Type type;
    private final int handlerId;
    private final CallbackContext callbackContext;

    ResultHandler(int id, Type type, CallbackContext callbackContext) {
        this.handlerId = id;
        this.callbackContext = callbackContext;
        this.type = type;
    }

    private void sendEvent(JSONObject info) throws JSONException {
        if (callbackContext != null) {
            info.put("hid", handlerId);
            PluginResult res = new PluginResult(PluginResult.Status.OK, info);
            res.setKeepCallback(true);
            callbackContext.sendPluginResult(res);
        }
    }

    @Override
    public void onError(HiveException ex) {
        try {
            sendEvent(errorJson(ex.getMessage()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(T result) {
        JSONObject ret = null;

        try {
            switch (type) {
                case Void:
                    ret = voidToJson();
                    break;
                case Length:
                    ret = lengthToJson((Long) result);
                    break;
                case Content:
                    ret = contentToJson((String) result);
                    break;
                case FileList:
                    ret = fileListToJson((ArrayList<String>) result);
                    break;
                case ValueList:
                    ret = valueListToJson((ArrayList<byte[]>) result);
                    break;
                case CID:
                    ret = cidToJSON((String) result);
                    break;
                default:
                    break;
            }
            if (ret == null)
                ret = errorJson("ret is null");

            sendEvent(ret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject voidToJson() throws JSONException {
        JSONObject ret = successJson();
        return ret;
    }

    private JSONObject lengthToJson(long length) throws JSONException {
        JSONObject ret = successJson();
        ret.put("length", length);
        return ret;
    }

    private JSONObject contentToJson(String content) throws JSONException {
        JSONObject ret = successJson();
        ret.put("content", content);
        return ret;
    }

    private JSONObject fileListToJson(ArrayList<String> fileList) throws JSONException {
        JSONObject ret = successJson();
        JSONArray array = new JSONArray(fileList);
        ret.put("fileList", array);
        return ret;
    }

    private JSONObject valueListToJson(ArrayList<byte[]> valueList) throws JSONException {
        JSONObject ret = successJson();
        JSONArray array = new JSONArray();
        for (byte[] value :valueList){
            array.put(new String(value));
        }
        ret.put("valueList", array);
        return ret;
    }
    private JSONObject cidToJSON(String cid) throws JSONException {
        JSONObject ret = successJson();
        ret.put("cid", cid);
        return ret;
    }
    private JSONObject successJson() throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("status", "success");
        return ret;
    }

    private JSONObject errorJson(String errorMsg) throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("status", "error");
        ret.put("error", errorMsg);
        return ret;

    }

    enum Type {
        Void,
        Length,
        Content,
        CID,
        FileList,
        ValueList
    }
}
