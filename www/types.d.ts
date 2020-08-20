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

/**
* Hive is the Elastos storage solution. It lets users deploy their own hive vaults in any location and 
* keep ownership of their data.
*
* Hive can manage database data, files, server side scripting a with limited set of instructions. 
* It uses DID to authenticate users and applications before giving access to data.
*
* <br><br>
* Declaration:
* <br>
* declare let hiveManager: HivePlugin.HiveManager;
* ...
* let hiveClient = await hiveManager.createClient(...);
*/

declare namespace HivePlugin {
    type Opaque<T, K> = T & { __opaque__: K };
    type Int = Opaque<number, 'Int'>;

    export class JSONObject {
        [k:string]: JSONObject | JSONObject[] | string | number | boolean
    }

    // TODO: what's the meaning of "remoteFile": url? path? string? What's the format?
    export interface Files {
        createFile(remoteFile: string): Promise<string>;

        // TODO: check byte[] type for data?
        upload(url: string, data: Uint8Array, remoteFile: string): Promise<void>;
    
        // TODO: should rename to download() ? 
        // TODO: what is the equivalent "outputstream" way in TS?
        downloader(remoteFile: string, outputStream: any /* TODO */): Promise<number>;
            
        deleteFile(remoteFile: string): Promise<void>;
        
        // TODO: folder "name" ? or path?
        createFolder(folder: string): Promise<void>;
    
        // TODO: src and dst formats? works for both files and folders?
        move(src: string, dst: string): Promise<void>;
        
        // TODO: src and dst formats? works for both files and folders? recursive?
        copy(src: string, dst: string): Promise<void>;
    
        // TODO: what's this for? Hashes but returns void ? Is this a java issue?
        // TODO CompletableFuture<Void> hash(String remoteFile);
    
        // TODO: folder name? path? -> rename
        list(folder: String): Promise<string[]>;
    
        size(remoteFile: string): Promise<number>;
    }
    
    namespace Database {
        export type CreateCollectionOptions = {
            // Nothing supported for now
        }

        export type CountOptions = {
            limit?: number;
            skip?: number;
        }

        export type FindOptions = {
            limit?: number;
            skip?: number;
            sort?: JSONObject | JSONObject[];
            projection?: JSONObject;
        }

        export type InsertOptions = {
            // Nothing supported for now
        }

        export type UpdateOptions = {
            // Nothing supported for now
        }

        export type DeleteOptions = {
            // Nothing supported for now
        }
        
        export type InsertResult = {
            insertedCount: number;
            insertedIds: string[]
        }

        export interface Database {
            /**
             * Creates a new collection with the given name.
             */
            createCollection(collectionName: string, options?: CreateCollectionOptions): Promise<void>;

            /**
             * Inserts a new document in the given collection, into current user's personal vault.
             * 
             * @returns The inserted entry ID
             */
            insertOne(collectionName: string, document: JSONObject, options?: InsertOptions): Promise<InsertResult>;

            /**
             * Returns the number of documents matching the given query and options.
             */
            countDocuments(collectionName: string, query: JSONObject, options?: CountOptions): Promise<number>;

            /**
             * Queries the database for some specific documents based on the given query.
             * 
             * @returns List of results matching the query
             */
            findOne(collectionName: string, query: JSONObject, options?: FindOptions): Promise<JSONObject>;
            findMany(collectionName: string, query: JSONObject, options?: FindOptions): Promise<JSONObject[]>;
            
            /**
             * Updates one or more existing documents based on the given query filter and using the given 
             * update query.
             */
            // TODO: update result type
            updateOne(collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: UpdateOptions): Promise<void>;
            updateMany(collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: UpdateOptions): Promise<void>;

            /**
             * Deletes one or more existing documents based on the given deletion filter.
             */
            // TODO: delete result type
            deleteOne(collectionName: string, filter: JSONObject, options?: DeleteOptions): Promise<void>;
            deleteMany(collectionName: string, filter: JSONObject, options?: DeleteOptions): Promise<void>;
        }
    }

    // TODO: what's this?
    export interface Authenticator {
        // TODO
        requestAuthentication(requestUrl: string);
    }

    export namespace Conditions {
        export namespace Database {
            /**
             * Vault script condition to check if a database query returns results or not.
             * This is a way for example to check is a user is in a group, if a message contains comments, if a user
             * is in a list, etc.
             */
            export interface QueryHasResultsCondition extends Condition {
                constructor(collectionName: string, queryParameters: JSONObject);
            }
        }

        /**
         * Base interface for all vault script conditions.
         */
        export interface Condition {
            toJSON(): JSONObject;
        }

        /**
         * Represents a sub-condition execution, previously registered in the ACL manager.
         * This way, several scripts can rely on simply the sub-condition name, without rewriting the condition content itself.
         */
        export interface SubCondition extends Condition {
            constructor(subConditionName: string);
        }

