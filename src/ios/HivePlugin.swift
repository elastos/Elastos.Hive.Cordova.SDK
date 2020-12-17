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

import Foundation
import ElastosHiveSDK

var clientAuthHandlerCompletionMap = Dictionary<String, Resolver<String>>()
class VaultHiveContext: HiveContext {
    var callbackId : String?
    var delegate: Any? = nil
    var clientObjectId: String?
    var authenticationDidDocument: DIDDocument?
    weak var hivePlugin: HivePlugin?

    init(hivePlugin: HivePlugin) {
        self.hivePlugin = hivePlugin
    }

    func getLocalDataDir() -> String {
        return hivePlugin!.getDataDir()
    }

    func getAppInstanceDocument() -> DIDDocument {
        return authenticationDidDocument!
    }

    /// This is the interface to make authorization from users, and it would be provided by application.
    func getAuthorization(_ jwtToken: String) -> Promise<String> {
        return Promise<String> { resolver in
            // Should normally not happen but that happen. In case we receive an invalid JWT, avoid crashing
            if jwtToken == "" {
                print("HivePlugin CRITICAL ERROR - Empty JWT token challenge received! Auth process cancelled and probably stuck.")
                return
            }

            clientAuthHandlerCompletionMap[callbackId!] = resolver
            let result: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus.ok, messageAs: jwtToken)
            result.setKeepCallbackAs(true)
            (self.delegate! as AnyObject).send(result, callbackId: self.callbackId)
        }
    }
}

private enum EnhancedErrorCodes : Int {
    // Vault errors - range -1 ~ -999
    case vaultNotFound = -1
    case providerNotPublished = -2
    case didNotPublished = -3

    // Database errors - range -1000 ~ -1999
    case collectionNotFound = -1000

    // File errors - range -2000 ~ -2999
    case fileNotFound = -2000

    case unspecified = -9999
}

@objc(HivePlugin)
class HivePlugin : TrinityPlugin {
    private var pluginInitialized = false
    private var safeRunLock = NSLock()
    private var clientMap = Dictionary<String, HiveClientHandle>()
    private var clientAuthHandlersMap   = Dictionary<String, HiveContext>()
    private var clientAuthHandlerCallbackMap = Dictionary<String, String>()
    private var vaultMap  = Dictionary<String, Vault>()
    private var readerMap  = Dictionary<String, FileReader>()
    private var writerMap   = Dictionary<String, FileWriter>()
    private var readerOffsetsMap   = Dictionary<String, Int>()
    private var didResolverInitialized: Bool = false

    @objc override func pluginInitialize() {
        pluginInitialized = true
    }

    @objc override func dispose() {
        // Make sure no callback is currently executing something
        safeRunLock.lock()
        pluginInitialized = false
        safeRunLock.unlock()

        for (key, authResponseFuture) in clientAuthHandlerCompletionMap {
            authResponseFuture.fulfill("")
            clientAuthHandlerCompletionMap[key] = nil
        }
    }

    /**
     * Ensures that the plugin is still running. If not, false is returned and the caller may not execute any code.
     * If true, it locks an execution context to make sure dispose() will not be called while executing such code while in a different thread.
     * This method is to be used from asynchronous callbacks.
     */
    private func safelyExecuteStart() -> Bool {
        if !pluginInitialized {
            return false
        }
        else {
            safeRunLock.lock()
            return true
        }
    }

