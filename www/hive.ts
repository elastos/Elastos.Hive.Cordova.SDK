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

function execAsPromise<T>(method: string, params: any[] = []): Promise<T> {
    return new Promise((resolve, reject)=>{
        exec((result)=>{
            resolve(result);
        }, (err)=>{
            reject(err);
        }, 'HivePlugin', method, params);
    });
}

class DatabaseImpl implements HivePlugin.Database.Database {
    constructor(private vault: VaultImpl) {}

    createCollection(collectionName: string, options?: HivePlugin.Database.CreateCollectionOptions): Promise<void> {
        return execAsPromise<void>("database_createCollection", [this.vault.objectId, collectionName, options]);
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

    updateOne(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<HivePlugin.Database.UpdateResult> {
        throw new Error("Method not implemented.");
    }

    updateMany(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<HivePlugin.Database.UpdateResult> {
        throw new Error("Method not implemented.");
    }

    deleteOne(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<HivePlugin.Database.DeleteResult> {
        throw new Error("Method not implemented.");
    }

    deleteMany(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<HivePlugin.Database.DeleteResult> {
        throw new Error("Method not implemented.");
    }
}

class FilesImpl implements HivePlugin.Files.Files  {
    upload(path: string): Promise<HivePlugin.Files.Writer> {
        throw new Error("Method not implemented.");
    }
    download(path: string): Promise<HivePlugin.Files.Reader> {
        throw new Error("Method not implemented.");
    }
    delete(path: string): Promise<boolean> {
        throw new Error("Method not implemented.");
    }
    createFolder(path: string): Promise<boolean> {
        throw new Error("Method not implemented.");
    }
    move(srcPath: string, dstpath: string): Promise<boolean> {
        throw new Error("Method not implemented.");
    }
    copy(srcPath: string, dstpath: string): Promise<boolean> {
        throw new Error("Method not implemented.");
    }
    hash(path: string): Promise<string> {
        throw new Error("Method not implemented.");
    }
    list(path: string): Promise<HivePlugin.Files.FileInfo[]> {
        throw new Error("Method not implemented.");
    }
    stat(path: string): Promise<HivePlugin.Files.FileInfo> {
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

class VaultImpl implements HivePlugin.Vault {
    objectId: string;

    private vaultProviderAddress: string;
    private vaultOwnerDid: string;

    private database: DatabaseImpl;
    private files: FilesImpl;
    private scripting: ScriptingImpl;

    constructor(vaultProviderAddress: string, vaultOwnerDid: string) {
        this.vaultProviderAddress = vaultProviderAddress;
        this.vaultOwnerDid = vaultOwnerDid;

        this.database = new DatabaseImpl(this);
        this.files = new FilesImpl();
        this.scripting = new ScriptingImpl();
    }

    static fromJson(json: HivePlugin.JSONObject): VaultImpl {
        let vault = new VaultImpl(null, null);
        Object.assign(vault, json);
        return vault;
    }

    getVaultProviderAddress(): string {
        return this.vaultProviderAddress;
    }

    getVaultOwnerDid(): string {
        return this.vaultOwnerDid;
    }

    getDatabase(): HivePlugin.Database.Database {
        return this.database;
    }

    getFiles(): HivePlugin.Files.Files {
        return this.files;
    }

    getScripting(): HivePlugin.Scripting.Scripting {
        return this.scripting;
    }
}

class HiveManagerImpl implements HivePlugin.HiveManager {
    constructor() {
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(VaultImpl.prototype);
        Object.freeze(DatabaseImpl.prototype);
        Object.freeze(FilesImpl.prototype);
        Object.freeze(ScriptingImpl.prototype);
    }

    async connectToVault(vaultProviderAddress: string, vaultOwnerDid: string): Promise<HivePlugin.Vault> {
        let vaultJson = await execAsPromise<HivePlugin.JSONObject>("connectToVault", [vaultProviderAddress, vaultOwnerDid]);
        let vault = VaultImpl.fromJson(vaultJson);
        return vault;
    }
}

export = new HiveManagerImpl();
