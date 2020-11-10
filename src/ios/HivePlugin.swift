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
class VaultAuthenticator: Authenticator {

    var callbackId : String?
    var delegate: Any? = nil
    var clientObjectId: String?

    func requestAuthentication(_ jwtToken: String) -> HivePromise<String> {
        return HivePromise<String> { resolver in
            clientAuthHandlerCompletionMap[callbackId!] = resolver
            let result: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus.ok, messageAs: jwtToken)
            result.setKeepCallbackAs(true)
            (self.delegate! as AnyObject).send(result, callbackId: self.callbackId)
        }
    }
}

@objc(HivePlugin)
class HivePlugin : TrinityPlugin {
    private var clientMap = Dictionary<String, HiveClientHandle>()
    private var clientAuthHandlersMap   = Dictionary<String, Authenticator>()
    private var clientAuthHandlerCallbackMap = Dictionary<String, String>()
    private var vaultMap  = Dictionary<String, Vault>()
    private var readerMap  = Dictionary<String, OutputStream>()
    private var didResolverInitialized: Bool = false

    @objc func success(_ command: CDVInvokedUrlCommand, retAsString: String) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK,
                                     messageAs: retAsString);

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsDict: NSDictionary) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK,
                                     messageAs: (retAsDict as! [AnyHashable : Any]));

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsPluginResult: CDVPluginResult) {

        self.commandDelegate.send(retAsPluginResult, callbackId: command.callbackId)
    }

    @objc func success(_ command: CDVInvokedUrlCommand, retAsArray: NSArray) {
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: (retAsArray as! [Any]))

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func error(_ command: CDVInvokedUrlCommand, retAsString: String) {
        let result = CDVPluginResult(status: CDVCommandStatus_ERROR,
                                     messageAs: retAsString);

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    private func getDataDir() -> String {
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

            // final atomic reference as a way to pass our non final client Id to the auth handler.
            var clientIdReference: [String] = [String]()
            let options = HiveClientOptions()
            _ = options.setLocalDataPath(getDataDir())

            // Set the authentication DID document
            let authDIDDocumentJson = optionsJson!["authenticationDIDDocument"]
            let authenticationDIDDocument = try DIDDocument.convertToDIDDocument(fromJson: authDIDDocumentJson as! String)
            _ = options.setAuthenticationDIDDocument(authenticationDIDDocument)

            // Create a authentication handler
            let authHandler = VaultAuthenticator()
            authHandler.delegate = self.commandDelegate
            _ = options.setAuthenticator(authHandler)
            let client = try HiveClientHandle.createInstance(withOptions: options)
            let clientId = "\(client.hashValue)"
            clientIdReference.append(clientId)
            clientMap[clientId] = client

            // Save the handler for later use
            clientAuthHandlersMap[clientId] = authHandler
            let ret = ["objectId": clientId]
            print("getClient result: \(clientId)")
            self.success(command, retAsDict: ret as NSDictionary)
        } catch {
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

     @objc func client_setVaultAddress(_ command: CDVInvokedUrlCommand) {
        let ownerDid = command.arguments[0] as? String ?? ""
        let vaultAddress = command.arguments[1] as? String ?? ""
        HiveClientHandle.setVaultProvider(ownerDid, vaultAddress)
        self.success(command, retAsString: "success")
    }

    @objc func client_getVaultAddress(_ command: CDVInvokedUrlCommand) {
        let ownerDid = command.arguments[0] as? String ?? ""
        HiveClientHandle.getVaultProvider(ownerDid).done { address in
            self.success(command, retAsString: "success")
        }.catch { error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func client_setAuthHandlerChallengeCallback(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        // Save current callback content to be able to call it back when an authentication is requests by the hive SDK
        let auth: VaultAuthenticator = clientAuthHandlersMap[clientObjectId] as! VaultAuthenticator
        auth.callbackId = command.callbackId
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
        self.success(command, retAsDict: [: ])
    }

    @objc func client_getVault(_ command: CDVInvokedUrlCommand) {
        let clientObjectId = command.arguments[0] as? String ?? ""
        let vaultOwnerDid = command.arguments[1] as? String

        if vaultOwnerDid == nil {
            self.error(command, retAsString: "getVault() cannot be called with a null string as vault owner DID")
            return
        }

        let client = clientMap[clientObjectId]
        _ = client?.getVault(vaultOwnerDid!).done{ [self] vault in
            let vaultId = "\(vault.hashValue)"
            vaultMap[vaultId] = vault
            let ret = ["objectId": vaultId,
                       "vaultProviderAddress": vault.providerAddress,
                       "vaultOwnerDid": vaultOwnerDid]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            self.error(command, retAsString: error.localizedDescription)
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
            let ret = ["insertedIds": result.insertedId()]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            self.error(command, retAsString: error.localizedDescription)
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            self.success(command, retAsDict: result as NSDictionary)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            self.success(command, retAsArray: result as NSArray)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
                       "upsertedId": result.upsertedId()]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            let ret = ["deletedCount": result.deletedCount()]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func database_deleteMany(_ command: CDVInvokedUrlCommand) {
        // For now, delete one and delete many seem to be totally identical on the client side.
        database_deleteOne(command)
    }

    @objc func files_upload(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let dstPath = command.arguments[2] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        // TODO: CHECK
        vault?.files.upload(dstPath, asRemoteFile: srcPath).done{ result in
            self.success(command, retAsDict: ["success": result])
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func files_download(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        // TODO: CHECK
        vault?.files.download(srcPath).done{ [self] outstr in
            let objectId = "\(outstr.hashValue)"
            readerMap[objectId] = outstr
            self.success(command, retAsDict: ["objectId": objectId])
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func files_delete(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""
        let vault = vaultMap[vaultObjectId]
        vault?.files.delete(srcPath).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func files_hash(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let srcPath = command.arguments[1] as? String ?? ""

        let vault = vaultMap[vaultObjectId]
        vault?.files.hash(srcPath).done{ [self] hash in
            self.success(command, retAsString: hash)
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
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
        let executable = RawExecutable(executionSequenceJsonstr!)

        let vault = vaultMap[vaultObjectId]
        vault?.scripting.registerScript(functionName, condition, executable).done{ [self] success in
            self.success(command, retAsDict: ["success": success])
        }.catch{ error in
            if error is HiveError {
                let errstring =  HiveError.description(error as! HiveError)
                self.error(command, retAsString: errstring)
            }
            else
            {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func scripting_call(_ command: CDVInvokedUrlCommand) {
        let vaultObjectId = command.arguments[0] as? String ?? ""
        let functionName = command.arguments[1] as? String ?? ""
        let emptyDict: Dictionary<String, Any> = [: ]
        let params = command.arguments[2] as? Dictionary<String, Any> ?? emptyDict

        let vault = vaultMap[vaultObjectId]
//        vault?.scripting.call(functionName, params, String.self).done{ [self] success in
//            self.success(command, retAsDict: ["success": success])
//        }.catch{ error in
//            self.error(command, retAsString: error.localizedDescription)
//        }
    }
}
