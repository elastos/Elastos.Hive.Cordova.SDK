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
import org.elastos.hive.HiveException;
import org.elastos.hive.vendors.onedrive.OneDriveParameter;

class ClientBuilder {
    private static String TAG = "ClientBuilder";

    private static final int NATIVE    = 1;
    private static final int ONEDRIVE  = 2;
    private static final int IPFS      = 3;

    private static Client createForOneDrive(String dir, JSONObject json) throws JSONException, HiveException {
        OAuthEntry entry = new OAuthEntry(
            json.getString("client_id"),
            json.getString("scope"),
            json.getString("redirect_url")
        );
        return Client.createInstance(new OneDriveParameter(entry, dir));
    }

    private static Client createForIPFS(String dir, JSONObject json) throws JSONException {
        // TODO;
        return null;
    }

    static Client create(String dir, String options) throws JSONException {
        JSONObject jsonObject = new JSONObject(options);
        int type = jsonObject.getInt("type");

        Client client = null;
        try {
            switch(type) {
            case ONEDRIVE:
                client = createForOneDrive(dir, jsonObject);
                break;

            case IPFS:
                client = createForIPFS(dir, jsonObject);
                break;
            }
        } catch (HiveException e) {
        }

        return client;
    }
}