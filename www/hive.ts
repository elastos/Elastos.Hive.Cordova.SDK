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
        exec((result: any)=>{
            resolve(result);
        }, (err: any)=>{
            reject(err);
        }, 'HivePlugin', method, params);
    });
}

class InsertResultImpl implements HivePlugin.Database.InsertResult {
    insertedCount: number;
    insertedIds: string[];

    static fromJson(json: HivePlugin.JSONObject): InsertResultImpl {
        let result = new InsertResultImpl();
        Object.assign(result, json);
        return result;
    }
}

class UpdateResultImpl implements HivePlugin.Database.UpdateResult {
    updatedCount: number;
    updatedIds: string[];

    static fromJson(json: HivePlugin.JSONObject): UpdateResultImpl {
        let result = new UpdateResultImpl();
        Object.assign(result, json);
        return result;
    }
}

class DeleteResultImpl implements HivePlugin.Database.DeleteResult {
    deletedCount: number;
    deletedIds: string[];

    static fromJson(json: HivePlugin.JSONObject): DeleteResultImpl {
        let result = new DeleteResultImpl();
        Object.assign(result, json);
        return result;
    }
}

class DatabaseImpl implements HivePlugin.Database.Database {
    constructor(private vault: VaultImpl) {}

    createCollection(collectionName: string, options?: HivePlugin.Database.CreateCollectionOptions): Promise<HivePlugin.Database.CreateCollectionResult> {
        return execAsPromise<HivePlugin.Database.CreateCollectionResult>("database_createCollection", [this.vault.objectId, collectionName, options]);
    }

    deleteCollection(collectionName: string, options?: HivePlugin.Database.DeleteCollectionOptions): Promise<HivePlugin.Database.DeleteCollectionResult> {
        return execAsPromise<HivePlugin.Database.DeleteCollectionResult>("database_deleteCollection", [this.vault.objectId, collectionName, options]);
    }

    async insertOne(collectionName: string, document: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions): Promise<HivePlugin.Database.InsertResult> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("database_insertOne", [this.vault.objectId, collectionName, document, options]);
        return InsertResultImpl.fromJson(resultJson);
    }

    countDocuments(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.CountOptions): Promise<number> {
        return execAsPromise<number>("database_countDocuments", [this.vault.objectId, collectionName, query, options]);
    }

    findOne(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): Promise<HivePlugin.JSONObject> {
        return execAsPromise<HivePlugin.JSONObject>("database_findOne", [this.vault.objectId, collectionName, query, options]);
    }

    findMany(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): Promise<HivePlugin.JSONObject[]> {
        return execAsPromise<HivePlugin.JSONObject[]>("database_findMany", [this.vault.objectId, collectionName, query, options]);
    }

    async updateOne(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<HivePlugin.Database.UpdateResult> {
        let resultJson = await execAsPromise<HivePlugin.Database.UpdateResult>("database_updateOne", [this.vault.objectId, collectionName, filter, updateQuery, options]);
        return UpdateResultImpl.fromJson(resultJson);
    }

    async updateMany(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): Promise<HivePlugin.Database.UpdateResult> {
        let resultJson = await execAsPromise<HivePlugin.Database.UpdateResult>("database_updateMany", [this.vault.objectId, collectionName, filter, updateQuery, options]);
        return UpdateResultImpl.fromJson(resultJson);
    }

    async deleteOne(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<HivePlugin.Database.DeleteResult> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("database_deleteOne", [this.vault.objectId, collectionName, filter, options]);
        return DeleteResultImpl.fromJson(resultJson);
    }

    async deleteMany(collectionName: string, filter: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): Promise<HivePlugin.Database.DeleteResult> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("database_deleteMany", [this.vault.objectId, collectionName, filter, options]);
        return DeleteResultImpl.fromJson(resultJson);
    }
}

class WriterImpl implements HivePlugin.Files.Writer {
    write(data: Blob): Promise<number> {
        return execAsPromise<number>("writer_write", [data]);
    }
    flush(): Promise<void> {
        return execAsPromise<void>("writer_flush", []);
    }
    close(): Promise<void> {
        return execAsPromise<void>("writer_close", []);
    }

    static fromJson(json: HivePlugin.JSONObject): WriterImpl {
        let result = new WriterImpl();
        Object.assign(result, json);
        return result;
    }
}

