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
import org.json.JSONException;
import org.json.JSONObject;

import org.elastos.hive.*;

class LoginHandler implements Authenticator {
    private final int handlerId;
    private final CallbackContext callbackContext;

    LoginHandler(int id, CallbackContext callbackContext) {
        this.handlerId = id;
        this.callbackContext = callbackContext;
    }

    private void sendEvent(JSONObject info) throws JSONException {
        info.put("id", handlerId);

        PluginResult res = new PluginResult(PluginResult.Status.OK, info);
        res.setKeepCallback(true);
        callbackContext.sendPluginResult(res);
    }

    @Override
    public void requestAuthentication(String requestUrl) {
        JSONObject ret = new JSONObject();
        try {
            ret.put("url", requestUrl);
            sendEvent(ret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}