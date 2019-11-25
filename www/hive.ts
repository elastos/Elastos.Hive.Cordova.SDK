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

class FileImpl implements HivePlugin.File  {
    objId  = null;
    plugin = null;
    clazz  = 4;

    getLastInfo(onSuccess: (info: any)=>void, onError?: (err: string)=>void) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    }

    getInfo(onSuccess: (info: any) => void, onError?: (err: string) => void): Promise<any> {
        return this.plugin.getPromise(this, 'getInfo', []);
    }

    moveTo(destPath: string): Promise<any> {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    }

    copyTo(newPath: string): Promise<any> {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    }

    deleteItem(): Promise<any> {
        return this.plugin.getPromise(this, 'deleteItem', []);
    }

    readData(length: HivePlugin.Int): Promise<any> {
        return this.plugin.getPromise(this, 'readData', [length]);
    }
    
    writeData(data: any): Promise<any> {
        return this.plugin.getPromise(this, 'writeData', [data]);
    }

    commit(): Promise<any> {
        return this.plugin.getPromise(this, 'commitData', []);
    }

    discard(onSuccess?: () => void) {
        exec(onSuccess, null, 'HivePlugin', 'discardData', [this.clazz, this.objId]);
    }
}

class DirectoryImpl implements HivePlugin.Directory {
    objId  = null;
    plugin = null;
    clazz  = 3;

    getLastInfo(onSuccess: (info: any)=>void, onError?: (err: string)=>void) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    }
    getInfo(): Promise<any> {
        return this.plugin.getPromise(this, 'getInfo', []);
    }
    createDirectory(name: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [name]).then(
            function(ret) {
                var directory = new DirectoryImpl();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    }
    getDirectory(name: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [name]).then(
            function(ret) {
                var directory = new DirectoryImpl();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    }
    createFile(name: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [name]).then(
            function(ret) {
                var file = new FileImpl();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    }
    getFile(name: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [name]).then(
            function(ret) {
                var file = new FileImpl();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    }
    getChildren(): Promise<any> {
        return this.plugin.getPromise(this, 'getChildren', []).then(
            function(ret) {
                return ret.items;
            }
        );
    }
    moveTo(destPath: string): Promise<any> {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    }
    copyTo(newPath: string): Promise<any> {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    }
    deleteItem(): Promise<any> {
        return this.plugin.getPromise(this, 'deleteItem', []);
    }
}
class DriveImpl implements HivePlugin.Drive {
    objId  = null;
    plugin = null;
    clazz  = 2;

    getLastInfo(onSuccess: (info: any) => void, onError?: (err: string) => void) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    }
    getInfo(): Promise<any> {
        return this.plugin.getPromise(this, 'getInfo', []);
    }
    rootDirectory(): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'rootDir', []).then(
            function(ret) {
                var directory = new DirectoryImpl();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    }
    createDirectory(path: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [path]).then(
            function(ret) {
                var directory = new DirectoryImpl();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    }
    getDirectory(path: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [path]).then(
            function(ret) {
                var directory = new DirectoryImpl();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    }
    createFile(path: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [path]).then(
            function(ret) {
                var file = new FileImpl();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    }
    getFile(path: string): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [path]).then(
            function(ret) {
                var file = new FileImpl();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    }
    getItemInfo(path: string): Promise<any> {
        return this.plugin.getPromise(this, 'getItemInfo', [path]);
    }
}

class ClientImpl implements HivePlugin.Client {
    objId  = null;
    plugin = null;
    clazz  = 1;

    login(handler: Function, onSuccess?: () => void, onError?: (err: string) => void) {
        var handlerId = this.plugin.addLoginRequestCb(handler);
        exec(onSuccess, onError, 'HivePlugin', 'login', [this.clazz, this.objId, handlerId]);
    }
    logout(onSuccess?: () => void, onError?: (err: string) => void) {
        exec(onSuccess, onError, 'HivePlugin', 'logout', [this.clazz, this.objId]);
    }
    getLastInfo(onSuccess?: (info: any) => void, onError?: (err: string) => void) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    }
    getInfo(): Promise<any> {
        return this.plugin.getPromise(this, 'getInfo', []);
    }
    getDefDrive(): Promise<any> {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDefDrive', []).then(
            function (ret) {
                var drive = new DriveImpl();
                drive.objId = ret.id;
                drive.plugin = plugin;
                return drive;
            }
        );
    }
}

const LISTENER_LOGIN  = 1;
const LISTENER_RESULT = 2;

type HivePluginEvent = {
    callback: Function;
    object: any;
};

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
        Object.freeze(DriveImpl.prototype);
        Object.freeze(DirectoryImpl.prototype);
        Object.freeze(FileImpl.prototype);

        this.setListener(LISTENER_LOGIN,  this.onLoginRequest);
        this.setListener(LISTENER_RESULT, this.onResultEvent);    
    
        exec(function () {}, null, 'HivePlugin', 'initVal', []);
    }

    addLoginRequestCb(callback) {
        var eventcb: HivePluginEvent;
        eventcb.callback = callback;

        this.loginEvent = eventcb;
        return 0;
    }

    onLoginRequest(event) {
        var id = event.id;
        if (id == 0) {
            this.loginEvent.callback(event.url);
        }
    }

    addResultEventCb = function(callback, object) {
        var eventcb: HivePluginEvent;
        eventcb.callback = callback;
        eventcb.object = object;

        this.resultIndex++;
        this.resultEvent[this.resultIndex] = eventcb;
        return this.resultIndex;
    }

    onResultEvent(event) {
        var id = event.hid;
        event.hid = null;

        if (this.resultEvent[id].callback)  {
            this.resultEvent[id].callback(event);
        }
    }

    getPromise(object, method, args) {
        var me = this;
        return new Promise(function(resolve, reject) {
            var onResult = function(ret) {
                if (null == ret.error)
                    resolve(ret);
                else
                    reject(ret.error);
            };

            var _args = [
                object.clazz,
                object.objId,
                me.addResultEventCb(onResult, object),
            ];

            exec(null, null, 'HivePlugin', method, _args.concat(args));
        });
    }

    getVersion(onSuccess?: ()=>void, onError?: (err: string)=>void) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', [])
    }

    setListener(type: any, eventCallback: Function) {
        exec(eventCallback, null, 'HivePlugin', 'setListener', [type]);
    }
    createClient(options: any, onSuccess: (client: ClientImpl) => void, onError?: (err: string) => void) {
        var client = new ClientImpl();
        var me = this;

        var _onSuccess = function(ret) {
            client.objId = ret.id;
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
        exec(_onSuccess, onError, 'HivePlugin', 'createClient', ["im", configStr]);
    }
}

export = new HiveManagerImpl();