class ReaderImpl implements HivePlugin.Files.Reader {
    read(bytesCount: number): Promise<Blob> {
        return execAsPromise<Blob>("reader_read", [bytesCount]);
    }
    readAll(): Promise<Blob> {
        return execAsPromise<Blob>("reader_readAll", []);
    }
    close(): Promise<void> {
        return execAsPromise<void>("reader_close", []);
    }

    static fromJson(json: HivePlugin.JSONObject): ReaderImpl {
        let result = new ReaderImpl();
        Object.assign(result, json);
        return result;
    }
}

class FileInfoImpl implements HivePlugin.Files.FileInfo {
    name: string;
    size?: number;
    lastModified?: Date;
    type: HivePlugin.Files.FileType;

    static fromJson(json: HivePlugin.JSONObject): FileInfoImpl {
        let result = new FileInfoImpl();
        Object.assign(result, json);
        return result;
    }
}

class FilesImpl implements HivePlugin.Files.Files  {
    constructor(private vault: VaultImpl) {}

    async upload(path: string): Promise<HivePlugin.Files.Writer> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("files_upload", [this.vault.objectId, path]);
        return WriterImpl.fromJson(resultJson);
    }

    async download(path: string): Promise<HivePlugin.Files.Reader> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("files_download", [this.vault.objectId, path]);
        return ReaderImpl.fromJson(resultJson);
    }

    delete(path: string): Promise<boolean> {
        return execAsPromise<boolean>("files_delete", [this.vault.objectId, path]);
    }

    async createFolder(path: string): Promise<boolean> {
        try {
            await execAsPromise<void>("files_createFolder", [this.vault.objectId, path]);
            return true;
        }
        catch (e) {
            return false;
        }
    }

    move(srcPath: string, dstpath: string): Promise<boolean> {
        return execAsPromise<boolean>("files_move", [this.vault.objectId, srcPath, dstpath]);
    }

    copy(srcPath: string, dstpath: string): Promise<boolean> {
        return execAsPromise<boolean>("files_copy", [this.vault.objectId, srcPath, dstpath]);
    }

    hash(path: string): Promise<string> {
        return execAsPromise<string>("files_hash", [this.vault.objectId, path]);
    }

    async list(path: string): Promise<HivePlugin.Files.FileInfo[]> {
        let resultsJson = await execAsPromise<HivePlugin.JSONObject[]>("files_list", [this.vault.objectId, path]);
        let fileInfos: HivePlugin.Files.FileInfo[] = [];
        for (let resultJson of resultsJson) {
            fileInfos.push(FileInfoImpl.fromJson(resultJson));
        }
        return fileInfos;
    }

    async stat(path: string): Promise<HivePlugin.Files.FileInfo> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("files_stat", [this.vault.objectId, path]);
        return FileInfoImpl.fromJson(resultJson);
    }
}

class ExecutionSequenceImpl implements HivePlugin.Scripting.Executables.ExecutionSequence {
    constructor(public executables: HivePlugin.Scripting.Executables.Executable[]) {}

    toJSON(): HivePlugin.JSONObject[] {
        let jsonObj: HivePlugin.JSONObject[] = [];
        for (let executable of this.executables) {
            jsonObj.push(executable.toJSON());
        }
        return jsonObj;
    }
}

class FindOneQueryImpl implements HivePlugin.Scripting.Executables.Database.FindOneQuery {
    constructor(private collectionName: String, private query?: HivePlugin.JSONObject, private options?: HivePlugin.Database.FindOptions) {}

    toJSON(): HivePlugin.JSONObject {
        let jsonObj = new HivePlugin.JSONObject();
        Object.assign(jsonObj, this);
        return jsonObj;
    }
}

// For now, exactly the same as FindOneQueryImpl
class FindManyQueryImpl extends FindOneQueryImpl {}

class InsertQueryImpl implements HivePlugin.Scripting.Executables.Database.InsertQuery {
    constructor(private collectionName: String, private document: HivePlugin.JSONObject, private options: HivePlugin.Database.InsertOptions) {}

    toJSON(): HivePlugin.JSONObject {
        let jsonObj = new HivePlugin.JSONObject();
        Object.assign(jsonObj, this);
        return jsonObj;
    }
}

