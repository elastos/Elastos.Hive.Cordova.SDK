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
* ```typescript
* declare let hiveManager: HivePlugin.HiveManager;
* let client = await hiveManager.getClient(...);
* let myVault = client.getVault(myDid);
* myVault.getDatabase().insertOne(...);
* ```
*/

declare namespace HivePlugin {
    /** @hidden */
    type Opaque<T, K> = T & { __opaque__: K };
    /** @hidden */
    type Int = Opaque<number, 'Int'>;

    export interface JSONObject {
        [k:string]: JSONObject | JSONObject[] | string | number | boolean
    }

    //export type JSONObjectOrArray = JSONObject | JSONObject[];

    export namespace Files {
        /**
         * File reader to retrieve remote file data.
         */
        export interface Reader {
            /**
             * Reads at most bytesCount bytes from the file.
             */
            read(bytesCount: number): Promise<Uint8Array>;

            /**
             * Convenient way to read a whole file at once. This method may be used only
             * for small files.
             */
            readAll(): Promise<Blob>;

            /**
             * Closes and frees reader's resources.
             */
            close(): Promise<void>;
        }

        /**
         * File write to write then upload data into a remote file.
         */
        export interface Writer {
            /**
             * Appends the given data to the current file buffer.
             */
            write(data: Uint8Array): Promise<void>;

            /**
             * Flushes buffered data previously written with write() to the remote file.
             */
            flush(): Promise<void>;

            /**
             * Closes and frees writer's resources.
             */
            close(): Promise<void>;
        }

        /** Represents a file path on the back-end side. */
        export type FilePath = string;

        /** Represents a folder path on the back-end side. */
        export type FolderPath = string;

        /**
         * Type of a remote file or folder.
         */
        export const enum FileType {
            FILE = 0,
            FOLDER = 1
        }

        /**
         * File information about a remote file or folder.
         */
        export type FileInfo = {
            /** File name */
            name: string;
            /** Size of the file in bytes. Undefined for folders. */
            size?: number;
            /** Last modification date */
            lastModified?: Date;
            /** File or folder? */
            type: FileType;
        }

        export interface Files {
            /**
             * Initiates an upload sequence by returning a Write object that can be used to write
             * small file chunks. After writing, flush() must be called to actually send the data
             * remotely.
             */
            upload(path: FilePath): Promise<Writer>;

            /**
             * Initiates a download sequence by returning a Reader object that can be used to read
             * the downloaded file in chunks.
             *
             * In case parts of the file path don't exist yet (folder parts), they are automatically
             * created.
             */
            download(path: FilePath): Promise<Reader>;

            /**
             * Deletes a file, or a folder. In case the given path is a folder, deletion is recursive.
             */
            delete(path: FilePath | FolderPath): Promise<boolean>;

            /**
             * Moves (or renames) a file or a folder.
             */
            move(srcPath: FilePath | FolderPath, dstpath: FilePath | FolderPath): Promise<boolean>;

            /**
             * Copies a file or a folder (recursively).
             */
            copy(srcPath: FilePath | FolderPath, dstpath: FilePath | FolderPath): Promise<boolean>;

            /**
             * Returns the SHA256 hash of the given file
             */
            hash(path: FilePath): Promise<string>;

            /**
             * Returns the list of all files in a given folder.
             */
            list(path: FolderPath): Promise<FileInfo[]>;

            /**
             * Information about the target file or folder.
             */
            stat(path: FilePath | FolderPath): Promise<FileInfo>;
        }
    }

    namespace Database {
        /**
         * Options used during collection creation.
         */
        export type CreateCollectionOptions = {
            // Nothing supported for now
        }

        /**
         * Options used during collection deletion.
         */
        export type DeleteCollectionOptions = {
            // Nothing supported for now
        }

        /**
         * Options used during a call to count().
         */
        export type CountOptions = {
            /** Maximum number of results to count */
            limit?: number;
            /** Number of results to skip before starting counting */
            skip?: number;
        }

        /**
         * Options used for findOne(), findMany() operations.
         */
        export type FindOptions = {
            /** Maximum number of results to return */
            limit?: number;
            /** Number of results to skip in the matching list */
            skip?: number;
            /** Fields to be used (and direction) to sort the results */
            sort?: JSONObject;
            /** Fields to return. By default, all fields are returned */
            projection?: JSONObject;
        }

        /**
         * Options used for insertOne() operations.
         */
        export type InsertOptions = {
            // Nothing supported for now
        }

        /**
         * Options used for updateOne(), updateMany() operations.
         */
        export type UpdateOptions = {
            // Nothing supported for now
        }

        /**
         * Options used for delete() operations.
         */
        export type DeleteOptions = {
            // Nothing supported for now
        }

        /**
         * Result after a call to createCollection.
         */
        export type CreateCollectionResult = {
            /** Whether the collection could be created or not. */
            created: boolean;
        }

        /**
         * Result after a call to deleteCollection.
         */
        export type DeleteCollectionResult = {
            /** Whether the collection could be deleted or not. */
            deleted: boolean;
        }

