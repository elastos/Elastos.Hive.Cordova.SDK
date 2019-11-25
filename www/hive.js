"use strict";
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
var FileImpl = /** @class */ (function () {
    function FileImpl() {
        this.objId = null;
        this.plugin = null;
        this.clazz = 4;
    }
    FileImpl.prototype.getLastInfo = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    };
    FileImpl.prototype.getInfo = function (onSuccess, onError) {
        return this.plugin.getPromise(this, 'getInfo', []);
    };
    FileImpl.prototype.moveTo = function (destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    };
    FileImpl.prototype.copyTo = function (newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    };
    FileImpl.prototype.deleteItem = function () {
        return this.plugin.getPromise(this, 'deleteItem', []);
    };
    FileImpl.prototype.readData = function (length) {
        return this.plugin.getPromise(this, 'readData', [length]);
    };
    FileImpl.prototype.writeData = function (data) {
        return this.plugin.getPromise(this, 'writeData', [data]);
    };
    FileImpl.prototype.commit = function () {
        return this.plugin.getPromise(this, 'commitData', []);
    };
    FileImpl.prototype.discard = function (onSuccess) {
        exec(onSuccess, null, 'HivePlugin', 'discardData', [this.clazz, this.objId]);
    };
    return FileImpl;
}());
var DirectoryImpl = /** @class */ (function () {
    function DirectoryImpl() {
        this.objId = null;
        this.plugin = null;
        this.clazz = 3;
    }
    DirectoryImpl.prototype.getLastInfo = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    };
    DirectoryImpl.prototype.getInfo = function () {
        return this.plugin.getPromise(this, 'getInfo', []);
    };
    DirectoryImpl.prototype.createDirectory = function (name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [name]).then(function (ret) {
            var directory = new DirectoryImpl();
            directory.objId = ret.id;
            directory.plugin = plugin;
            return directory;
        });
    };
    DirectoryImpl.prototype.getDirectory = function (name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [name]).then(function (ret) {
            var directory = new DirectoryImpl();
            directory.objId = ret.id;
            directory.plugin = plugin;
            return directory;
        });
    };
    DirectoryImpl.prototype.createFile = function (name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [name]).then(function (ret) {
            var file = new FileImpl();
            file.objId = ret.id;
            file.plugin = plugin;
            return file;
        });
    };
    DirectoryImpl.prototype.getFile = function (name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [name]).then(function (ret) {
            var file = new FileImpl();
            file.objId = ret.id;
            file.plugin = plugin;
            return file;
        });
    };
    DirectoryImpl.prototype.getChildren = function () {
        return this.plugin.getPromise(this, 'getChildren', []).then(function (ret) {
            return ret.items;
        });
    };
    DirectoryImpl.prototype.moveTo = function (destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    };
    DirectoryImpl.prototype.copyTo = function (newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    };
    DirectoryImpl.prototype.deleteItem = function () {
        return this.plugin.getPromise(this, 'deleteItem', []);
    };
    return DirectoryImpl;
}());
var DriveImpl = /** @class */ (function () {
    function DriveImpl() {
        this.objId = null;
        this.plugin = null;
        this.clazz = 2;
    }
    DriveImpl.prototype.getLastInfo = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    };
    DriveImpl.prototype.getInfo = function () {
        return this.plugin.getPromise(this, 'getInfo', []);
    };
    DriveImpl.prototype.rootDirectory = function () {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'rootDir', []).then(function (ret) {
            var directory = new DirectoryImpl();
            directory.objId = ret.id;
            directory.plugin = plugin;
            return directory;
        });
    };
    DriveImpl.prototype.createDirectory = function (path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [path]).then(function (ret) {
            var directory = new DirectoryImpl();
            directory.objId = ret.id;
            directory.plugin = plugin;
            return directory;
        });
    };
    DriveImpl.prototype.getDirectory = function (path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [path]).then(function (ret) {
            var directory = new DirectoryImpl();
            directory.objId = ret.id;
            directory.plugin = plugin;
            return directory;
        });
    };
    DriveImpl.prototype.createFile = function (path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [path]).then(function (ret) {
            var file = new FileImpl();
            file.objId = ret.id;
            file.plugin = plugin;
            return file;
        });
    };
    DriveImpl.prototype.getFile = function (path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [path]).then(function (ret) {
            var file = new FileImpl();
            file.objId = ret.id;
            file.plugin = plugin;
            return file;
        });
    };
    DriveImpl.prototype.getItemInfo = function (path) {
        return this.plugin.getPromise(this, 'getItemInfo', [path]);
    };
    return DriveImpl;
}());
var ClientImpl = /** @class */ (function () {
    function ClientImpl() {
        this.objId = null;
        this.plugin = null;
        this.clazz = 1;
    }
    ClientImpl.prototype.login = function (handler, onSuccess, onError) {
        var handlerId = this.plugin.addLoginRequestCb(handler);
        exec(onSuccess, onError, 'HivePlugin', 'login', [this.clazz, this.objId, handlerId]);
    };
    ClientImpl.prototype.logout = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'logout', [this.clazz, this.objId]);
    };
    ClientImpl.prototype.getLastInfo = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    };
    ClientImpl.prototype.getInfo = function () {
        return this.plugin.getPromise(this, 'getInfo', []);
    };
    ClientImpl.prototype.getDefDrive = function () {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDefDrive', []).then(function (ret) {
            var drive = new DriveImpl();
            drive.objId = ret.id;
            drive.plugin = plugin;
            return drive;
        });
    };
    return ClientImpl;
}());
var LISTENER_LOGIN = 1;
var LISTENER_RESULT = 2;
var HiveManagerImpl = /** @class */ (function () {
    function HiveManagerImpl() {
        this.clients = [];
        this.resultIndex = 0;
        this.resultEvent = [];
        this.loginCount = 0;
        this.loginRequest = [];
        this.addResultEventCb = function (callback, object) {
            var eventcb;
            eventcb.callback = callback;
            eventcb.object = object;
            this.resultIndex++;
            this.resultEvent[this.resultIndex] = eventcb;
            return this.resultIndex;
        };
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(ClientImpl.prototype);
        Object.freeze(DriveImpl.prototype);
        Object.freeze(DirectoryImpl.prototype);
        Object.freeze(FileImpl.prototype);
        this.setListener(LISTENER_LOGIN, this.onLoginRequest);
        this.setListener(LISTENER_RESULT, this.onResultEvent);
        exec(function () { }, null, 'HivePlugin', 'initVal', []);
    }
    HiveManagerImpl.prototype.addLoginRequestCb = function (callback) {
        var eventcb;
        eventcb.callback = callback;
        this.loginEvent = eventcb;
        return 0;
    };
    HiveManagerImpl.prototype.onLoginRequest = function (event) {
        var id = event.id;
        if (id == 0) {
            this.loginEvent.callback(event.url);
        }
    };
    HiveManagerImpl.prototype.onResultEvent = function (event) {
        var id = event.hid;
        event.hid = null;
        if (this.resultEvent[id].callback) {
            this.resultEvent[id].callback(event);
        }
    };
    HiveManagerImpl.prototype.getPromise = function (object, method, args) {
        var me = this;
        return new Promise(function (resolve, reject) {
            var onResult = function (ret) {
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
    };
    HiveManagerImpl.prototype.getVersion = function (onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', []);
    };
    HiveManagerImpl.prototype.setListener = function (type, eventCallback) {
        exec(eventCallback, null, 'HivePlugin', 'setListener', [type]);
    };
    HiveManagerImpl.prototype.createClient = function (options, onSuccess, onError) {
        var client = new ClientImpl();
        var me = this;
        var _onSuccess = function (ret) {
            client.objId = ret.id;
            client.plugin = me;
            me.clients[client.objId] = client;
            if (onSuccess)
                onSuccess(client);
        };
        if (typeof (options) == "undefined" || options == null ||
            typeof (options.driveType) != "string") {
            if (onError) {
                onError("invalid options");
            }
            return;
        }
        var configStr = JSON.stringify(options);
        exec(_onSuccess, onError, 'HivePlugin', 'createClient', ["im", configStr]);
    };
    return HiveManagerImpl;
}());
module.exports = new HiveManagerImpl();