class UpdateQueryImpl implements HivePlugin.Scripting.Executables.Database.UpdateQuery {
    constructor(private collectionName: String, private document: HivePlugin.JSONObject, private options: HivePlugin.Database.UpdateOptions) {}

    toJSON(): HivePlugin.JSONObject {
        let jsonObj = new HivePlugin.JSONObject();
        Object.assign(jsonObj, this);
        return jsonObj;
    }
}

class DeleteQueryImpl implements HivePlugin.Scripting.Executables.Database.DeleteQuery {
    constructor(private collectionName: String, private document: HivePlugin.JSONObject, private options: HivePlugin.Database.DeleteOptions) {}

    toJSON(): HivePlugin.JSONObject {
        let jsonObj = new HivePlugin.JSONObject();
        Object.assign(jsonObj, this);
        return jsonObj;
    }
}

class ScriptingImpl implements HivePlugin.Scripting.Scripting {
    constructor(private vault: VaultImpl) {}

    registerSubCondition(conditionName: string, condition: HivePlugin.Scripting.Conditions.Condition): Promise<void> {
        return execAsPromise<void>("scripting_registerSubCondition", [this.vault.objectId, conditionName, condition]);
    }
    setScript(functionName: string, executionSequence: HivePlugin.Scripting.Executables.ExecutionSequence, accessCondition?: HivePlugin.Scripting.Conditions.Condition): Promise<void> {
        return execAsPromise<void>("scripting_setScript", [this.vault.objectId, functionName, executionSequence, accessCondition]);
    }
    call(functionName: string, params?: HivePlugin.JSONObject): Promise<HivePlugin.JSONObject> {
        return execAsPromise<HivePlugin.JSONObject>("scripting_call", [this.vault.objectId, functionName, params]);
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
        this.files = new FilesImpl(this);
        this.scripting = new ScriptingImpl(this);
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
    Scripting: {
        Executables: {
            newExecutionSequence: (executables: HivePlugin.Scripting.Executables.Executable[]) => HivePlugin.Scripting.Executables.ExecutionSequence;

            Database: {
                newFindOneQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions) => HivePlugin.Scripting.Executables.Database.FindOneQuery;
                newFindManyQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions) => HivePlugin.Scripting.Executables.Database.FindManyQuery;
                newInsertQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions) => HivePlugin.Scripting.Executables.Database.InsertQuery;
                newUpdateQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions) => HivePlugin.Scripting.Executables.Database.UpdateQuery;
                newDeleteQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions) => HivePlugin.Scripting.Executables.Database.DeleteQuery;
            }
        };
    };

    constructor() {
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(VaultImpl.prototype);
        Object.freeze(DatabaseImpl.prototype);
        Object.freeze(FilesImpl.prototype);
        Object.freeze(ScriptingImpl.prototype);

        this.Scripting = {
            Executables: {
                newExecutionSequence: function(executables: HivePlugin.Scripting.Executables.Executable[]): HivePlugin.Scripting.Executables.ExecutionSequence {
                    return new ExecutionSequenceImpl(executables);
                },

                Database: {
                    newFindOneQuery: function(collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): HivePlugin.Scripting.Executables.Database.FindOneQuery {
                        return new FindOneQueryImpl(collectionName, query, options);
                    },

                    newFindManyQuery: function(collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions): HivePlugin.Scripting.Executables.Database.FindOneQuery {
                        return new FindManyQueryImpl(collectionName, query, options);
                    },

                    newInsertQuery: function(collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions): HivePlugin.Scripting.Executables.Database.InsertQuery {
                        return new InsertQueryImpl(collectionName, query, options);
                    },

                    newUpdateQuery: function(collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions): HivePlugin.Scripting.Executables.Database.UpdateQuery {
                        return new UpdateQueryImpl(collectionName, query, options);
                    },

                    newDeleteQuery: function(collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions): HivePlugin.Scripting.Executables.Database.DeleteQuery {
                        return new DeleteQueryImpl(collectionName, query, options);
                    }
                }
            }
        };
    }

    async getVault(vaultProviderAddress: string, vaultOwnerDid: string): Promise<HivePlugin.Vault> {
        let vaultJson = await execAsPromise<HivePlugin.JSONObject>("getVault", [vaultProviderAddress, vaultOwnerDid]);
        return VaultImpl.fromJson(vaultJson);
    }
}

export = new HiveManagerImpl();