        /**
         * Result after calls to insert operations.
         */
        export type InsertResult = {
            insertedIds: string[]
        }

        /**
         * Result after calls to update operations.
         */
        export type UpdateResult = {
            matchedCount: number;
            modifiedCount: number;
            upsertedCount: number;
            upsertedId: string;
        }

        /**
         * Result after calls to delete operations.
         */
        export type DeleteResult = {
            deletedCount: number;
        }

        /**
         * Equivalent of Mongo's ObjectId to be used in database queries.
         */
        export interface ObjectId extends JSONObject {}

        export interface Database {
            /**
             * Creates a new collection with the given name.
             */
            createCollection(collectionName: string, options?: CreateCollectionOptions): Promise<CreateCollectionResult>;

            /**
             * Deletes a collection.
             */
            deleteCollection(collectionName: string, options?: DeleteCollectionOptions): Promise<DeleteCollectionResult>;

            /**
             * Returns the number of documents matching the given query and options.
             */
            countDocuments(collectionName: string, query: JSONObject, options?: CountOptions): Promise<number>;

            /**
             * Queries the database for some specific documents based on the given query and returns at most
             * one document.
             */
            findOne(collectionName: string, query?: JSONObject, options?: FindOptions): Promise<JSONObject>;

            /**
             * Queries the database for some specific documents based on the given query and returns a list of
             * documents.
             *
             * @returns List of results matching the query
             */
            findMany(collectionName: string, query?: JSONObject, options?: FindOptions): Promise<JSONObject[]>;

            /**
             * Inserts a new document in the given collection, into current user's personal vault.
             */
            insertOne(collectionName: string, document: JSONObject, options?: InsertOptions): Promise<InsertResult>;

            /**
             * Updates at most one existing document based on the given query filter and using the given
             * update query, which can be a fully new document, or a partial update ($set).
             */
            updateOne(collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: UpdateOptions): Promise<UpdateResult>;

            /**
             * Updates all documents matching the given query filter, using the given
             * update query, which can be a fully new document, or a partial update ($set).
             */
            updateMany(collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: UpdateOptions): Promise<UpdateResult>;

            /**
             * Deletes at most one document based on the given deletion filter.
             */
            deleteOne(collectionName: string, filter: JSONObject, options?: DeleteOptions): Promise<DeleteResult>;

            /**
             * Deletes all documents matching the given deletion filter.
             */
            deleteMany(collectionName: string, filter: JSONObject, options?: DeleteOptions): Promise<DeleteResult>;
        }
    }

    export interface Authenticator {
        // TODO - How to manage DID auth?
        requestAuthentication(requestUrl: string): Promise<void>;
    }

    export namespace Scripting {
        export namespace Conditions {
            export namespace Database {
                /**
                 * Vault script condition to check if a database query returns results or not.
                 * This is a way for example to check is a user is in a group, if a message contains comments, if a user
                 * is in a list, etc.
                 */
                export interface QueryHasResultsCondition extends Condition {}
            }

            /**
             * Base interface for all vault script conditions.
             */
            export interface Condition {}

            /**
             * Vault script condition that succeeds if at least one of the contained conditions are successful.
             * Contained conditions are tested in the given order, and test stops as soon as one successful condition
             * succeeds.
             */
            export interface OrCondition extends Condition {}

            /**
             * Vault script condition that succeeds only if all the contained conditions are successful.
             */
            export interface AndCondition extends Condition {}
        }

        export namespace Executables {
            /**
             * Client side representation of back-end executables.
             * Executables are predefined, and are executed by the hive back-end when running vault scripts.
             * For example, a Database.FindQuery executable type will execute a mongo query and return a list of results.
             */
            export interface Executable {}

            /**
             * Convenient interface to store and serialize a sequence of executables.
             */
            export interface AggregatedExecutable extends Executable {}

            export namespace Database {
                /**
                 * Client side representation of a back-end execution that runs a mongo "find one" query and returns zero or one item
                 * as a result.
                 */
                export interface FindOneQuery extends Executable {}

                /**
                 * Client side representation of a back-end execution that runs a mongo "find" query and returns some items
                 * as a result.
                 *
                 * The hive back-end may truncate the number of returned results for performance reasons. Pagination
                 * must be handled on the application level.
                 */
                export interface FindManyQuery extends Executable {}

                /**
                 * Client side representation of a back-end execution that runs a mongo "insert one" query.
                 */
                export interface InsertQuery extends Executable {}

                /**
                 * Client side representation of a back-end execution that runs a mongo "update many" query.
                 */
                export interface UpdateQuery extends Executable {}

                /**
                 * Client side representation of a back-end execution that runs a mongo "delete many" query.
                 */
                export interface DeleteQuery extends Executable {}
            }
        }

        export interface Scripting {
            /**
             * Lets the vault owner register a script on his vault for a given app. The script is built on the client side, then
             * serialized and stored on the hive back-end. Later on, anyone, including the vault owner or external users, can
             * use Scripting.call() to execute one of those scripts and get results/data.
             */
            setScript(functionName: string, executable: Executables.Executable, accessCondition?: Conditions.Condition): Promise<boolean>;

