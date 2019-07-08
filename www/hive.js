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
    const FILE = 4;

    this.objId = null;
    //TODO;
};

File.prototype = {
    onstructor: File,

    getInfo: function() {
        exec(onSuccess, onError, 'HivePlugin', 'getInfo', [this.FILE, this.objId]);
    },

    getPromise: function(onSuccess, onError, methodName, args) {
        return new Promise(resolve, reject) {
            var onResult: function(ret) {
                if (ret.object != null) {
                    onSuccess(ret.object);
                    resolve(ret.object);
                } else {
                    onError(ret.error);
                    reject(ret.error);
                }
            };
            exec(onResult, null, 'HivePlugin', methodName, args);
        };
    },

    getLastInfo: function(onSuccess, onError)  {
        return getPromise(onSuccess, onError, 'getLastInfo', [this.FILE, this.objId]);
    }

    moveTo: function(onSuccess, onError, newPath) {
        return getPromise(onSuccess, onError, 'moveTo', [this.FILE, this.objId, destPath]);
    },

    copyTo: function(onSuccess, onError, newPath) {
        return getPromise(onSuccess, onError, 'copyTo', [this.FILE, this.objId, newPath]);
    },

    deleteItem: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'deleteItem', [this.FILE, this.objId]);
    },
};

function Directory() {
    const DIR = 3;

    this.objId = null;
    //TODO
};

Directory.prototype = {
    onstructor: Directory,

    getInfo: function() {
        exec(onSuccess, onError, 'HivePlugin', 'getInfo', [this.DIR, this.objId]);
    },

    getPromise: function(onSuccess, onError, methodName, args) {
        return new Promise(resolve, reject) {
            var onResult: function(ret) {
                if (ret.object != null) {
                    onSuccess(ret.object);
                    resolve(ret.object);
                } else {
                    onError(ret.error);
                    reject(ret.error);
                }
            };
            exec(onResult, null, 'HivePlugin', methodName, args);
        };
    },

    getLastInfo: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'getLastInfo', [this.DIR, this.objId]);
    },

    createDirectory: function(onSuccess, onError, name) {
        return getPromise(onSuccess, onError, 'createDirectory', [this.DIR, this.objId, name]);
    },

    getDirectory: function(onSuccess, onError, name) {
        return getPromise(onSuccess, onError, 'getDirectory', [this.DIR, this.objId, name]);
    },

    createFile: function(onSuccess, onError, name) {
        return getPromise(onSuccess, onError, 'createFile', [this.DIR, this.objId, name]);
    },

    getFile: function(onSuccess, onError, name) {
        return getPromise(onSuccess, onError, 'getFile', [this.DIR, this.objId, name]);
    },

    getChildren: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'getChildren', [this.DIR, this.objId]);
    },

    moveTo: function(onSuccess, onError, newPath) {
        return getPromise(onSuccess, onError, 'moveTo', [this.DIR, this.objId, destPath]);
    },

    copyTo: function(onSuccess, onError, newPath) {
        return getPromise(onSuccess, onError, 'copyTo', [this.DIR, this.objId, newPath]);
    },

    deleteItem: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'deleteItem', [this.DIR, this.objId]);
    },
};

function Drive() {
    const DRIVE = 2;

    this.objId = null;

    // TODO
};

Drive.prototype = {
    onstructor: Drive,

    getInfo: function() {
        exec(onSuccess, onError, 'HivePlugin', 'getInfo', [this.DRIVE, this.objId]);
    },

    getPromise: function(onSuccess, onError, methodName, args) {
        return new Promise(resolve, reject) {
            var onResult: function(ret) {
                if (ret.object != null) {
                    onSuccess(ret.object);
                    resolve(ret.object);
                } else {
                    onError(ret.error);
                    reject(ret.error);
                }
            };

            exec(onResult, null, 'HivePlugin', methodName, args);
        };
    },

    getLastInfo: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'getLastInfo', [this.DRIVE, this.objId]);
    },

    rootDirctory: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'rootDirctory',[this.DRIVE, this.objId]);
    },

    createDirectory: function(onSuccess, onError, path) {
        return getPromise(onSuccess, onError, 'createDirectory', [this.DRIVE, this.objId, path]);
    },

    getDirectory: function(onSuccess, onError, path) {
        return getPromise(onSuccess, onError, 'getDirectory', [this.DRIVE, this.objId, path]);
    },

    createFile: function(onSuccess, onError, path) {
        return getPromise(onSuccess, onError, 'createFile', [this.DRIVE, this.objId, path]);
    },

    getFile: function(onSuccess, onError, path) {
        return getPromise(onSuccess, onError, 'getFile', [this.DRIVE, this.objId, path]);
    },

    getItemInfo: function(onSuccess, onError, path) {
        return getPromise(onSuccess, onError, 'getItemInfo', [this.DRIVE, this.objId, path]);
    },
};

function Client() {
    this.objId = null;

    const CLIENT = 1;
    // TODO
};

Client.prototype = {
    constructor: Client;

    getInfo: function(onSuccess, onError) {
        exec(onSuccess, onError, "getInfo", [this.CLIENT, this.objId]);
    }

    getPromise: function(onSuccess, onError, methodName) {
        return new Promise(resolve, reject) {
            var onResult: function(ret) {
                if (ret.object != null) {
                    onSuccess(ret.object);
                    resolve(ret.object);
                } else {
                    onError(ret.error);
                    reject(ret.error);
                }
            };

            exec(onResult, null, 'HivePlugin', methodName, [this.CLIENT, this.objId]);
        };
    },

    getLastInfo: function(onSuccess, onError) {
        return getPromise(onSuccess, onError, 'getInfo');
    },

    getDefaultDrive: function(onSuccess, onError) {
        return getPromise(onSuccess, onError,'getDefaultDrive');
    },
};

function HivePlugin() {
    // TODO;

    Object.freeze(HivePlugin.prototype);
    Object.freeze(Client.prototype);
    Object.freeze(Drive.prototype);
    Object.freeze(Directory.prototype);
    Object.freeze(File.prototype);

    // TODO;
};

HivePlugin.prototype = {
    constructor: HivePlugin,

    options: {
        persistentLocation: ".data",
        bootstraps: this.bootstraps
    },

    test: function(onSuccess, onError, buf) {
        exec(onSuccess, onError, 'HivePlugin', 'test', [buf]);
    },

    getVersion: function(onSuccess, onError) {
        exec(onSuccess, onError, 'HivePlugin', 'getVersion', [])
    },

    createClient: function(onSuccess, onError, options) {
        var client = Client();
        var me = this;

        if (typeof (options) == "undefined" || options == null)
            options = this.options;

        var configString = JSON.stringify(options);
        exec(onSuccess, onError, 'HivePlugin', 'createClient', [configString]);
    },
};

module.exports = new HivePlugin();
