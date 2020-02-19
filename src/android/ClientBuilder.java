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

import org.elastos.hive.exception.HiveException;
import org.elastos.hive.vendor.ipfs.IPFSOptions;
import org.elastos.hive.vendor.onedrive.OneDriveOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.elastos.hive.*;

class ClientBuilder {
    private static String TAG = "ClientBuilder";

    private static final int NATIVE = 1;
    private static final int ONEDRIVE = 2;
    private static final int IPFS = 3;

    private static Client.Options createOneDriveOptions(String storePath, String jsonStr, Authenticator authenticator) throws JSONException, HiveException {
        JSONObject json = new JSONObject(jsonStr);
        String clientId = json.getString("clientId");
        String redirectUrl = json.getString("redirectUrl");
        return new OneDriveOptions
                .Builder()
                .setStorePath(storePath)
                .setClientId(clientId)
                .setRedirectUrl(redirectUrl)
                .setAuthenticator(authenticator)
                .build();
    }

    private static Client.Options createIPFSOptions(String storePath, String jsonStr) throws JSONException, HiveException {
        IPFSOptions.Builder options = new IPFSOptions.Builder().setStorePath(storePath);
        JSONObject json = new JSONObject(jsonStr);
        JSONArray array = json.getJSONArray("nodes");
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            options.addRpcNode(new IPFSOptions.RpcNode(item.getString("ip"), item.getInt("port")));
        }
        return options.build();
    }

    static Client createClient(String storePath, String options, Authenticator authenticator) throws Exception {
        JSONObject jsonObject = new JSONObject(options);
        int type = jsonObject.getInt("driveType");
        Client client;

        switch (type) {
            case ONEDRIVE:
                client = Client.createInstance(createOneDriveOptions(storePath, options, authenticator));
                break;

            case IPFS:
                client = Client.createInstance(createIPFSOptions(storePath, options));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        return client;
    }
}