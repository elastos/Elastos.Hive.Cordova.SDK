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

class JSONObjectImpl implements HivePlugin.JSONObject {
    [k: string]: string | number | boolean | HivePlugin.JSONObject | HivePlugin.JSONObject[];
}

class InsertOneResultImpl implements HivePlugin.Database.InsertOneResult {
    insertedId: string;

    static fromJson(json: HivePlugin.JSONObject): InsertOneResultImpl {
        let result = new InsertOneResultImpl();
        Object.assign(result, json);
        return result;
    }
}

class InsertManyResultImpl implements HivePlugin.Database.InsertManyResult {
    insertedIds: string[];

    static fromJson(json: HivePlugin.JSONObject): InsertManyResultImpl {
        let result = new InsertManyResultImpl();
        Object.assign(result, json);
        return result;
    }
}

class UpdateResultImpl implements HivePlugin.Database.UpdateResult {
    matchedCount: number;
    modifiedCount: number;
    upsertedCount: number;
    upsertedId: string;

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

    async insertOne(collectionName: string, document: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions): Promise<HivePlugin.Database.InsertOneResult> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("database_insertOne", [this.vault.objectId, collectionName, document, options]);
        return InsertOneResultImpl.fromJson(resultJson);
    }

    async insertMany(collectionName: string, documents: HivePlugin.JSONObject[], options?: HivePlugin.Database.InsertOptions): Promise<HivePlugin.Database.InsertManyResult> {
        let resultJson = await execAsPromise<HivePlugin.JSONObject>("database_insertMany", [this.vault.objectId, collectionName, documents, options]);
        return InsertManyResultImpl.fromJson(resultJson);
    }

    async countDocuments(collectionName: string, query: HivePlugin.JSONObject, options?: HivePlugin.Database.CountOptions): Promise<number> {
        let resultJson = await execAsPromise<{count:number}>("database_countDocuments", [this.vault.objectId, collectionName, query, options]);
        return resultJson.count;
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
    objectId: string;

    write(data: Uint8Array): Promise<void> {
        return execAsPromise<void>("writer_write", [this.objectId, data.buffer]);
    }
    flush(): Promise<void> {
        return execAsPromise<void>("writer_flush", [this.objectId]);
    }
    close(): Promise<void> {
        return execAsPromise<void>("writer_close", [this.objectId]);
    }

    static fromJson(json: HivePlugin.JSONObject): WriterImpl {
        if (!json)
            return null;

        let result = new WriterImpl();
        Object.assign(result, json);
        return result;
    }
}

/**
 * Helper for abase64 conversions.
 * Converted to TS from the original JS code at https://github.com/niklasvh/base64-arraybuffer/blob/master/lib/base64-arraybuffer.js
 */
class Base64Binary {
    private chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private lookup: Uint8Array = null;

    constructor() {
        // Use a lookup table to find the index.
        this.lookup = new Uint8Array(256);
        for (var i = 0; i < this.chars.length; i++) {
            this.lookup[this.chars.charCodeAt(i)] = i;
        }
    }

    encode(arraybuffer: ArrayBuffer): string {
      var bytes = new Uint8Array(arraybuffer), i, len = bytes.length, base64 = "";

      for (i = 0; i < len; i+=3) {
        base64 += this.chars[bytes[i] >> 2];
        base64 += this.chars[((bytes[i] & 3) << 4) | (bytes[i + 1] >> 4)];
        base64 += this.chars[((bytes[i + 1] & 15) << 2) | (bytes[i + 2] >> 6)];
        base64 += this.chars[bytes[i + 2] & 63];
      }

      if ((len % 3) === 2) {
        base64 = base64.substring(0, base64.length - 1) + "=";
      } else if (len % 3 === 1) {
        base64 = base64.substring(0, base64.length - 2) + "==";
      }

      return base64;
    };

    decode(base64: string): Uint8Array {
      var bufferLength = base64.length * 0.75, len = base64.length, i, p = 0, encoded1, encoded2, encoded3, encoded4;

      if (base64[base64.length - 1] === "=") {
        bufferLength--;
        if (base64[base64.length - 2] === "=") {
          bufferLength--;
        }
      }

      var arraybuffer = new ArrayBuffer(bufferLength),
      bytes = new Uint8Array(arraybuffer);

      for (i = 0; i < len; i+=4) {
        encoded1 = this.lookup[base64.charCodeAt(i)];
        encoded2 = this.lookup[base64.charCodeAt(i+1)];
        encoded3 = this.lookup[base64.charCodeAt(i+2)];
        encoded4 = this.lookup[base64.charCodeAt(i+3)];

        bytes[p++] = (encoded1 << 2) | (encoded2 >> 4);
        bytes[p++] = ((encoded2 & 15) << 4) | (encoded3 >> 2);
        bytes[p++] = ((encoded3 & 3) << 6) | (encoded4 & 63);
      }

      return new Uint8Array(arraybuffer);
    };
}

