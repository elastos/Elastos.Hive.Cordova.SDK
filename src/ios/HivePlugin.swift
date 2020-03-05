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

    private var clientIndex = 0
    private var ipfsIndex = 0
    private var filesIndex = 0
    private var keyValuesIndex = 0

    internal var loginCallbackId:  String = ""
    internal var resultCallbackId: String = ""

    var callbackId: String = ""

    @objc func initVal(_ command: CDVInvokedUrlCommand) {
    }

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
        self.success(command, retAsString: "ElastosHiveSDK-v0.2");
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

    @objc func disconnect(_ command: CDVInvokedUrlCommand) {
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

        guard let _ = ipfs else {
            self.error(command, retAsString: "can not get IPFS interface")
            return
        }

        let ipfsId = ipfsIndex
        ipfsIndex += 1
        ipfsMap[ipfsId] = ipfs

        let ret: Dictionary<String, Any> = ["id": ipfsId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func getFiles(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!
        let files = client.asFiles()

        guard let _ = files else {
            self.error(command, retAsString: "can not get Files interface")
            return
        }

        let filesId = filesIndex
        filesIndex += 1
        filesMap[filesId] = files

        let ret: Dictionary<String, Any> = ["id": filesId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func getKeyValues(_ command: CDVInvokedUrlCommand) {
        let clientId = command.arguments[0] as? Int ?? 0
        let client = clientMap[clientId]!
        let keyValues = client.asKeyValues()

        guard let _ = keyValues else {
            self.error(command, retAsString: "can not get KeyValues interface")
            return
        }

        let keyValuesId = keyValuesIndex
        keyValuesIndex += 1
        keyValuesMap[keyValuesId] = keyValues

        let ret: Dictionary<String, Any> = ["id": keyValuesId]
        self.success(command, retAsDict: ret as NSDictionary)
    }

    @objc func putStringByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""
        let data = command.arguments[2] as? String ?? ""

        guard !remoteFile.isEmpty && !data.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        filesMap[filesId]!.putString(data, asRemoteFile: remoteFile)
        .done {
            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func getStringByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""

        guard !remoteFile.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        filesMap[filesId]!.getString(fromRemoteFile: remoteFile)
        .done { data in
            let ret = [
                    "status": "success",
                    "content": data
                ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func getSizeByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""

        guard !remoteFile.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        filesMap[filesId]!.sizeofRemoteFile(remoteFile)
        .done { size in
            let ret: Dictionary<String, Any> = [
                "status": "success",
                "length": size
            ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func deleteFileByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""

        guard !remoteFile.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        filesMap[filesId]!.deleteRemoteFile(remoteFile)
        .done { size in
            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func listFilesByFiles(_ command: CDVInvokedUrlCommand) {
        let filesId = command.arguments[0] as? Int ?? 0
        let remoteFile = command.arguments[1] as? String ?? ""

        guard !remoteFile.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }
        filesMap[filesId]!.listRemoteFiles()
        .done { fileList in
            let ret: Dictionary<String, Any> = [
                "status": "success",
                "fileList": fileList
            ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func pugStringByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let data = command.arguments[1] as? String ?? ""

        guard !data.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        ipfsMap[ipfsId]!.putString(data)
        .done { hash in
            let ret: Dictionary<String, Any> = [
                "status": "success",
                "value": hash
            ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func getStringByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let cid = command.arguments[1] as? Hash ?? ""

        guard !cid.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        ipfsMap[ipfsId]!.getString(fromRemoteFile: cid)
        .done { data in
            let ret = ["status": "success",
                       "content": data]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func getSizeByIPFS(_ command: CDVInvokedUrlCommand) {
        let ipfsId = command.arguments[0] as? Int ?? 0
        let cid = command.arguments[1] as? Hash ?? ""

        guard !cid.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        ipfsMap[ipfsId]!.sizeofRemoteFile(cid)
        .done { size in
            let ret: Dictionary<String, Any> = [
                "status": "success",
                "length": size
            ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func putValueByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let val  = command.arguments[2] as? String ?? ""

        guard !key.isEmpty && !val.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        keyValuesMap[kvId]!.putValue(val, forKey: key)
        .done {
            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func setValueByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""
        let val  = command.arguments[2] as? String ?? ""

        guard !key.isEmpty && !val.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        keyValuesMap[kvId]!.setValue(val, forKey: key)
        .done {
            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func getValuesByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""

        guard !key.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        keyValuesMap[kvId]!.values(ofKey: key)
        .done { dataList in
            var valueList = Array<String>()
            for data in dataList {
                valueList.append(String(data: data, encoding: .utf8)!)
            }
            let ret: Dictionary<String, Any> = [
                "status": "success",
                "valueList": valueList
            ]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }

    @objc func deleteKeyByKV(_ command: CDVInvokedUrlCommand) {
        let kvId = command.arguments[0] as? Int ?? 0
        let key  = command.arguments[1] as? String ?? ""

        guard !key.isEmpty else {
            self.error(command, retAsString: "invalid arguments")
            return
        }

        keyValuesMap[kvId]!.deleteValues(forKey: key)
        .done {
            let ret = ["status": "success"]
            self.success(command, retAsDict: ret as NSDictionary)
        }.catch { error in
            self.error(command, retAsString: error.localizedDescription)
        }
    }
}
