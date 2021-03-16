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

    // Can't use const enums in shared modules.
    export type EnhancedErrorType =
        // Vault errors - range -1 ~ -999
        "VAULT_NOT_FOUND" |
        "PROVIDER_NOT_PUBLISHED" | // No vault provider information inside a DID document
        "DID_NOT_PUBLISHED" |
        "INVALID_HIVE_URL_FORMAT" |

        // Database errors - range -1000 ~ -1999
        "COLLECTION_NOT_FOUND" |

        // File errors - range -2000 ~ -2999
        "FILE_NOT_FOUND" |

        // Unknown error
        "UNSPECIFIED";

    export class EnhancedError {
        getType(): EnhancedErrorType;
    }

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
             *
             * In order to display a downloaded hive picture as a HTML image, the following sample can be useful:
             * img.src = URL.createObjectURL(await hiveFileReader.readAll());
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
        export type FileType = "FILE" | "FOLDER";

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

    export namespace Payment {
        export class PaymentSettings {
            /**
             * Crypto address that receives payments for placed orders.
             */
            getReceivingELAAddress(): Promise<string>;
        }

        export class PricingInfo {
            /**
             * Returns the list of all available pricing plans
             */
            getPricingPlans(): PricingPlan[];

            /**
             * Returns generic information about payment for this vault on this vault provider.
             */
            getPaymentSettings(): PaymentSettings;
        }

        export class PricingPlan {
            /**
             * Returns the pricing plan name.
             */
            getName(): string;

            /**
             * Maximum storage size allowed for this plan. Write operations will fail is this quota is reached.
             *
             * @returns Max storage size, in megabytes.
             */
            getMaxStorage(): number;

            /**
             * Number of days during which this plan remains active after been purchased and started.
             */
            getDuration(): number;

            /**
             * Cost in currency, for the getDuration() days during which this plan is active.
             */
            getCost(): number;

            /**
             * Currency to be used to pay for this plan, together with getCost().
             */
            getCurrency(): string;
        }

        export type OrderState =
            "AWAITING_PAYMENT" |
            "AWAITING_TX_CONFIRMATION" |
            "TIMED_OUT_WHILE_WAITING_FOR_PAYMENT" |
            "TIMED_OUT_WHILE_WAITING_FOR_TX_CONFIRMATION" |
            "FAILED_UNSPECIFIED_REASON" |
            "COMPLETED";

        export class Order {
            /**
             * Returns the order unique ID.
             */
            getId(): string;

            /**
             * Returns the pricing plan for which this order was created.
             */
            getPricingPlan(): PricingPlan;

            /**
             * Returns the list of transactions created to pay for this order.
             * Those transactions can have been verified/confirmed, or not yet.
             */
            getPaymentTransactionIDs(): string[];

            /**
             * Current state of this order.
             */
            getState(): OrderState;

            /**
             * Timestamp (seconds since epoch) at which this order was created.
             */
            getCreationTime(): number;

            /**
             * Timestamp (seconds since epoch) at which the order was closed.
             * An order is closed after all payment transactions have been confirmed by the vault provider,
             * or if the order expired without payments.
             */
            getCompletionTime(): number;
        }

        export class ActivePricingPlan {
            /**
             * Returns the pricing plan name.
             */
            getName(): string;

            /**
             * Maximum storage size allowed for this plan. Write operations will fail is this quota is reached.
             *
             * @returns Max storage size, in megabytes.
             */
            getMaxStorage(): number;

            /**
             * Returns the disk space currently used by this vault's files, in megabytes.
             */
            getCurrentFileStorageUsed(): number;

            /**
             * Returns the disk space currently used by this vault's database, in megabytes.
             */
            getCurrentDatabaseStorageUsed(): number;

            // TODO
            modifyTime(): number;

            /**
             * Timestamp (seconds since epoch) at which the plan became active. This could be in the future compared
             * to the order date, in case the order is paid in advance.
             */
            getStartTime(): number;

            /**
             * Timestamp (seconds since epoch) at which the plan became inactive (ended).
             */
            getEndTime(): number;
        }

        export interface Payment {
            /**
            * Returns pricing information for the vault, on its current vault provider.
            * Pricing information includes the list of purchaseable plans and other settings for payments.
            */
            getPricingInfo(): Promise<PricingInfo>;

            /**
            * Retrieves a pricing plan by its name, among the list of all available pricing plans on this vault
            * provider, for the current vault.
            */
            getPricingPlan(pricingPlanName: string): Promise<PricingPlan>;

            /**
            * Places a new order for a given pricing plan. This is the first step required when
            * willing to activate a new pricing plan. Based on this created order, the required payments can
            * then be done by an external wallet and notified to the vault provider using payOrder().
            *
            * @returns The newly created order ID.
            */
            placeOrder(pricingPlanName: string): Promise<string>;

            /**
            * Notifies the hive provider that a payment has been issues for a previously created order.
            * Once the payment transactions are confirmed by the vault provider, the order will be completed
            * and the target plan will become active.
            *
            * @param transactionIDs List of transaction IDs created by an external wallet, for this order.
            *
            * @returns True if the operation was successful (TODO: clarify what this means), false otherwise.
            */
            payOrder(orderId: string, transactionIDs: string[]): Promise<boolean>;

            /**
            * Retrieves information about a previously created order.
            */
            getOrder(orderId: string): Promise<Order>;

            /**
            * Returns the list of all previously created orders for this vault, on the current vault provider.
            */
            getAllOrders(): Promise<Order[]>;

            /**
            * Returns the active pricing plan for this vault on this vault provider. Null in case there is no
            * active plan (though this should normally never happen - as a plan may always be required, even if
            * it is a free plan).
            */
            getActivePricingPlan(): Promise<ActivePricingPlan>;

            /**
            * Returns the version number for this payment module.
            */
            getPaymentVersion(): Promise<string>;
        }
    }

    export namespace Database {
        export namespace Exceptions {
            /** The requested collection was not found */
            export interface CollectionNotFoundException extends Error {}
        }

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
         * Result after calls to insertOne() operations.
         */
        export type InsertOneResult = {
            insertedId: string;
        }

        /**
         * Result after calls to insertMany() operations.
         */
        export type InsertManyResult = {
            insertedIds: string[];
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
             * Inserts a new document to the given collection, into current user's personal vault.
             */
            insertOne(collectionName: string, document: JSONObject, options?: InsertOptions): Promise<InsertOneResult>;

            /**
             * Inserts several new documents to the given collection, into current user's personal vault.
             */
            insertMany(collectionName: string, documents: JSONObject[], options?: InsertOptions): Promise<InsertManyResult>;

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

            export namespace Files {
                /**
                 * Client side representation of a back-end execution that will initiate a file upload operation
                 * and save it with the given name.
                 */
                export interface UploadExecutable extends Executable {}

                /**
                 * Client side representation of a back-end execution that will initiate a file download operation
                 * for an existing vault file that has the given name.
                 */
                export interface DownloadExecutable extends Executable {}
            }
        }

        export interface Scripting {
            /**
             * Lets the vault owner register a script on his vault for a given app. The script is built on the client side, then
             * serialized and stored on the hive back-end. Later on, anyone, including the vault owner or external users, can
             * use Scripting.call() to execute one of those scripts and get results/data.
             *
             * @param functionName Reference script name that will be used later with scripting call().
             * @param executable Simple or aggregated executables that are ran by the hive node, if the access condition is successful.
             * @param accessCondition Optional simple condition or aggregated conditions that are checked by the hive node when the script is called.
             * @param allowAnonymousUser Whether to allow un-authenticated users to call this script to another user's vault. This allows making some resources available for public use. False by default.
             * @param allowAnonymousApp Whether another application than the one registering the script is allowed to call the script or not. False by default.
             */
            setScript(functionName: string, executable: Executables.Executable, accessCondition?: Conditions.Condition, allowAnonymousUser?: boolean, allowAnonymousApp?: boolean): Promise<boolean>;

            /**
             * Executes a previously registered server side script using Scripting.setScript(). Vault owner or external users are
             * allowed to call scripts on someone's vault.
             *
             * Call parameters (params field) are meant to be used by scripts on the server side, for example as injected parameters
             * to mongo queries. Ex: if "params" contains a field "name":"someone", then the called script is able to reference this parameter
             * using "$params.name".
             *
             * The appDID parameter is used to override the default app did target that is (optionally) embedded in the authentication
             * challenge response. This allows calling scripts from other application contexts in case they chose to made their
             * script accessible anonimously.
             */
            call(functionName: string, params?: JSONObject, appDID?: string): Promise<JSONObject>;

            /**
             * After calling a script that runs a DownloadExecutable, a call() to that script returns a transaction_id in its
             * response. That transaction_id can be used with downloadFile() to download a file that is provided by the
             * script output.
             */
            downloadFile(transactionId: string): Promise<Files.Reader>;

            /**
             * After calling a script that runs a UploadExecutable, a call() to that script returns a transaction_id in its
             * response. That transaction_id can be used with uploadFile() to upload a file that has to be provided as part
             * of the script.
             */
            uploadFile(transactionId: string): Promise<Files.Writer>;
        }
    }

    namespace KeyValues {
        // TODO
    }

    interface Vault {
        /**
         *   provider address (carrier or http)
         */
        getVaultProviderAddress(): string;

        /**
         * DID string of this vault owner
         */
        getVaultOwnerDid(): string;

        /**
         * Returns the deployment version for this vault's vault provider (node).
         * This can be useful to maintain backward compatibility with older nodes.
         */
        getNodeVersion(): Promise<string>;

        /**
         * Gives access to the payment module that allows paying for vault usage, check active subscriptions
         * and quotas and more.
         *
         * Returns null in case we are accessing a vault that is not the current user's.
         */
        getPayment(): Payment.Payment;

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

        /**
         * Revokes the existing access token linking the current user with the vault. Next operation on the vault
         * after calling launches a new authentication flow in order to generate a new access token.
         */
        revokeAccessToken(): Promise<void>;
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
         * Creates a new vault for the target DID on the target vault provider.
         * This is a mandatory step prior to using a vault provider, otherwise all API calls return an error.
         *
         * @returns The created vault if it didn't exists, or null in case the vault already exists on this provider.
         */
        createVault(vaultOwnerDid: string, vaultProviderAddress: string): Promise<Vault>;

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

        /**
         * Parses a Hive standard url into a url info that can later be executed to get the result or the
         * target url.
         *
         * For example, later calling a url such as ...
         *      hive://userdid@appdid/getAvatar?params=
         *
         * ... results in a call to the "getAvatar" script, previously registered by "userdid" on his vault,
         * in the "appdid" scope. This is similar to calling:
         *      hiveClient.getVault(userdid).getScripting().call("getAvatar");
         *
         * Usage example (assuming the url is a call to a getAvatar script that contains a FileDownload
         * executable named "download"):
         *
         * - let hiveURLInfo = hiveclient.parseHiveURL(urlstring)
         * - let scriptOutput = await hiveURLInfo.callScript();
         * - hiveURLInfo.getVault().getScripting().downloadFile(scriptOutput.items["download"].getTransferID())
         */
        parseHiveURL(hiveURL: string): Promise<HiveURLInfo>;

        /**
         * Convenient method to directly get a URL result as JSON object, in case no advanced url information
         * is required.
         * This is a shortcut for client.parseHiveURL().callScript();
         */
        callScriptURL(scriptURL: string): Promise<JSONObject>;

        /**
         * Convenient method that first calls a script by url using callScriptURL(), and expects the
         * JSON output to contain a file download information. If this is the case, the file download is
         * starting and a file reader is returned.
         */
        downloadFileByScriptUrl(scriptURL: string): Promise<Files.Reader>;
    }

    /**
     * Information about a parsed hive url. Usually, this url is a a textual description that represents
     * a way to access a user's vault, call an existing script, with given parameters.
     *
     * This object also contains a reference to the vault
     */
    interface HiveURLInfo {
        /**
         * Calls a script represented by the parsed hive url.
         * Internally calls client.getVault().getScripting().call("scriptName", {params});
         */
        callScript(): Promise<JSONObject>;

        /**
         * Returns the vault targeted by the parsed url. Useful to be able to call consecutive actions following
         * the script call, such as a file download or upload.
         */
        getVault(): Promise<Vault>;
    }

    interface HiveManager {
        Database: {
            newObjectId: (_id: string) => Database.ObjectId;
        }

        Scripting: {
            Conditions: {
                newAndCondition: (conditions: Scripting.Conditions.Condition[], conditionName?: string) => Scripting.Conditions.AndCondition;
                newOrCondition: (conditions: Scripting.Conditions.Condition[], conditionName?: string) => Scripting.Conditions.OrCondition;

                Database: {
                    newQueryHasResultsCondition: (collectionName: string, queryParameters: HivePlugin.JSONObject, conditionName?: string) => Scripting.Conditions.Database.QueryHasResultsCondition;
                }
            },

            Executables: {
                newAggregatedExecutable: (executables: Scripting.Executables.Executable[], executableName?: string) => Scripting.Executables.Executable;

                Database: {
                    newFindOneQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean, executableName?: string) => Scripting.Executables.Database.FindOneQuery;
                    newFindManyQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.FindOptions, output?: boolean, executableName?: string) => Scripting.Executables.Database.FindManyQuery;
                    newInsertQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.InsertOptions, output?: boolean, executableName?: string) => Scripting.Executables.Database.InsertQuery;
                    newUpdateQuery: (collectionName: string, filter: JSONObject, updateQuery: JSONObject, options?: HivePlugin.Database.UpdateOptions, output?: boolean, executableName?: string) => Scripting.Executables.Database.UpdateQuery;
                    newDeleteQuery: (collectionName: string, query?: JSONObject, options?: HivePlugin.Database.DeleteOptions, output?: boolean, executableName?: string) => Scripting.Executables.Database.DeleteQuery;
                }

                Files: {
                    /**
                     * See newDownloadExecutable().
                     */
                    newUploadExecutable: (filePath: string, executableName?: string) => Scripting.Executables.Files.UploadExecutable;

                    /**
                     * Creates a new DownloadExecutable for setScript().
                     *
                     * filePath can be hardcoded (ex: "icons/userProfile.png"), or composed with values of call()
                     * parameters (ex: "icons/appIcon_${params.path}" + call() with a "path" field in params).
                     *
                     * Please note that using call() parameters can potentially be harmful if you make it too permissive, as
                     * a remote user could potentially access any file from your app storage. For example, we highly discourage
                     * registering "filePath:${params.path}" that would be very flexible from the application to download any file,
                     * but would also allow anyone pass an unexpected path and access other files. Also, it is suggested
                     * to use access conditions to verify that the caller is allowed to access a resoruce based on the target path.
                     */
                    newDownloadExecutable: (filePath: string, executableName?: string) => Scripting.Executables.Files.DownloadExecutable;
                }
            }
        }

        /**
         * Gets the singleton hive client instance for this application context, base for all
         * further operations.
         */
        getClient(options: ClientCreationOptions): Promise<Client>;

        /**
         * Tells if a javascript error is a specific hive error, and that error is of the given type.
         */
        errorOfType(error: any, errorType: HivePlugin.EnhancedErrorType): boolean;

        setDIDResolverUrl(resolver: string): Promise<void>;
    }
}
