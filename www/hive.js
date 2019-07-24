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

var exec = require('cordova/exec');

function File() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 4;
}

File.prototype = {
    onstructor: File,

    getLastInfo: function() {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    getInfo: function()  {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    moveTo: function(destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    },

    copyTo: function(newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    },

    deleteItem: function() {
        return this.plugin.getPromise(this, 'deleteItem', []);
    },
}

function Directory() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 3;
}

Directory.prototype = {
    onstructor: Directory,

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    createDirectory: function(name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [name]).then(
            function(ret) {
                var directory = new Directory();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    },

    getDirectory: function(name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [name]).then(
            function(ret) {
                var directory = new Directory();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    },

    createFile: function(name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [name]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    getFile: function(name) {
        var plguin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [name]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    getChildren: function() {
        return this.plugin.getPromise(this, 'getChildren', []);
    },

    moveTo: function(destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    },

    copyTo: function(newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    },

    deleteItem: function() {
        return this.plugin.getPromise(this, 'deleteItem', []);
    },
}

function Drive() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 2;
}

Drive.prototype = {
    onstructor: Drive,

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    rootDirctory: function() {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'rootDir', []).then(
            function(ret) {
                var directory = new Directory();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    },

    createDirectory: function(path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createDir', [path]).then(
            function(ret) {
                var directory = new Directory();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    },

    getDirectory: function(path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDir', [path]).then(
            function(ret) {
                var directory = new Directory();
                directory.objId = ret.id;
                directory.plugin  = plugin;
                return directory;
            }
        );
    },

    createFile: function(path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'createFile', [path]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    getFile: function(path) {
        var plguin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [path]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    getItemInfo: function(path) {
        return this.plugin.getPromise(this, 'getItemInfo', [path]);
    },
}

function Client() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 1;
}

Client.prototype = {
    constructor: Client,

    login: function(onSuccess, onError, handler) {
        var handlerId = this.plugin.addLoginRequestCb(handler);
        exec(onSuccess, onError, 'HivePlugin', 'login', [this.clazz, this.objId, handlerId]);
    },

    logout: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'logout', [this.clazz, this.objId]);
    },

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    getDefDrive: function() {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getDefDrive', []).then(
            function (ret) {
                var drive = new Drive();
                drive.objId = ret.id;
                drive.plugin = plugin;
                return drive;
            }
        );
    },
}

function HivePlugin() {
    this.clients = [];

    this.resultIndex = 0;
    this.resultEvent = [];
    this.loginCount  = 0;
    this.loginRequest = [];

    this.driveType = {
        NATIVE: 1,
        ONEDRIVE: 2,
        IPFS: 3,
    };

    const LISTENER_LOGIN  = 1;
    const LISTENER_RESULT = 2;

    Object.freeze(HivePlugin.prototype);
    Object.freeze(Client.prototype);
    Object.freeze(Drive.prototype);
    Object.freeze(Directory.prototype);
    Object.freeze(File.prototype);

    Object.freeze(this.driveType);

    exec(function () {}, null, 'HivePlugin', 'initVal', []);

    var me = this;

    this.addLoginRequestCb = function(callback) {
        var eventcb = new Object();
        eventcb.callback = callback;

        me.loginevent = eventcb;
        return 0;
    },

    this.onLoginRequest = function(event) {
        var id = event.id;
        if (id == 0) {
            me.loginevent.callback(event.url);
        }
    },

    this.addResultEventCb = function(callback, object) {
        var eventcb = new Object;
        eventcb.callback = callback;
        eventcb.object = object;

        me.resultIndex++;
        me.resultEvent[me.resultIndex] = eventcb;
        return me.resultIndex;
    },

    this.onResultEvent = function(event) {
        var id = event.hid;
        event.hid = null;

        if (me.resultEvent[id].callback)  {
            me.resultEvent[id].callback(event);
        }
    },

    this.getPromise = function(object, method, args) {
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
    },

    this.setListener(LISTENER_LOGIN,  this.onLoginRequest);
    this.setListener(LISTENER_RESULT, this.onResultEvent);
}

HivePlugin.prototype = {
    constructor: HivePlugin,

    getVersion: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', [])
    },

    setListener: function (type, eventCallback) {
        exec(eventCallback, null, 'HivePlugin', 'setListener', [type]);
    },

    createClient: function(onSuccess, onError, options) {
        var client = new Client();
        var me = this;

        var _onSuccess = function(ret) {
            client.objId = ret.id;
            client.plugin = me;
            me.clients[client.objId] = client;
            if (onSuccess)
                onSuccess(client);
        }

        if (typeof (options) == "undefined" || options == null ||
            typeof (options.driveType) != "string")
            if (onError) {
                onError("invalid options");
                return;
            }

        var configStr = JSON.stringify(options);
        exec(_onSuccess, onError, 'HivePlugin', 'createClient', ["im", configStr]);
    },
}

module.exports = new HivePlugin();
