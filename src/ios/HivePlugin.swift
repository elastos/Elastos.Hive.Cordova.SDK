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
    internal static let LOGIN : Int = 1
    internal static let RESULT: Int = 2

    internal var loginCallbackId:  String = ""
    internal var resultCallbackId: String = ""

    internal var count: Int = 1

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
        self.success(command, retAsString: "HiveSDK-v0.0.1");
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

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(1)
        let client: HiveClientHandle? = ClientBuilder.createClient(dataDir, options)
        guard client != nil else {
            self.error(command, retAsString: "Client create error")
            return
        }

        let objId: Int = count
        ObjectMap.asClientMap(map).put(objId, client!)
        count += 1

        let ret: NSDictionary = [ "id": objId ]
        self.success(command, retAsDict: ret);
    }

    @objc func login(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        guard ObjectMap.isClientMap(map) else {
            return
        }

        let backgroundQueue = DispatchQueue(label: "org.elastos.hive.queue",
                                            qos: .background, target: nil)
        backgroundQueue.async {
            var ret = Dictionary<String, Any>()

            _ = try! ObjectMap.asClientMap(map).get(objId).login(
                LoginHandler(hdrId, self.loginCallbackId, self.commandDelegate)
            )

            ret["result"] = "success"
            self.success(command, retAsDict: ret as NSDictionary);
        }
    }

    @objc func logout(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        guard ObjectMap.isClientMap(map) else {
            return
        }

        let backgroundQueue = DispatchQueue(label: "org.elastos.hive.queue",
                                            qos: .background, target: nil)
        backgroundQueue.async {
            var ret = Dictionary<String, Any>()

            _ = try! ObjectMap.asClientMap(map).get(objId).logout()

            ret["result"] = "success"
            self.success(command, retAsDict: ret as NSDictionary);
        }
    }

    @objc func getLastInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isClientMap(map)) {
            var holder: DictionaryHolder<HiveClientInfo>
            var info: HiveClientInfo?

            info = ObjectMap.asClientMap(map).get(objId).lastInfo
            if (info == nil) {
                self.error(command, retAsString: "Expected one non-empty let argument.")
                return
            }

            holder = DictionaryHolder<HiveClientInfo>(info!)
            _ = holder.put(HiveClientInfo.userId)
                .put(HiveClientInfo.name)
                .put(HiveClientInfo.email)
                .put(HiveClientInfo.phoneNo)
                .put(HiveClientInfo.region)

            self.success(command, retAsDict: holder.get() as NSDictionary)
            return;
        }

        if (ObjectMap.isDriveMap(map)) {
            var holder: DictionaryHolder<HiveDriveInfo>
            var info: HiveDriveInfo

            info = ObjectMap.asDriveMap(map).get(objId).lastInfo
            holder = DictionaryHolder<HiveDriveInfo>(info)
            _ = holder.put(HiveDriveInfo.driveId)

            self.success(command, retAsDict: holder.get() as NSDictionary)
            return;
        }

        if (ObjectMap.isDirMap(map)) {
            var holder: DictionaryHolder<HiveDirectoryInfo>
            var info: HiveDirectoryInfo

            info = ObjectMap.asDirMap(map).get(objId).lastInfo!
            holder = DictionaryHolder<HiveDirectoryInfo>(info)
            _ =  holder.put(HiveDirectoryInfo.itemId)
                .put(HiveDirectoryInfo.name)
                .put(HiveDirectoryInfo.childCount)

            self.success(command, retAsDict: holder.get() as NSDictionary)
            return;
        }

        if (ObjectMap.isFileMap(map)) {
            var holder: DictionaryHolder<HiveFileInfo>
            var info: HiveFileInfo

            info = ObjectMap.asFileMap(map).get(objId).lastInfo!
            holder = DictionaryHolder<HiveFileInfo>(info)
            _ = holder.put(HiveFileInfo.itemId)
                .put(HiveFileInfo.name)
                .put(HiveFileInfo.size)

            self.success(command, retAsDict: holder.get() as NSDictionary)
            return;
        }
    }

    @objc func getInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isClientMap(map)) {
            _ = ObjectMap.asClientMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<HiveClientInfo>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }

        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<HiveDriveInfo>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }

        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<HiveDirectoryInfo>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }

        if (ObjectMap.isFileMap(map)) {
            _ = ObjectMap.asFileMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<HiveFileInfo>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
        }
    }

    @objc func getDefDrive(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isClientMap(map)) {
            _ = ObjectMap.asClientMap(map).get(objId).defaultDriveHandle(
                handleBy: ResultHandler<HiveDriveHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func rootDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).rootDirectoryHandle(
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func createDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let path  = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).createDirectory(withPath: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).createDirectory(withName: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func getDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).directoryHandle(atPath: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).directoryHandle(atName: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func createFile(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).createFile(withPath: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).createFile(withName: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func getFile(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).fileHandle(atPath: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).fileHandle(atName: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func getItemInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            _ = ObjectMap.asDriveMap(map).get(objId).getItemInfo(path,
                handleBy: ResultHandler<HiveItemInfo>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func getChildren(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).getChildren(
                handleBy: ResultHandler<HiveChildren>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func moveTo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).moveTo(newPath: path,
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            _ = ObjectMap.asFileMap(map).get(objId).moveTo(newPath: path,
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func copyTo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? ""

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).copyTo(newPath: path,
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            _ = ObjectMap.asFileMap(map).get(objId).copyTo(newPath: path,
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }

    @objc func deleteItem(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap<HiveClientHandle>.acquire(mapId)
        if (ObjectMap.isDirMap(map)) {
            _ = ObjectMap.asDirMap(map).get(objId).deleteItem(
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            _ = ObjectMap.asFileMap(map).get(objId).deleteItem(
                handleBy: ResultHandler<Void>(hdrId, self.resultCallbackId, self.commandDelegate)
            )
            return
        }
    }
}