            /**
             * Executes a previously registered server side script using Scripting.setScript(). Vault owner or external users are
             * allowed to call scripts on someone's vault.
             *
             * Call parameters (params field) are meant to be used by scripts on the server side, for example as injected parameters
             * to mongo queries. Ex: if "params" contains a field "name":"someone", then the called script is able to reference this parameter
             * using "$params.name".
             */
            call(functionName: string, params?: JSONObject): Promise<JSONObject>;
        }
    }

    namespace KeyValues {
        // TODO
    }

    interface Vault {
        /**
         * Vault provider address (carrier or http)
         */
        getVaultProviderAddress(): string;

        /**
         * DID string of this vault owner
         */
        getVaultOwnerDid(): string;

        /**
         * Gives access to database features on this vault.
         *
         * Returns null in case we are accessing a vault that is not the current user's.
         */
        getDatabase(): Database.Database;

        /**
         * Gives access to files features on this vault.
         */
        getFiles(): Files.Files;

        /**
         * Gives access to all vault scripting features on this vault.
         */
        getScripting(): Scripting.Scripting;
    }

    /**
     * Interface that must be implemented by dApps when creating a hive client instance.
     * It is used to receive an authentication challenge from a hive back-end and return a authentication
     * response.
     */
    interface AuthenticationHandler {
        /**
         * Called whenever the hive back-end needs to authenticate this client. The received JWT token
         * contains a nonce. The application must create a DID verifiable presentation that embeds a special
         * app identification credential (usually generated by the Identity dapp) and the nonce. This presentation
         * is then packaged as a response JWT, signed by the application instance DID, and returned to the
         * back-end. The back-end will then issue an access token that can be used to all future vault API calls.
         */
        authenticationChallenge(jwtToken: string): Promise<string>;
    }

    interface ClientCreationOptions {
        authenticationHandler: AuthenticationHandler;
        authenticationDIDDocument: string; // JSON String representing a DIDDocument object
    }

    interface Client {
        /**
         * Gets a reference to a personal vault or another user's vault.
         * The resulting Vault object is used to access vault features such as database, fils, scripting.
         *
         * This method resolves the vault and returns a vault object only if the initial handshake could be
         * completed. At this point, the caller is not authenticated yet. Authentication is part of another
         * step later on (authentication callback).
         *
         * To resolve the vault, the hive SDK first tried to resolve the address in the given user's did document
         * from ID chain. If nothing can be found, it checks if there is a locally mapped match, that was
         * set before using setVaultProvider() (for example in case a user doesn't want to publish his vault address).
         *
         * @param vaultOwnerDid: Target user DID for which we want to get vault access
         */
        getVault(vaultOwnerDid: string): Promise<Vault>;
    }

    interface HiveManager {
        Database: {
            newObjectId: (_id: string) => Database.ObjectId;
        }

        Scripting: {
            Conditions: {
                newAndCondition: (conditions: Scripting.Conditions.Condition[]) => Scripting.Conditions.AndCondition;
                newOrCondition: (conditions: Scripting.Conditions.Condition[]) => Scripting.Conditions.OrCondition;

                Database: {
                    newQueryHasResultsCondition: (collectionName: string, queryParameters: HivePlugin.JSONObject) => Scripting.Conditions.Database.QueryHasResultsCondition;
                }
            },

            Executables: {
                newAggregatedExecutable: (executables: Scripting.Executables.Executable[]) => Scripting.Executables.Executable;

                Database: {
                    newFindOneQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.FindOptions) => Scripting.Executables.Database.FindOneQuery;
                    newFindManyQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.FindOptions) => Scripting.Executables.Database.FindManyQuery;
                    newInsertQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.InsertOptions) => Scripting.Executables.Database.InsertQuery;
                    newUpdateQuery: (collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: HivePlugin.Database.UpdateOptions) => Scripting.Executables.Database.UpdateQuery;
                    newDeleteQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.DeleteOptions) => Scripting.Executables.Database.DeleteQuery;
                }
            }
        }

        /**
         * Gets the singleton hive client instance for this application context, base for all
         * further operations.
         */
        getClient(options: ClientCreationOptions): Promise<Client>;

        /**
         * Tries to find a vault address in the public DID document of the given user's DID.
         *
         * This API always tries to fetch this information from ID chain first (vault address published
         * publicly for this user) and falls back to the local DID/Vault mapping if it fails to resolve
         * from chain.
         *
         * After being able to resolve from chain, any previously set local mapping is deleted.
         */
        getVaultAddress(ownerDid: string): Promise<string>;

        /**
         * Locally maps the given owner DID with the given vault address. This is useful for example in case
         * a user doesn't publish his vault address on the ID chain, and shared it privately.
         */
        setVaultAddress(ownerDid: string, vaultAddress: string): Promise<void>;
    }
}
