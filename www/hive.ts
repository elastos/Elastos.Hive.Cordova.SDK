/*
 * Copyright (c) 2020 Elastos Foundation
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

function execAsPromise(method: string, params: any[] = []): Promise<any> {
    return new Promise((resolve, reject)=>{
        exec((result)=>{
            resolve(result);
        }, (err)=>{
            reject(err);
        }, 'HivePlugin', method, params);
    });
}

class DatabaseImpl implements HivePlugin.Database.Database {
    createCollection(collectionName: string, options?: HivePlugin.Database.CreateCollectionOptions): Promise<void> {
        return execAsPromise("database_createCollection", [collectionName, options]);
    }

    insertOne(collectionName: string, document: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions): Promise<HivePlugin.Database.InsertResult> {
        return execAsPromise("database_insertOne", [collectionName, document, options]);
    }

    countDocuments(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.CountOptions): Promise<number> {
        throw new Error("Method not implemented.");
    }

    findOne(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): Promise<HivePlugin.JSONObject> {
        throw new Error("Method not implemented.");
    }

    findMany(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): Promise<HivePlugin.JSONObject[]> {
        throw new Error("Method not implemented.");
    }

    updateOne(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<void> {
        throw new Error("Method not implemented.");
    }

    updateMany(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<void> {
        throw new Error("Method not implemented.");
    }

    deleteOne(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<void> {
        throw new Error("Method not implemented.");
    }

    deleteMany(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<void> {
        throw new Error("Method not implemented.");
    }
}

class FilesImpl implements HivePlugin.Files.Files  {
    createFile(remoteFile: string): Promise<string> {
        throw new Error("Method not implemented.");
    }

    upload(url: string, filePath: string): Promise<HivePlugin.Files.Writer> {
        throw new Error("Method not implemented.");
    }

    download(filePath: string): Promise<HivePlugin.Files.Reader> {
        throw new Error("Method not implemented.");
    }

    delete(filePath: string): Promise<void> {
        throw new Error("Method not implemented.");
    }

    createFolder(folder: string): Promise<void> {
        throw new Error("Method not implemented.");
    }

    move(remotePathSrc: string, remotePathDst: string): Promise<void> {
        throw new Error("Method not implemented.");
    }

    copy(remotePathSrc: string, remotePathDst: string): Promise<void> {
        throw new Error("Method not implemented.");
    }

    hash(filePath: String): Promise<string> {
        throw new Error("Method not implemented.");
    }

    listFiles(folderPath: String): Promise<string[]> {
        throw new Error("Method not implemented.");
    }

    fileSize(remoteFile: string): Promise<number> {
        throw new Error("Method not implemented.");
    }
}

class ScriptingImpl implements HivePlugin.Scripting.Scripting {
    registerSubCondition(conditionName: string, condition: HivePlugin.Scripting.Conditions.Condition): Promise<void> {
        throw new Error("Method not implemented.");
    }
    setScript(functionName: string, executionSequence: HivePlugin.Scripting.Executables.ExecutionSequence, accessCondition?: HivePlugin.Scripting.Conditions.Condition) {
        throw new Error("Method not implemented.");
    }
    call(functionName: string, params?: HivePlugin.JSONObject) {
        throw new Error("Method not implemented.");
    }
}

class ClientImpl implements HivePlugin.Client {
    private database: DatabaseImpl;
    private files: FilesImpl;
    private scripting: ScriptingImpl;

    constructor(options: HivePlugin.ClientCreationOptions) {
        this.database = new DatabaseImpl();
        this.files = new FilesImpl();
        this.scripting = new ScriptingImpl();
    }

    async getDatabase(): Promise<HivePlugin.Database.Database> {
        return this.database;
    }

    async getFiles(): Promise<HivePlugin.Files.Files> {
        return this.files;
    }

    async getScripting(): Promise<HivePlugin.Scripting.Scripting> {
        return this.scripting;
    }
}

class HiveManagerImpl implements HivePlugin.HiveManager {
    private client: HivePlugin.Client = null;

    constructor() {
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(ClientImpl.prototype);
        Object.freeze(DatabaseImpl.prototype);
        Object.freeze(FilesImpl.prototype);
        Object.freeze(ScriptingImpl.prototype);
    }

    async getClient(options: HivePlugin.ClientCreationOptions): Promise<HivePlugin.Client> {
        if (!this.client) {
            // TODO: call native create client
            this.client = new ClientImpl(options);
        }
        return this.client;
    }

    resolveOwnVaultProvider(): Promise<HivePlugin.OwnVaultProvider> {
        throw new Error("Method not implemented.");
    }

    resolveRemoteProvider(userDID: string): Promise<HivePlugin.RemoteVaultProvider> {
        throw new Error("Method not implemented.");
    }
}

export = new HiveManagerImpl();
