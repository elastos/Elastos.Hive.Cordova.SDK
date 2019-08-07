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

import org.json.JSONException;
import org.json.JSONObject;

import org.elastos.hive.*;

class JSONObjectHolder<T extends Result> {
    private JSONObject jsonObject;
    private final T result;

    JSONObjectHolder(T result) {
        this.jsonObject = new JSONObject();
        this.result = result;
    }

    JSONObject get() {
        return jsonObject;
    }

    JSONObjectHolder put() {
        try {
            jsonObject.put("void", "Void");
        } catch(JSONException e) {
        }

        return this;
    }

    JSONObjectHolder put(String propId) {
        try {
            if (result instanceof Client.Info)
                put(propId, (Client.Info)result);
            else if (result instanceof Drive.Info)
                put(propId, (Drive.Info)result);
            else if (result instanceof Directory.Info)
                put(propId, (Directory.Info)result);
            else if (result instanceof File.Info)
                put(propId, (File.Info)result);
            else if (result instanceof ItemInfo)
                put(propId, (ItemInfo)result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    private void put(String propId, Client.Info info) throws JSONException {
        if (info.containsKey(propId))
            jsonObject.put(propId, info.get(propId));
    }

    private void put(String propId, Drive.Info info) throws JSONException {
        if (info.containsKey(propId))
            jsonObject.put(propId, info.get(propId));
    }

    private void put(String propId, Directory.Info info) throws JSONException {
        if (info.containsKey(propId))
            jsonObject.put(propId, info.get(propId));
    }

    private void put(String propId, File.Info info) throws JSONException {
        if (info.containsKey(propId))
            jsonObject.put(propId, info.get(propId));
    }

    private void put(String propId, ItemInfo info) throws JSONException {
        if (info.containsKey(propId))
            jsonObject.put(propId, info.get(propId));
    }
}
