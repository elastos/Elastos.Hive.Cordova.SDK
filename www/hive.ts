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

var exec = cordova.exec;

class IPFSImpl implements HivePlugin.IPFS  {
    objId  = null;
    plugin = null;
    clazz  = 4;

    put(data: string): Promise<any> {
        return this.plugin.getPromise(this, 'putStringByIPFS', [this.objId, data]);
    }

    get(cid: string): Promise<any> {
        return this.plugin.getPromise(this, 'getStringByIPFS', [this.objId, cid]);
    }

    size(cid: string): Promise<any> {
        return this.plugin.getPromise(this, 'getSizeByIPFS', [this.objId, cid]);
    }
}

class FilesImpl implements HivePlugin.Files  {
    objId  = null;
    plugin = null;
    clazz  = 3;

    put(remoteFile: string, data: string): Promise<any> {
        return this.plugin.getPromise(this, 'putStringByFiles', [this.objId, remoteFile, data]);
    }

    getAsString(remoteFile: string): Promise<any> {
        return this.plugin.getPromise(this, 'getStringByFiles', [this.objId, remoteFile]);
    }

    size(remoteFile: string): Promise<any> {
        return this.plugin.getPromise(this, 'getSizeByFiles', [this.objId, remoteFile]);
    }

    deleteFile(remoteFile: string): Promise<any> {
        return this.plugin.getPromise(this, 'deleteFileByFiles', [this.objId, remoteFile]);
    }

    list(): Promise<any> {
        return this.plugin.getPromise(this, 'listFilesByFiles', [this.objId]);
    }
}

class KeyValuesImpl implements HivePlugin.KeyValues  {
    objId  = null;
    plugin = null;
    clazz  = 2;

    putValue(key: string, value: string): Promise<any> {
        return this.plugin.getPromise(this, 'putValueByKV', [this.objId, key, value]);
    }

    setValue(key: string, value: string): Promise<any> {
        return this.plugin.getPromise(this, 'setValueByKV', [this.objId, key, value]);
    }

    getValues(key: string): Promise<any> {
        return this.plugin.getPromise(this, 'getValuesByKV', [this.objId, key]);
    }

    deleteKey(key: string): Promise<any> {
        return this.plugin.getPromise(this, 'deleteKeyByKV', [this.objId, key]);
    }
}

class ClientImpl implements HivePlugin.Client {
    objId  = null;
    plugin = null;
    clazz  = 1;

    ipfs = [];
    files = [];
    keyValues = [];

    connect(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            if (onSuccess)
                onSuccess(ret.status);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'connect', [this.objId]);
    }

    disConnect(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            if (onSuccess)
                onSuccess(ret.status);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'disConnect', [this.objId]);
    }

    isConnected(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            if (onSuccess)
                onSuccess(ret.isConnect);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'isConnected', [this.objId]);
    }

    getIPFS(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            var ipfs = new IPFSImpl();
            ipfs.objId = ret.ipfsId;
            ipfs.plugin = me.plugin;
            me.ipfs[ipfs.objId] = ipfs;
            if (onSuccess)
                onSuccess(ipfs);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'getIPFS', [this.objId]);
    }

    getFiles(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            var files = new FilesImpl();
            files.objId = ret.filesId;
            files.plugin = me.plugin;
            me.files[files.objId] = files;
            if (onSuccess)
                onSuccess(files);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'getFiles', [this.objId]);
    }

    getKeyValues(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        var me = this;
        var _onSuccess = function (ret) {
            var keyValues = new KeyValuesImpl();
            keyValues.objId = ret.keyValuesId;
            keyValues.plugin = me.plugin;
            me.keyValues[keyValues.objId] = keyValues;
            if (onSuccess)
                onSuccess(keyValues);
        };
        exec(_onSuccess, onError, 'HivePlugin', 'getKeyValues', [this.objId]);
    }
}

const LISTENER_LOGIN  = 1;
const LISTENER_RESULT = 2;

type HivePluginEvent = {
    callback: Function;
    object: any;
};

class IPFSClientCreationOptions implements HivePlugin.IPFSClientCreationOptions {
    driveType: HivePlugin.DriveType.IPFS;
}

class OneDriveClientCreationOptions implements HivePlugin.IPFSClientCreationOptions {
    driveType = HivePlugin.DriveType.ONEDRIVE;
    clientId: string;
    redirectUrl: string;

    constructor(clientId: string, redirectUrl: string) {
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
    }
}

class HiveManagerImpl implements HivePlugin.HiveManager {
    clients = [];

    resultIndex = 0;
    resultEvent: HivePluginEvent[] = [];
    loginCount  = 0;
    loginRequest = [];
    loginEvent: HivePluginEvent;

    constructor() {
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(ClientImpl.prototype);
        Object.freeze(IPFSImpl.prototype);
        Object.freeze(FilesImpl.prototype);
        Object.freeze(KeyValuesImpl.prototype);

        this.setListener(LISTENER_LOGIN, (event) => {
            var id = event.id;
            if (id == 0) {
                this.loginEvent.callback(event.url);
            }
        });

        this.setListener(LISTENER_RESULT, (event) => {
            var id = event.hid;
            event.hid = null;

            if (this.resultEvent[id].callback)  {
                this.resultEvent[id].callback(event);
            }
        });

        exec(function () {}, null, 'HivePlugin', 'initVal', []);
    }

    addLoginRequestCb(callback) {
        var eventcb: HivePluginEvent = {
            callback: callback,
            object: null
        };

        this.loginEvent = eventcb;
        return 0;
    }

    addResultEventCb = function(callback, object) {
        var eventcb: HivePluginEvent = {
            callback: callback,
            object: object
        }

        this.resultIndex++;
        this.resultEvent[this.resultIndex] = eventcb;
        return this.resultIndex;
    }

    getPromise(object, method, args): Promise<any> {
        var me = this;
        return new Promise(function(resolve, reject) {
            var onResult = function(ret) {
                if (null == ret.error)
                    resolve(ret);
                else
                    reject(ret.error);
            };
            var _args = args;
            _args.push(me.addResultEventCb(onResult, object))
            exec(null, null, 'HivePlugin', method, _args);
        });
    }

    getVersion(onSuccess?: ()=>void, onError?: (err: string)=>void) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', [])
    }

    setListener(type: any, eventCallback: Function) {
        exec(eventCallback, null, 'HivePlugin', 'setListener', [type]);
    }

    createClient(handler: Function, options: HivePlugin.ClientCreationOptions, onSuccess: (client: ClientImpl) => void, onError?: (err: string) => void) {
        var client = new ClientImpl();
        var me = this;

        var _onSuccess = function(ret) {
            client.objId = ret.clientId;
            client.plugin = me;
            me.clients[client.objId] = client;
            if (onSuccess)
                onSuccess(client);
        }

        if (typeof (options) == "undefined" || options == null ||
            typeof (options.driveType) != "string") {
            if (onError) {
                onError("invalid options");
            }
            return;
        }

        var configStr = JSON.stringify(options);
        var handlerId = this.addLoginRequestCb(handler);
        exec(_onSuccess, onError, 'HivePlugin', 'createClient', ["im", configStr, handlerId]);
    }
}

export = new HiveManagerImpl();