        /**
         * Vault script condition that succeeds if at least one of the contained conditions are successful.
         * Contained conditions are tested in the given order, and test stops as soon as one successful condition
         * succeeds.
         */
        export interface OrCondition extends Condition {
            constructor(conditions: Condition[]);
        }

        /**
         * Vault script condition that succeeds only if all the contained conditions are successful.
         */
        export interface AndCondition extends Condition {
            constructor(conditions: Condition[]);
        }
    }

    export interface Scripting {
        /**
         * Registers a sub-condition on the backend. Sub conditions can be referenced from the client side, by the vault owner,
         * while registering scripts using Scripting.setScript().
         */
        registerSubCondition(conditionName: string, condition: Conditions.Condition): Promise<void>;

        /**
         * Lets the vault owner register a script on his vault for a given app. The script is built on the client side, then
         * serialized and stored on the hive back-end. Later on, anyone, including the vault owner or external users, can
         * use Scripting.call() to execute one of those scripts and get results/data.
         */
        setScript(functionName: string, executionSequence: Executables.ExecutionSequence, accessCondition?: Conditions.Condition);

        /**
         * Executes a previously registered server side script using Scripting.setScript(). Vault owner or external users are
         * allowed to call scripts on someone's vault.
         *
         * Call parameters (params field) are meant to be used by scripts on the server side, for example as injected parameters
         * to mongo queries. Ex: if "params" contains a field "name":"someone", then the called script is able to reference this parameter
         * using "$params.name".
         */
        call(functionName: string, params?: JSONObject);
    }

    export namespace Executables {
        /**
         * Client side representation of back-end executables.
         * Executables are predefined, and are executed by the hive back-end when running vault scripts.
         * For example, a Database.FindQuery executable type will execute a mongo query and return a list of results.
         */
        export interface Executable {
            toJSON(): JSONObject;
        }

        /**
         * Convenient interface to store and serialize a sequence of executables.
         */
        // TODO: type or class?
        export type ExecutionSequence = {
            constructor(executables: Executable[]);

            toJSON(): JSONObject[];
        }

        export namespace Database {
            /**
             * Client side representation of a back-end execution that runs a mongo "find" query and returns some items
             * as a result.
             */
            // TODO IMPORTANT: how to deal with cursors here? If a script wants to return 1000 messages ?
            export class FindQuery {
                constructor(collectionName: String, query?: JSONObject);
            }

            /**
             * Client side representation of a back-end execution that runs a mongo "insert" query.
             */
            export class InsertQuery {
                constructor(collectionName: String, document: JSONObject);
            }
    
            /**
             * Client side representation of a back-end execution that runs a mongo "update" query, 
             * overwriting the whole target object with the new content.
             */
            // TODO: eve protocol is restricting us a bit too much here compared to mongo
            export class OverwriteQuery {
                constructor(collectionName: String, id: string, newItem: JSONObject);
            }

            /**
             * Client side representation of a back-end execution that runs a mongo "update" query, 
             * updating only specific fields of the document with the new content.
             */
            // TODO: eve protocol is restricting us a bit too much here compared to mongo
            export class UpdateQuery {
                constructor(collectionName: String, id: string, updatedFields: JSONObject);
            }
    
            // TODO: java sdk currently deletes one specific ID only, not by query
            // TODO: eve protocol is restricting us a bit too much here compared to mongo
            export class DeleteQuery {
                constructor(collectionName: String, deleteQuery: JSONObject);
            }
        }
    }

    interface VaultProviderBase {
        address: string; // Vault provider carrier or http address

        files: Files;
        scripts: Scripting;
    }

    export interface RemoteVaultProvider extends VaultProviderBase {
        constructor();
    }

    export interface OwnVaultProvider extends VaultProviderBase {
        database: Database.Database;

        constructor();
    }

    /**
     * TODO
     */
    interface Client {
        /**
         * Gives access to database features for this client.
         */
        getDatabase(): Promise<Database.Database>

        /**
         * Gives access to files features for this client.
         */
        getFiles(): Promise<Files>;

        /**
         * Gives access to all vault scripting features for this client.
         */
        getScripting(): Promise<Scripting>;
    }

    type ClientCreationOptions = {
        authenticator?: Authenticator;
    }

    interface HiveManager {
        /**
         * Creates a new instance of a hive client, base for every further operation in hive.
         */
        createClient(options: ClientCreationOptions): Promise<Client>;

        /**
         * Resolves the provider of the currently signed in user.
         */
        resolveOwnVaultProvider(): Promise<OwnVaultProvider>;

        /**
         * Resolves the provider of another user. The provider address must be located in the DID 
         * document of that user on the ID sidechain.
         */
        resolveRemoteProvider(userDID: string): Promise<RemoteVaultProvider>;
    }
}
