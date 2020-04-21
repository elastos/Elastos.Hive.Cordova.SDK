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

@objc(HivePlugin)
class HivePlugin : TrinityPlugin {
    private static let LOGIN : Int = 1
    private static let RESULT: Int = 2

    private var clientMap = Dictionary<Int, HiveClientHandle>()
    private var ipfsMap   = Dictionary<Int, IPFSProtocol>()
    private var filesMap  = Dictionary<Int, FilesProtocol>()
    private var keyValuesMap = Dictionary<Int, KeyValuesProtocol>()

    private var clientIndex: Int = 1
    private var ipfsIndex: Int = 1
    private var filesIndex: Int = 1
    private var keyValuesIndex: Int = 1

    internal var loginCallbackId:  String = ""
    internal var resultCallbackId: String = ""

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

    @objc func error(_ command: CDVInvokedUrlCommand, retAsString: String) {
        let result = CDVPluginResult(status: CDVCommandStatus_ERROR,
                                     messageAs: retAsString);

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func getVersion(_ command: CDVInvokedUrlCommand) {
        self.success(command, retAsString: "ElastosHiveSDK-v1.0");
    }

    @objc func setListener(_ command: CDVInvokedUrlCommand) {
        let type = command.arguments[0] as? Int ?? 0

        switch (type) {
        case HivePlugin.LOGIN:
            loginCallbackId = command.callbackId

        case HivePlugin.RESULT:
            resultCallbackId = command.callbackId

        default:
            self.error(command, retAsString: "Expected one non-empty let argument.")
        }

        // Don't return any result now
        let result = CDVPluginResult(status: CDVCommandStatus_NO_RESULT);
        result?.setKeepCallbackAs(true);
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func createClient(_ command: CDVInvokedUrlCommand) {
        let dataDir = command.arguments[0] as? String ?? ""
        let options = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as! Int

        let client: HiveClientHandle? = ClientBuilder.createClient(dataDir, options, handlerId, loginCallbackId, self)
        guard client != nil else {
            self.error(command, retAsString: "Create client error")
            return
        }

        let clientId = clientIndex
        clientIndex += 1
        clientMap[clientId] = client

        let ret: NSDictionary = [ "clientId": clientId ]
        self.success(command, retAsDict: ret);
    }

    @objc func isConnected(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!

        let ret = ["isConnect": client.isConnected()]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func connect(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!

        DispatchQueue(label: "org.elastos.hive.queue", qos: .background, target: nil).async {
            do {
                try client.connect()

                let ret = ["status": "success"]
                self.success(command, retAsDict: ret as NSDictionary)
            } catch let error {
                self.error(command, retAsString: error.localizedDescription)
            }
        }
    }

    @objc func disConnect(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!

        DispatchQueue(label: "org.elastos.hive.queue", qos: .background, target: nil).async {
            client.disconnect()

            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary);
        }
    }

    @objc func getIPFS(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!
        let ipfs = client.asIPFS()

        let ipfsId = ipfsIndex
        ipfsIndex += 1
        ipfsMap[ipfsId] = ipfs

        let ret: Dictionary<String, Any> = ["ipfsId": ipfsId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func getFiles(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!
        let files = client.asFiles()

        let filesId = filesIndex
        filesIndex += 1
        filesMap[filesId] = files

        let ret: Dictionary<String, Any> = ["filesId": filesId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func getKeyValues(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!
        let keyValues = client.asKeyValues()

        let keyValuesId = keyValuesIndex
        keyValuesIndex += 1
        keyValuesMap[keyValuesId] = keyValues

        let ret: Dictionary<String, Any> = ["keyValuesId": keyValuesId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func putStringByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""
        let data = command.arguments[2] as? String ?? ""
        let handlerId = command.arguments[3] as? Int ?? 0

        _ = filesMap[filesId]!.putString(data, asRemoteFile: remoteFile,
                handler: ResultHandler<Void>(handlerId, .Void, self.resultCallbackId, self.commandDelegate))
    }

    @objc func getStringByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = filesMap[filesId]!.getString(fromRemoteFile: remoteFile,
                handler: ResultHandler<String>(handlerId, .Content, self.resultCallbackId, self.commandDelegate))
    }

    @objc func getSizeByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = filesMap[filesId]!.sizeofRemoteFile(remoteFile,
                handler: ResultHandler<UInt64>(handlerId, .Length, self.resultCallbackId, self.commandDelegate))
    }

    @objc func deleteFileByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = filesMap[filesId]!.deleteRemoteFile(remoteFile,
                handler: ResultHandler<Void>(handlerId, .Void, self.resultCallbackId, self.commandDelegate))
    }

    @objc func listFilesByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let handlerId = command.arguments[1] as? Int ?? 0

        _ = filesMap[filesId]!.listRemoteFiles(
                handler: ResultHandler<Array<String>>(handlerId, .FileList, self.resultCallbackId, self.commandDelegate))
    }

    @objc func putStringByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let data = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = ipfsMap[ipfsId]!.putString(data,
                handler: ResultHandler<Hash>(handlerId, .CID, self.resultCallbackId, self.commandDelegate))
    }

    @objc func getStringByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let cid = command.arguments[1] as? Hash ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = ipfsMap[ipfsId]!.getData(fromRemoteFile: cid,
                handler: ResultHandler<Data>(handlerId, .Data, self.resultCallbackId, self.commandDelegate))
    }

    @objc func getSizeByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let cid = command.arguments[1] as? Hash ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = ipfsMap[ipfsId]!.sizeofRemoteFile(cid,
                handler: ResultHandler<UInt64>(handlerId, .Length, self.resultCallbackId, self.commandDelegate))
    }

    @objc func putValueByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let val  = command.arguments[2] as? String ?? ""
        let handlerId = command.arguments[3] as? Int ?? 0

        _ = keyValuesMap[kvId]!.putValue(val, forKey: key,
                handler: ResultHandler<Void>(handlerId, .Void, self.resultCallbackId, self.commandDelegate))
    }

    @objc func setValueByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let val  = command.arguments[2] as? String ?? ""
        let handlerId = command.arguments[3] as? Int ?? 0

        _ = keyValuesMap[kvId]!.setValue(val, forKey: key,
                handler: ResultHandler<Void>(handlerId, .Void, self.resultCallbackId, self.commandDelegate))
    }

    @objc func getValuesByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = keyValuesMap[kvId]!.values(ofKey: key,
                handler: ResultHandler<Array<Data>>(handlerId, .ValueList, self.resultCallbackId, self.commandDelegate))
    }

    @objc func deleteKeyByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let handlerId = command.arguments[2] as? Int ?? 0

        _ = keyValuesMap[kvId]!.deleteValues(forKey: key,
                handler: ResultHandler<Void>(handlerId, .Void, self.resultCallbackId, self.commandDelegate))
    }
}
