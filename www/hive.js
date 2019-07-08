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

    const CLAZZ = 4;
    //TODO;
};

File.prototype = {
    onstructor: File,

    getLastInfo: function() {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.CLAZZ, this.objId]);
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
};

function Directory() {
    this.objId  = null;
    this.plugin = null;

    const CLAZZ = 3;
    //TODO
};

Directory.prototype = {
    onstructor: Directory,

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.CLAZZ, this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    createDirectory: function(name) {
        return this.plugin.getPromise(this, 'createDir', [name]);
    },

    getDirectory: function(name) {
        return this.plugin.getPromise(this, 'getDir', [name]);
    },

    createFile: function(name) {
        return this.plugin.getPromise(this, 'createFile', [name]);
    },

    getFile: function(name) {
        return this.plugin.getPromise(this, 'getFile', [name]);
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
};

function Drive() {
    this.objId  = null;
    this.plugin = null;

    const CLAZZ = 2;

    // TODO
};

Drive.prototype = {
    onstructor: Drive,

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.CLAZZ, this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    rootDirctory: function() {
        return this.plugin.getPromise(this, 'rootDir', []);
    },

    createDirectory: function(path) {
        return this.plugin.getPromise(this, 'createDir', [path]);
    },

    getDirectory: function(path) {
        return this.plugin.getPromise(this, 'getDir', [path]);
    },

    createFile: function(path) {
        return this.plugin.getPromise(this, 'createFile', [path]);
    },

    getFile: function(path) {
        return this.plugin.getPromise(this, 'getFile', [path]);
    },

    getItemInfo: function(path) {
        return this.plugin.getPromise(this, 'getItemInfo', [path]);
    },
};

function Client() {
    this.objId  = null;
    this.plugin = null;

    const CLAZZ = 1;
    // TODO
};

Client.prototype = {
    constructor: Client;

    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.CLAZZ, this.objId]);
    },

    login: function(onSuccess, onError, handler) {
        var args = [
          this.objId,
          this.plugin.addLoginRequestCb(handler)
        ];
        exec(onSuccess, onError, 'HivePlugin', 'login', args);
    },

    logout: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'logout', [this.objId]);
    },

    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    getDefaultDrive: function() {
        return this.plugin.getPromise(this, 'getDefDrive', []);
    },
};

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

    // TODO

    Object.freeze(HivePlugin.prototype);
    Object.freeze(Client.prototype);
    Object.freeze(Drive.prototype);
    Object.freeze(Directory.prototype);
    Object.freeze(File.prototype);

    Object.freeze(this.driveType);

    exec(function () {}, null, 'HivePlugin', 'initVal', []);

    var me = this;

    this.addLoginRequestCb = function(callback) {
        // TODO;
    },

    this.onLoginRequest = function(event) {
        // TODO;
    },

    this.addResultEventCb = function(callback, object) {
        var eventCb = new Object;
        eventCb.callback = callback;
        eventCb.object = object;

        me.resultIndex++;
        me.resultEvent[me.resultIndex] = eventCb;
        return me.resultIndex;
    },

    this.onResultEvent = function(event) {
        var id = event.id;
        event.id = null;

        if (me.resultEvent[id].callback)  {
            client = me.resultEvent[id].object;
            me.resultEvent[id].callback(event);
        }
    },

    this.getPromise = async function(object, method, args) {
        var onResult: function(ret) {
            if (null != ret.object)
                Promise.resolve(ret.object);
            if (null != ret.error)
                Promise.reject(ret.error);
        };

        var _args = [
          client.CLAZZ,
          client.objId,
          this.addResultEventCb(onResult, object),
        ];

        exec(null, null, 'HivePlugin', method, _args.concat(args));
    },

    this.setListener(LISTENER_LOGIN,  this.onLoginRequest);
    this.setListener(LISTENER_RESULT, this.onResultEvent);
};

HivePlugin.prototype = {
    constructor: HivePlugin,

    getVersion: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', [])
    },

    setListener: function (type, eventCallback) {
        exec(eventCallback, null, 'HivePlugin', 'setListener', [type]);
    },

    createClient: function(onSuccess, onError, options) {
        var client = Client();
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
};

module.exports = new HivePlugin();