    private func safelyExecuteEnd() {
        safeRunLock.unlock()
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsString: String) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK,
                                     messageAs: retAsString);

        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsDict: NSDictionary) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK,
                                     messageAs: (retAsDict as! [AnyHashable : Any]));

        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsPluginResult: CDVPluginResult) {
        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(retAsPluginResult, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsArray: NSArray) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: (retAsArray as! [Any]))

        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result, callbackId: command.callbackId)
    }

    @objc func successAsNil(_ command: CDVInvokedUrlCommand) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK)
        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result, callbackId: command.callbackId)
    }

    @objc func error(_ command: CDVInvokedUrlCommand, retAsString: String) {
        let result = CDVPluginResult(status: CDVCommandStatus_ERROR,
                                     messageAs: retAsString);

        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result, callbackId: command.callbackId)
    }

    private func createEnhancedError(code: EnhancedErrorCodes, message: String) -> [AnyHashable : Any] {
        var error = Dictionary<AnyHashable, Any>()
        error["code"] = code.rawValue
        error["message"] = message
        return error
    }

    /**
     * Returns the passed  error as a JSON object with a clear error code if we are able to know it. Otherwise,
     * the error description is returned as a string.
     */
    private func enhancedError(_ command: CDVInvokedUrlCommand, error: Error?) {
        let errorMessage: String
        var result: CDVPluginResult?

        if let error = error {
            errorMessage = error.localizedDescription

            if error is HiveError {
                let hiveErrorMessage = HiveError.description(error as! HiveError)
                if hiveErrorMessage.contains("collection not exist") {
                    result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: createEnhancedError(code: .collectionNotFound, message: hiveErrorMessage))
                }
                else {
                    switch error as! HiveError {
                    case .fileNotFound:
                        result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: createEnhancedError(code: .fileNotFound, message: hiveErrorMessage))
                        break
                    case .providerIsNil:
                        // TODO: differenciate "nil" because of network error VS really not published.
                        result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: createEnhancedError(code: .providerNotPublished, message: hiveErrorMessage))
                        break
                    default:
                        result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: createEnhancedError(code: .unspecified, message: hiveErrorMessage))
                    }
                }
            }
        }
        else {
            // Error is nil
            errorMessage = "Empty error content - no additional information"
        }

        if result == nil {
            result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorMessage)
        }

        // Command delegate can become nil in case the pluign is deinitialized while
        // doing an asynchronous call.
        self.commandDelegate?.send(result!, callbackId: command.callbackId)
    }

    func getDataDir() -> String {
        return getDataPath()
    }

    private func getDIDResolverUrl() -> String {
        return PreferenceManager.getShareInstance().getDIDResolver()
    }

    @objc func setupDIDResolver() throws {
        guard !didResolverInitialized else {
            return
        }
        try HiveClientHandle.setupResolver(getDIDResolverUrl(), "\(NSHomeDirectory())/Library/Caches/didCache") //暂时拿不到appManager 先写死
        didResolverInitialized = true
    }

    @objc func getClient(_ command: CDVInvokedUrlCommand) {
        let optionsJson = command.arguments[0] as? Dictionary<String, Any> ?? nil

        guard optionsJson != nil else {
            self.error(command, retAsString: "Client creation options must be provided")
            return
        }

        do {
            try setupDIDResolver()

            // Enable HTTP calls logging
            ElastosHiveSDK.Log.setLevel(.Debug)

            // final atomic reference as a way to pass our non final client Id to the auth handler.
            var clientIdReference: [String] = [String]()

            // Set the authentication DID document
            let authDIDDocumentJson = optionsJson!["authenticationDIDDocument"]
            let authenticationDIDDocument = try DIDDocument.convertToDIDDocument(fromJson: authDIDDocumentJson as! String)

            // Create a authentication handler
            let hiveContext = VaultHiveContext(hivePlugin: self)
            hiveContext.authenticationDidDocument = authenticationDIDDocument
            hiveContext.delegate = self.commandDelegate
            let client = try HiveClientHandle.createInstance(withContext: hiveContext)
            let clientId = "\(client.hashValue)"
            clientIdReference.append(clientId)
            clientMap[clientId] = client

            // Save the handler for later use
            clientAuthHandlersMap[clientId] = hiveContext
            let ret = ["objectId": clientId]
            print("getClient result: \(clientId)")
            self.success(command, retAsDict: ret as NSDictionary)
        } catch {
            self.enhancedError(command, error: error)
        }
    }

    @objc func client_setAuthHandlerChallengeCallback(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        // Save current callback content to be able to call it back when an authentication is requests by the hive SDK
        let hiveContext: VaultHiveContext = clientAuthHandlersMap[clientObjectId] as! VaultHiveContext
        hiveContext.callbackId = command.callbackId
        clientAuthHandlerCallbackMap[clientObjectId] = command.callbackId
        let result: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_NO_RESULT)
        result.setKeepCallbackAs(true)
        self.success(command, retAsPluginResult: result)
    }

    @objc func client_sendAuthHandlerChallengeResponse(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        let challengeResponseJwt = command.arguments[1] as? String ?? ""
        guard challengeResponseJwt != "" else {
            self.error(command, retAsString: "Empty challenge response given!")
            return
        }

        // Retrieve the auth response callback and send the authentication JWT back to the hive SDK
        print("clientObjectId === \(clientObjectId)")
        let callbackId = clientAuthHandlerCallbackMap[clientObjectId]
        let authResponseFuture: Resolver<String> = clientAuthHandlerCompletionMap[callbackId!]!
        authResponseFuture.fulfill(challengeResponseJwt)
        clientAuthHandlerCompletionMap[callbackId!] = nil
        self.success(command, retAsDict: [: ])
    }

    @objc func client_createVault(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        let vaultOwnerDid = command.arguments[1] as? String
        let vaultProviderAddress = command.arguments[2] as? String

        if vaultOwnerDid == nil {
            self.error(command, retAsString: "createVault() cannot be called with a null string as vault owner DID")
            return
        }

        if vaultProviderAddress == nil {
            self.error(command, retAsString: "createVault() cannot be called with a null string as vault provider address")
            return
        }

        let client = clientMap[clientObjectId]
        _ = client?.createVault(vaultOwnerDid!, vaultProviderAddress!).done{ [self] vault in
            if safelyExecuteStart() {
                let vaultId = "\(vault.hashValue)"
                vaultMap[vaultId] = vault
                let ret = ["objectId": vaultId,
                           "vaultProviderAddress": vault.providerAddress,
                           "vaultOwnerDid": vaultOwnerDid]
                self.success(command, retAsDict: ret as NSDictionary)

                safelyExecuteEnd()
            }
        }.catch { error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func client_getVault(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        let vaultOwnerDid = command.arguments[1] as? String ?? ""
        if vaultOwnerDid == "" {
            self.error(command, retAsString: "getVault() cannot be called with a null string as vault owner DID")
            return
        }

        let client = clientMap[clientObjectId]
        _ = client?.getVault(vaultOwnerDid, nil).done{ [self] vault in
            if safelyExecuteStart() {
                let vaultId = "\(vault.hashValue)"
                vaultMap[vaultId] = vault
                let ret = ["objectId": vaultId,
                           "vaultProviderAddress": vault.providerAddress,
                           "vaultOwnerDid": vaultOwnerDid]
                self.success(command, retAsDict: ret as NSDictionary)

                safelyExecuteEnd()
            }
        }.catch{ error in
            if let hiveError = error as? HiveError {
                switch hiveError {
                    case .providerIsNil:
                        self.successAsNil(command)
                        break
                default:
                    self.enhancedError(command, error: error)
                }
            }
            else {
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func vault_getNodeVersion(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.nodeVersion().done { version in
                self.success(command, retAsString: version)
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func vault_revokeAccessToken(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            do {
                try vault!.revokeAccessToken()
            }
            catch let error {
                self.enhancedError(command, error: error)
            }
            self.successAsNil(command)
        }
    }

    @objc func database_createCollection(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let optionsJson = command.arguments[2] as? NSDictionary ?? nil
        let options = CreateCollectionOptions()

        if optionsJson != nil {
            // Nothing to do, no option handle for now.
        }

        let vault = vaultMap[vaultObjectId]
        guard vault != nil else {
            return
        }

        vault?.database.createCollection(collectionName, options: options).done{ success in
            let ret = ["created": success]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_deleteCollection(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        guard vault != nil else {
            return
        }

        vault?.database.deleteCollection(collectionName).done{ success in
            let ret = ["deleted": success]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_insertOne(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let documentJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        let options = InsertOptions()
        let vault = vaultMap[vaultObjectId]
        vault?.database.insertOne(collectionName, documentJson, options: options).done{ result in
            let ret = ["insertedId": result.insertedId()]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_insertMany(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let emptyArray: Array<Dictionary<String, Any>> = [ ]
        let documentArray = command.arguments[2] as? Array<Dictionary<String, Any>> ?? emptyArray
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict

        let options = InsertOptions()
        let vault = vaultMap[vaultObjectId]
        print("documentArray === \(documentArray)")
        if vault != nil {
            vault?.database.insertMany(collectionName, documentArray, options: options).done{ inserResult in
                let insertIds = inserResult.insertedIds()
                let ret = ["insertedIds": insertIds]
                self.success(command, retAsDict: ret as NSDictionary)
            }.catch{ error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func database_countDocuments(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let queryJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        let options = CountOptions()
        let vault = vaultMap[vaultObjectId]
        vault?.database.countDocuments(collectionName, queryJson, options: options).done{ count in
            let ret = ["count": count]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_findOne(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let queryJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        let options = HivePluginHelper.jsonFindOptionsToNative(optionsJson)
        let vault = vaultMap[vaultObjectId]
        vault?.database.findOne(collectionName, queryJson, options: options).done{ result in
            if result == nil {
                self.successAsNil(command)
            }
            else {
                self.success(command, retAsDict: result! as NSDictionary)
            }
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_findMany(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let queryJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        let options = HivePluginHelper.jsonFindOptionsToNative(optionsJson)
        let vault = vaultMap[vaultObjectId]
        vault?.database.findMany(collectionName, queryJson, options: options).done{ result in
            if result == nil {
                self.success(command, retAsArray: [])
            }
            else {
                self.success(command, retAsArray: result! as NSArray)
            }
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_updateOne(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let filterJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let updatequeryJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[4] as? Dictionary<String, Any> ?? emptyDict

        let options = HivePluginHelper.jsonUpdateOptionsToNative(optionsJson)
        let vault = vaultMap[vaultObjectId]
        vault?.database.updateOne(collectionName, filterJson, updatequeryJson, options: options).done{ result in
            let ret = ["matchedCount": result.matchedCount(),
                       "modifiedCount": result.modifiedCount(),
                       "upsertedCount": result.upsertedCount(),
                       "upsertedId": result.upsertedId()] as [String : Any]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_updateMany(_ command: CDVInvokedUrlCommand) {
        // For now, update one and update many seem to be totally identical on the client side.
        database_updateOne(command)
    }

    @objc func database_deleteOne(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let collectionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let filterJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let optionsJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict

        let options = HivePluginHelper.jsonDeleteOptionsToNative(optionsJson)
        let vault = vaultMap[vaultObjectId]
        vault?.database.deleteOne(collectionName, filterJson, options: options).done{ result in
            let ret = ["deletedCount": result.deletedCount]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func database_deleteMany(_ command: CDVInvokedUrlCommand) {
        // For now, delete one and delete many seem to be totally identical on the client side.
        database_deleteOne(command)
    }

    @objc func files_upload(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        vault?.files.upload(srcPath).done{ [self] writer in
            if safelyExecuteStart() {
                let objectId = "\(writer.hashValue)"
                writerMap[objectId] = writer
                let ret = ["objectId": objectId] as [String : Any]
                self.success(command, retAsDict: ret as NSDictionary)

                safelyExecuteEnd()
            }
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_download(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        vault?.files.download(srcPath).done{ [self] reader in
            if safelyExecuteStart() {
                let objectId = "\(reader.hashValue)"
                readerMap[objectId] = reader
                let ret = ["objectId": objectId] as [String : Any]
                self.success(command, retAsDict: ret as NSDictionary)

                safelyExecuteEnd()
            }
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_delete(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        vault?.files.delete(srcPath).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_move(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let dstPath = command.arguments[2] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.move(srcPath, dstPath).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_copy(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let dstPath = command.arguments[2] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.copy(srcPath, dstPath).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_hash(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.hash(srcPath).done{ [self] hash in
            self.success(command, retAsString: hash)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_list(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.list(srcPath).done{ [self] fileInfos in
            var jsonArray: Array<Dictionary<String, Any>> = []
            for info in fileInfos {
                jsonArray.append(HivePluginHelper.hiveFileInfoToPluginJson(info))
            }
            self.success(command, retAsArray: jsonArray as NSArray)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func files_stat(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.stat(srcPath).done{ [self] fileInfo in
            var jsonArray: Array<Dictionary<String, Any>> = []
            jsonArray.append(HivePluginHelper.hiveFileInfoToPluginJson(fileInfo))
            self.success(command, retAsArray: jsonArray as NSArray)
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func scripting_setScript(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let functionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]

        let executionSequenceJson = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let accessConditionJson = command.arguments[3] as? Dictionary<String, Any> ?? emptyDict
        var data = try? JSONSerialization.data(withJSONObject: accessConditionJson, options: [])
        let accessConditionJsonstr = String(data: data!, encoding: String.Encoding.utf8)
        let condition = RawCondition(accessConditionJsonstr!)
        data = try? JSONSerialization.data(withJSONObject: executionSequenceJson, options: [])
        let executionSequenceJsonstr = String(data: data!, encoding: String.Encoding.utf8)
        let executable = RawExecutable(executable: executionSequenceJsonstr!)

        let vault = vaultMap[vaultObjectId]
        vault?.scripting.registerScript(functionName, condition, executable).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            self.enhancedError(command, error: error)
        }
    }

    @objc func scripting_call(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let functionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let params = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict
        let appDID = command.arguments[3] as? String

        let vault = vaultMap[vaultObjectId]
        if vault != nil {
            vault?.scripting.callScript(functionName, params, appDID, Dictionary<String, Any>.self).done{ scriptOutput in
                self.success(command, retAsDict: scriptOutput as NSDictionary)
            }.catch{ error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func scripting_uploadFile(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let transactionId = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.scripting.uploadFile(transactionId).done{ [self] writer in
                if safelyExecuteStart() {
                    let objectId = "\(writer.hashValue)"
                    writerMap[objectId] = writer
                    let ret = ["objectId": objectId] as [String : Any]
                    self.success(command, retAsDict: ret as NSDictionary)

                    safelyExecuteEnd()
                }
            }.catch{ error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func scripting_downloadFile(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let transactionId = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.scripting.downloadFile(transactionId).done{ [self] reader in
                if safelyExecuteStart() {
                    let objectId = "\(reader.hashValue)"
                    readerMap[objectId] = reader
                    let ret = ["objectId": objectId] as [String : Any]
                    self.success(command, retAsDict: ret as NSDictionary)

                    safelyExecuteEnd()
                }
            }.catch{ error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getPricingInfo(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getPaymentInfo().done { pricingInfo in
                if let info = try? pricingInfo.serialize().toDict() {
                    self.success(command, retAsDict: info as NSDictionary)
                }
                else {
                    self.error(command, retAsString: "Invalid payment info received (json format)")
                }
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getPricingPlan(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let pricingPlanName = command.arguments[1] as? String

        guard pricingPlanName != nil else {
            self.error(command, retAsString: "payment_getPricingPlan(): Pricing plan cannot be empty")
            return
        }

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getPricingPlan(pricingPlanName!).done { pricingPlan in
                if let plan = try? pricingPlan.serialize().toDict() {
                    self.success(command, retAsDict: plan as NSDictionary)
                }
                else {
                    self.error(command, retAsString: "Invalid pricing plan received (json format)")
                }
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_placeOrder(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let pricingPlanName = command.arguments[1] as? String

        guard pricingPlanName != nil else {
            self.error(command, retAsString: "payment_placeOrder(): Pricing plan cannot be empty")
            return
        }

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.placeOrder(pricingPlanName!).done { orderId in
                self.success(command, retAsString: orderId)
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_payOrder(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let orderId = command.arguments[1] as? String

        guard orderId != nil else {
            self.error(command, retAsString: "payment_payOrder(): orderId cannot be empty")
            return
        }

        let transactionIDs = command.arguments[2] as? Array<String>

        guard transactionIDs != nil else {
            self.error(command, retAsString: "payment_payOrder(): transaction ids list cannot be empty")
            return
        }

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.payOrder(orderId!, transactionIDs!).done { success in
                var ret = Dictionary<String, Any>()
                ret["success"] = success
                self.success(command, retAsDict: ret as NSDictionary)
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getOrder(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let orderId = command.arguments[1] as? String

        guard orderId != nil else {
            self.error(command, retAsString: "payment_getOrder(): orderId cannot be empty")
            return
        }

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getOrder(orderId!).done { order in
                if let order = try? order.serialize().toDict() {
                    self.success(command, retAsDict: order as NSDictionary)
                }
                else {
                    self.error(command, retAsString: "Invalid order received (json format)")
                }
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getAllOrders(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getAllOrders().done { orders in
                var retOrders = Array<Dictionary<String, Any>>()
                for order in orders {
                    if let retOrder = try? order.serialize().toDict() {
                        retOrders.append(retOrder)
                    }
                }
                self.success(command, retAsArray: retOrders as NSArray)
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getActivePricingPlan(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getUsingPricePlan().done { activePlan in
                if let plan = try? activePlan.serialize().toDict() {
                    self.success(command, retAsDict: plan as NSDictionary)
                }
                else {
                    self.error(command, retAsString: "Invalid active plan received (json format)")
                }
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    @objc func payment_getPaymentVersion(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        if ensureValidVault(vault, command) {
            vault!.payment.getPaymentVersion().done { version in
                self.success(command, retAsString: version)
            }.catch { error in
                self.enhancedError(command, error: error)
            }
        }
    }

    private func ensureValidVault(_ vault: Vault?, _ command: CDVInvokedUrlCommand) -> Bool {
        if vault == nil {
            self.error(command, retAsString: "The passed vault is a null object...")
            return false
        }
        else {
            return true
        }
    }

    @objc func writer_write(_ command: CDVInvokedUrlCommand) {
        let writerObjectId = command.arguments[0] as? String ?? ""
        let data = command.arguments[1] as? Data

        do {
            if let _ = data {
            let writer = writerMap[writerObjectId]
                try writer?.write(data: data!, { error in
                    self.error(command, retAsString: HiveError.description(error))
                })
                self.success(command, retAsDict: ["success": "success"])
            }
            else {
                self.error(command, retAsString: "data is nil.")
            }
        } catch {
            self.enhancedError(command, error: error)
        }
    }

    @objc func writer_flush(_ command: CDVInvokedUrlCommand) {
        self.success(command, retAsDict: ["success": "success"])
    }

    @objc func writer_close(_ command: CDVInvokedUrlCommand) {
        let writerObjectId = command.arguments[0] as? String ?? ""
        let writer = writerMap[writerObjectId]
        writer!.close() { (success, error) in
            if (success) {
                self.success(command, retAsDict: ["success": "success"])
            }
            else {
                self.enhancedError(command, error: error)
            }

            self.writerMap.removeValue(forKey: writerObjectId)
        }
    }

    @objc func reader_read(_ command: CDVInvokedUrlCommand) {
        let readerObjectId = command.arguments[0] as? String ?? ""
        let bytesCount = command.arguments[1] as? Int ?? 0
        let reader = readerMap[readerObjectId]
        // Resume reading at the previous read offset
        var data: Data?
        while !reader!.didLoadFinish {
            data = reader!.read(bytesCount, { error in
                self.enhancedError(command, error: error)
            })
            if data != nil {
                break
            }
        }
        self.success(command, retAsString: data?.base64EncodedString() ?? "")
    }

    @objc func reader_readAll(_ command: CDVInvokedUrlCommand) {
        let readerObjectId = command.arguments[0] as? String ?? ""

        let reader = readerMap[readerObjectId]

        var data: Data?
        while !reader!.didLoadFinish {
            if let d = reader!.read({ error in
                self.enhancedError(command, error: error)
            }){
                data?.append(d)
            }
        }

        self.success(command, retAsString: data?.base64EncodedString() ?? "")
    }

    @objc func reader_close(_ command: CDVInvokedUrlCommand) {
        let readerObjectId = command.arguments[0] as? String ?? ""

        let reader = readerMap[readerObjectId]
        reader?.close() { (success, error) in
            if (success) {
                self.success(command, retAsString: "success")
            }
            else {
                self.enhancedError(command, error: error)
            }

            self.readerMap.removeValue(forKey: readerObjectId)
        }
    }
}