class ReaderImpl implements HivePlugin.Files.Reader {
    objectId: string;

    async read(bytesCount: number): Promise<Uint8Array> {
        // Cordova automatically converts Uint8Array to a encoded base64 string in the direction JS->Native.
        // But it does not convert from base64 to Uint8Array in the other direction. So we do this manually.
        let readData = await execAsPromise<string>("reader_read", [this.objectId, bytesCount]);
        if (!readData)
            return null;

        return new Base64Binary().decode(readData);
    }
    readAll(): Promise<Blob> {
        return execAsPromise<Blob>("reader_readAll", [this.objectId]);
    }
    close(): Promise<void> {
        return execAsPromise<void>("reader_close", [this.objectId]);
    }

    static fromJson(json: HivePlugin.JSONObject): ReaderImpl {
        if (!json)
            return null;

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
        if (!json)
            return null;

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

    async delete(path: string): Promise<boolean> {
        let result = await execAsPromise<{success:boolean}>("files_delete", [this.vault.objectId, path]);
        return result.success;
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

abstract class ConditionImpl implements HivePlugin.Scripting.Conditions.Condition {
    constructor(public type: string) {}

    abstract toJSON(): JSONObjectImpl;
}

class AndConditionImpl extends ConditionImpl implements HivePlugin.Scripting.Conditions.AndCondition {
    constructor(private conditions: ConditionImpl[]) {
        super("and");
    }

    toJSON(): JSONObjectImpl {
        let body: JSONObjectImpl[] = [];
        for (let condition of this.conditions) {
            body.push(condition.toJSON());
        }
        return {
            type: this.type,
            name: this.type,
            body: body
        }
    }
}

class OrConditionImpl extends ConditionImpl {
    constructor(private conditions: ConditionImpl[]) {
        super("or");
    }

    toJSON(): JSONObjectImpl {
        let body: JSONObjectImpl[] = [];
        for (let condition of this.conditions) {
            body.push(condition.toJSON());
        }
        return {
            type: this.type,
            name: this.type,
            body: body
        }
    }
}

class QueryHasResultsCondition extends ConditionImpl implements HivePlugin.Scripting.Conditions.AndCondition {
    constructor(private collectionName: string, private queryParameters: HivePlugin.JSONObject) {
        super("queryHasResults");
    }

    toJSON(): JSONObjectImpl {
        return {
            type: this.type,
            name: this.type,
            body: {
                collection: this.collectionName,
                filter: this.queryParameters,
            }
        }
    }
}

abstract class ExecutableImpl implements HivePlugin.Scripting.Executables.Executable {
    constructor(public type: string) {}

    abstract toJSON(): JSONObjectImpl;
}

class AggregatedExecutableImpl extends ExecutableImpl implements HivePlugin.Scripting.Executables.AggregatedExecutable {
    constructor(public executables: ExecutableImpl[]) {
        super("aggregated");
    }

    toJSON(): JSONObjectImpl {
        let body: JSONObjectImpl[] = [];
        for (let executable of this.executables) {
            body.push(executable.toJSON());
        }
        return {
            type: this.type,
            name: this.type,
            body: body
        }
    }
}

class FindOneQueryImpl extends ExecutableImpl implements HivePlugin.Scripting.Executables.Database.FindOneQuery {
    constructor(private collectionName: string, private query?: HivePlugin.JSONObject, private options?: HivePlugin.Database.FindOptions, private output: boolean = false) {
        super("find");
    }

    toJSON(): JSONObjectImpl {
        return {
            type: this.type,
            name: this.type,
            output: this.output,
            body: {
                collection: this.collectionName,
                filter: this.query,
            }
            // TODO: where are the options?
        }
    }
}

// For now, exactly the same as FindOneQueryImpl
class FindManyQueryImpl extends FindOneQueryImpl {}

class InsertQueryImpl extends ExecutableImpl implements HivePlugin.Scripting.Executables.Database.InsertQuery {
    constructor(private collectionName: string, private document: HivePlugin.JSONObject, private options: HivePlugin.Database.InsertOptions, private output: boolean = false) {
        super("insert");
    }

    toJSON(): JSONObjectImpl {
        return {
            type: this.type,
            name: this.type,
            output: this.output,
            body: {
                collection: this.collectionName,
                document: this.document,
            }
            // TODO: where are the options?
        }
    }
}

class UpdateQueryImpl extends ExecutableImpl implements HivePlugin.Scripting.Executables.Database.UpdateQuery {
    constructor(private collectionName: string, private filter: HivePlugin.JSONObject, private updateQuery: HivePlugin.JSONObject, private options: HivePlugin.Database.UpdateOptions, private output: boolean = false) {
        super("update");
    }

    toJSON(): JSONObjectImpl {
        return {
            type: this.type,
            name: this.type,
            output: this.output,
            body: {
                collection: this.collectionName,
                filter: this.filter,
                update: this.updateQuery,
            }
            // TODO: where are the options?
        }
    }
}

class DeleteQueryImpl extends ExecutableImpl implements HivePlugin.Scripting.Executables.Database.DeleteQuery {
    constructor(private collectionName: string, private query: HivePlugin.JSONObject, private options: HivePlugin.Database.DeleteOptions, private output: boolean = false) {
        super("delete");
    }

    toJSON(): JSONObjectImpl {
        return {
            type: this.type,
            name: this.type,
            output: this.output,
            body: {
                collection: this.collectionName,
                filter: this.query,
            }
            // TODO: where are the options?
        }
    }
}

class ObjectIdImpl extends JSONObjectImpl implements HivePlugin.Database.ObjectId {
    // Matches the extended mongo JSON format {"$oid": "the_string_id"}
    constructor(public $oid: string) {
        super();
    }
}

class ScriptingImpl implements HivePlugin.Scripting.Scripting {
    constructor(private vault: VaultImpl) {}

    async setScript(functionName: string, executionSequence: AggregatedExecutableImpl, accessCondition?: ConditionImpl): Promise<boolean> {
        let executableAsJson = executionSequence ? executionSequence.toJSON() : null;
        let conditionAsJson = accessCondition ? accessCondition.toJSON() : null;
        let result = await execAsPromise<{success:boolean}>("scripting_setScript", [this.vault.objectId, functionName, executableAsJson, conditionAsJson]);
        return result.success;
    }

    call(functionName: string, params?: HivePlugin.JSONObject, appDID?: string): Promise<HivePlugin.JSONObject> {
        return execAsPromise<HivePlugin.JSONObject>("scripting_call", [this.vault.objectId, functionName, params, appDID]);
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
        if (!json)
            return null;

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

class ClientImpl implements HivePlugin.Client {
    objectId: string;

    async getVault(vaultOwnerDid: string): Promise<HivePlugin.Vault> {
        let vaultJson = await execAsPromise<HivePlugin.JSONObject>("client_getVault", [this.objectId, vaultOwnerDid]);
        return VaultImpl.fromJson(vaultJson);
    }

    static fromJson(json: HivePlugin.JSONObject): ClientImpl {
        let client = new ClientImpl();
        Object.assign(client, json);
        return client;
    }
}

class HiveManagerImpl implements HivePlugin.HiveManager {
    Database: {
        newObjectId: (_id: string) => HivePlugin.Database.ObjectId;
    };

    Scripting: {
        Conditions: {
            newAndCondition: (conditions: HivePlugin.Scripting.Conditions.Condition[]) => HivePlugin.Scripting.Conditions.AndCondition;
            newOrCondition: (conditions: HivePlugin.Scripting.Conditions.Condition[]) => HivePlugin.Scripting.Conditions.OrCondition;

            Database: {
                newQueryHasResultsCondition: (collectionName: string, queryParameters: HivePlugin.JSONObject) => HivePlugin.Scripting.Conditions.Database.QueryHasResultsCondition;
            }
        },

        Executables: {
            newAggregatedExecutable: (executables: HivePlugin.Scripting.Executables.Executable[]) => HivePlugin.Scripting.Executables.AggregatedExecutable;

            Database: {
                newFindOneQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean) => HivePlugin.Scripting.Executables.Database.FindOneQuery;
                newFindManyQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean) => HivePlugin.Scripting.Executables.Database.FindManyQuery;
                newInsertQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions, output?: boolean) => HivePlugin.Scripting.Executables.Database.InsertQuery;
                newUpdateQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions, output?: boolean) => HivePlugin.Scripting.Executables.Database.UpdateQuery;
                newDeleteQuery: (collectionName: String, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions, output?: boolean) => HivePlugin.Scripting.Executables.Database.DeleteQuery;
            }
        };
    };

    constructor() {
        Object.freeze(HiveManagerImpl.prototype);
        Object.freeze(VaultImpl.prototype);
        Object.freeze(DatabaseImpl.prototype);
        Object.freeze(FilesImpl.prototype);
        Object.freeze(ScriptingImpl.prototype);
        Object.freeze(InsertOneResultImpl.prototype);
        Object.freeze(InsertManyResultImpl.prototype);
        Object.freeze(UpdateResultImpl.prototype);
        Object.freeze(DeleteResultImpl.prototype);

        this.Database = {
            newObjectId: function(_id: string): HivePlugin.Database.ObjectId {
                return new ObjectIdImpl(_id);
            }
        };

        this.Scripting = {
            Conditions: {
                newAndCondition: function(conditions: ConditionImpl[]): HivePlugin.Scripting.Conditions.AndCondition {
                    return new AndConditionImpl(conditions);
                },

                newOrCondition: function(conditions: ConditionImpl[]): HivePlugin.Scripting.Conditions.OrCondition {
                    return new OrConditionImpl(conditions);
                },

                Database: {
                    newQueryHasResultsCondition: function(collectionName: string, queryParameters: HivePlugin.JSONObject): HivePlugin.Scripting.Conditions.Database.QueryHasResultsCondition {
                        return new QueryHasResultsCondition(collectionName, queryParameters);
                    }
                }
            },

            Executables: {
                newAggregatedExecutable: function(executables: ExecutableImpl[]): HivePlugin.Scripting.Executables.AggregatedExecutable {
                    return new AggregatedExecutableImpl(executables);
                },

                Database: {
                    newFindOneQuery: function(collectionName: string, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean): HivePlugin.Scripting.Executables.Database.FindOneQuery {
                        return new FindOneQueryImpl(collectionName, query, options, output);
                    },

                    newFindManyQuery: function(collectionName: string, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean): HivePlugin.Scripting.Executables.Database.FindOneQuery {
                        return new FindManyQueryImpl(collectionName, query, options, output);
                    },

                    newInsertQuery: function(collectionName: string, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.InsertOptions, output?: boolean): HivePlugin.Scripting.Executables.Database.InsertQuery {
                        return new InsertQueryImpl(collectionName, query, options, output);
                    },

                    newUpdateQuery: function(collectionName: string, filter: HivePlugin.JSONObject, updateQuery: HivePlugin.JSONObject, options?: HivePlugin.Database.UpdateOptions, output?: boolean): HivePlugin.Scripting.Executables.Database.UpdateQuery {
                        return new UpdateQueryImpl(collectionName, filter, updateQuery, options, output);
                    },

                    newDeleteQuery: function(collectionName: string, query?: HivePlugin.JSONObject, options?: HivePlugin.Database.DeleteOptions, output?: boolean): HivePlugin.Scripting.Executables.Database.DeleteQuery {
                        return new DeleteQueryImpl(collectionName, query, options, output);
                    }
                }
            }
        };
    }

    async getClient(options: HivePlugin.ClientCreationOptions): Promise<HivePlugin.Client> {
        // Get the client instance
        let clientJson = await execAsPromise<HivePlugin.JSONObject>("getClient", [options]);
        let client = ClientImpl.fromJson(clientJson);

        // Setup the authentication challenge callback mechanism
        exec(async (jwtToken: string)=>{
            // When a challenge request is received, pass this to the app and wait for the async result.
            // This result is passed back to the hive SDK.
            let challengeResponseJwt = await options.authenticationHandler.authenticationChallenge(jwtToken);

            // Return the challenge response to the Hive SDK.
            exec(()=>{}, (err)=>{}, 'HivePlugin', "client_sendAuthHandlerChallengeResponse", [client.objectId, challengeResponseJwt]);
        }, (err: any)=>{
            console.error(err);
        }, 'HivePlugin', "client_setAuthHandlerChallengeCallback", [client.objectId]);

        return client;
    }

    getVaultAddress(ownerDid: string): Promise<string> {
        return execAsPromise<string>("client_getVaultAddress", [ownerDid]);
    }

    setVaultAddress(ownerDid: string, vaultAddress: string): Promise<void> {
        return execAsPromise<void>("client_setVaultAddress", [ownerDid, vaultAddress]);
    }
}

export = new HiveManagerImpl();
