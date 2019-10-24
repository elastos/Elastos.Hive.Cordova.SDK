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

/**
 * The class representing File.
 * @class
 */
function File() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 4;
}

File.prototype = {
    constructor: File,

    /**
     * Get the information(ID, name size, type) of the file got last time.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    /**
     * Get the information(ID, name, size, type) of the file from the server.
     * @return
     * A promise object that contains the information(ID, name, size, type) of the file
     * will be returned on success, otherwise a promise object that contains error
     * information will be returned.
     */
    getInfo: function()  {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    /**
     * Move to a new path.
     * @param {string} destPath     The new path.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    moveTo: function(destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    },

    /**
     * Copy to a new path.
     * @param {string} newPath      The new path.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    copyTo: function(newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    },

    /**
     * Delete.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    deleteItem: function() {
        return this.plugin.getPromise(this, 'deleteItem', []);
    },

    /**
     * Read data of a specified length sequentially.
     * @param {number} length      The length of data to write.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    readData: function(length) {
        return this.plugin.getPromise(this, 'readData', [length]);
    },

    /**
     * Write local change on File.
     * @param {string} data      The data to write.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    writeData: function(data) {
        return this.plugin.getPromise(this, 'writeData', [data]);
    },

    /**
     * Commit local change on File to backend.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    commit: function() {
        return this.plugin.getPromise(this, 'commitData', []);
    },

    /**
     * Discard local change on File.
     * @param {Function} onSuccess  The function to call on success.
     * @return
     * onSuccess will be called on success.
     */
    discard: function(onSuccess) {
        exec(onSuccess, null, 'HivePlugin', 'discardData', [this.clazz, this.objId]);
    },
}

/**
 * The class representing Directory.
 * @class
 */
function Directory() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 3;
}

Directory.prototype = {
    constructor: Directory,

    /**
     * Get the information(ID, name, childCount) of the directory got last time.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    /**
     * Get the information(ID, name, childCount) of the directory from the server.
     * @return
     * A promise object that contains the information(ID, name, childCount) of the file
     * will be returned on success, otherwise a promise object that contains error
     * information will be returned.
     */
    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    /**
     * Create directory with name.
     * @param {string} name      The directory name.
     * @return
     * A directory will be returned on success, otherwise a promise object that contains
     * error information will be returned.
     */
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

    /**
     * Get the directory with a specified name.
     * @param {string} name      The directory name.
     * @return
     * A directory will be returned on success, otherwise a promise object that contains
     * error information will be returned.
     */
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

    /**
     * Create file with name.
     * @param {string} name      The file name.
     * @return
     * A file will be returned on success, otherwise a promise object that contains
     * error information will be returned.
     */
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

    /**
     * Get the File with a specified name.
     * @param {string} name      The file name.
     * @return
     * A file will be returned on success, otherwise a promise object that contains
     * error information will be returned.
     */
    getFile: function(name) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [name]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    /**
     * Get children for current directory.
     * @return
     * The children for current directory will be returned on success, otherwise
     * a promise object that contains error information will be returned.
     */
    getChildren: function() {
        return this.plugin.getPromise(this, 'getChildren', []).then(
            function(ret) {
                return ret.items;
            }
        );
    },

    /**
     * Move to a new path.
     * @param {string} destPath     The destination path.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    moveTo: function(destPath) {
        return this.plugin.getPromise(this, 'moveTo', [destPath]);
    },

    /**
     * Copy to a new path.
     * @param {string} newPath      The new path.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    copyTo: function(newPath) {
        return this.plugin.getPromise(this, 'copyTo', [newPath]);
    },

    /**
     * Delete.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    deleteItem: function() {
        return this.plugin.getPromise(this, 'deleteItem', []);
    },
}

/**
 * The class representing Drive.
 * @class
 */
function Drive() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 2;
}

Drive.prototype = {
    constructor: Drive,

    /**
     * Get the information(ID) of the drive got last time.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    /**
     * Get the information(ID) of the drive from the server.
     * @return
     * A promise object that contains the information(ID) of the file
     * will be returned on success, otherwise a promise that contains error information
     * will be returned.
     */
    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    /**
     * Get the root directory.
     * @return
     * A directory will be returned on success, otherwise a promise that contains
     * error information will be returned.
     */
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

    /**
     * Create directory with path.
     * @param {string} path      The directory path.
     * @return
     * A directory will be returned on success, otherwise a promise that contains
     * error information will be returned.
     */
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

    /**
     * Get the directory with a specified path.
     * @param {string} path      The directory path.
     * @return
     * A directory will be returned on success, otherwise a promise that contains
     * error information will be returned.
     */
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

    /**
     * Create file with path.
     * @param {string} path      The file path.
     * @return
     * A file will be returned on success, otherwise a promise that contains
     * error information will be returned.
     */
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

    /**
     * Get the File with a specified path.
     * @param {string} path      The file path.
     * @return
     * A file will be returned on success, otherwise a promise that contains
     * error information will be returned.
     */
    getFile: function(path) {
        var plugin = this.plugin;
        return this.plugin.getPromise(this, 'getFile', [path]).then(
            function(ret) {
                var file = new File();
                file.objId = ret.id;
                file.plugin = plugin;
                return file;
            }
        );
    },

    /**
     * Get the information(ID, name, size, type) of the drive with a specified path.
     * @param {string} path      The drive path.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise that contains error information will be returned.
     */
    getItemInfo: function(path) {
        return this.plugin.getPromise(this, 'getItemInfo', [path]);
    },
}

/**
 * The class representing Client.
 * @class
 */
function Client() {
    this.objId  = null;
    this.plugin = null;
    this.clazz  = 1;
}

Client.prototype = {
    constructor: Client,

    /**
     * Associate a user with the Client.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @param {Function} handler    The function to call.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    login: function(onSuccess, onError, handler) {
        var handlerId = this.plugin.addLoginRequestCb(handler);
        exec(onSuccess, onError, 'HivePlugin', 'login', [this.clazz, this.objId, handlerId]);
    },

    /**
     * Dissociate the user from the Client.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    logout: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'logout', [this.clazz, this.objId]);
    },

    /**
     * Get the last associated user's information with client information.
     * @param {Function} onSuccess  The function to call on success.
     * @param {Function} onError    The function to call on error.
     * @return
     * onSuccess will be called on success, otherwise onError will be called.
     */
    getLastInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getLastInfo', [this.clazz, this.objId]);
    },

    /**
     * Get associated user's information with client information.
     * @return
     * A promise object that contains success information will be returned on success,
     * otherwise a promise object that contains error information will be returned.
     */
    getInfo: function() {
        return this.plugin.getPromise(this, 'getInfo', []);
    },

    /**
     * Get the current backend's Drive instance associated with the client's drive.
     * @return
     * A drive will be returned on success, otherwise a promise object that contains
     * error information will be returned.
     */
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
            typeof (options.driveType) != "string") {
            if (onError) {
                onError("invalid options");
            }
            return;
        }

        var configStr = JSON.stringify(options);
        exec(_onSuccess, onError, 'HivePlugin', 'createClient', ["im", configStr]);
    },
}

module.exports = new HivePlugin();
