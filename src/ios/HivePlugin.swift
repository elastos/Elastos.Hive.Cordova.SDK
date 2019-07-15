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

typealias Client    = ElastosHiveSDK.Client
typealias Drive     = ElastosHiveSDK.Drive
typealias Directory = ElastosHiveSDK.Directory
typealias File      = ElastosHiveSDK.File

@objc(HivePlugin)
class HivePlugin : TrinityPlugin {

    //        let test = Test()

    let OK        = 0
    let CLIENT    = 1
    let DRIVE     = 2
    let DIRECTORY = 3
    let FILE      = 4

    var mClientDict    = [Int: Client]()
    var mDriveDict     = [Int: Drive]()
    var mDirectoryDict = [Int: Directory]()
    var mFileDict      = [Int: File]()

    var callbackId: String = ""

    var count: Int = 1;

    //    override init() {
    //        super.init();
    //    }

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
        case CLIENT:
        case DRIVE:
        case DIRECTORY:
        case FILE
            callbackId = command.callbackId
        default:
            self.error(command, retAsString: "Expected one non-empty let argument.")
        }

        // Don't return any result now
        let result = CDVPluginResult(status: CDVCommandStatus_NO_RESULT);
        result?.setKeepCallbackAs(true);
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func createObject(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getLastInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(mapId)
          if (ObjectMap.isClientMap(map)) {
              JSONObjectHolder<Client.Info> holder;
              Client.Info info;

              info = ObjectMap.toClientMap(map).get(objId).getInfo();
              if (info == null) {
                  callbackContext.error("no info");
                  return;
              }

              holder = new JSONObjectHolder<Client.Info>(info);
              holder.put(Client.Info.userId)
                    .put(Client.Info.name)
                    .put(Client.Info.email)
                    .put(Client.Info.phoneNo)
                    .put(Client.Info.region);

              callbackContext.success(holder.get());
              return;
          }

          if (ObjectMap.isDriveMap(map)) {
              JSONObjectHolder<Drive.Info> holder;
              Drive.Info info;

              info = ObjectMap.toDriveMap(map).get(objId).getInfo();
              holder = new JSONObjectHolder<Drive.Info>(info);
              holder.put(Drive.Info.driveId)
                    .put(Drive.Info.name);

              callbackContext.success(holder.get());
              return;
          }

          if (ObjectMap.isDirMap(map)) {
              JSONObjectHolder<Directory.Info> holder;
              Directory.Info info;

              info = ObjectMap.toDirMap(map).get(objId).getInfo();
              holder = new JSONObjectHolder<Directory.Info>(info);
              holder.put(Directory.Info.itemId)
                    .put(Directory.Info.name)
                    .put(Directory.Info.childCount);

              callbackContext.success(holder.get());
              return;
          }

          if (ObjectMap.isFileMap(map)) {
              JSONObjectHolder<File.Info> holder;
              File.Info info;

              info = ObjectMap.toDirMap(map).get(objId).getInfo();
              holder = new JSONObjectHolder<File.Info>(info);
              holder.put(File.Info.itemId)
                    .put(File.Info.name)
                    .put(File.Info.size);

              callbackContext.success(holder.get());
              return;
          }
        // TODO
    }

    @objc func getInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isClientMap(map)) {
            ObjectMap.toClientMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<Client.Info>(handlerId, resultCallbackCtxt)
            )
            return
        }

        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<Drive.Info>(handlerId, resultCallbackCtxt)
            )
            return
        }

        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<Directory.Info>(handlerId, resultCallbackCtxt)
            )
            return
        }

        if (ObjectMap.isFileMap(map)) {
            ObjectMap.toFileMap(map).get(objId).lastUpdatedInfo(
                handleBy: ResultHandler<File.Info>(handlerId, resultCallbackCtxt)
            )
        }
    }

    @objc func getDefDrive(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isClientMap(map)) {
            ObjectMap.toClientMap(map).get(objId).getDefaultDrive(
                ResultHandler<HiveDriveHandle>(hdrId, resultCallback)
            )
            return
        }
    }

    @objc func rootDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).rootDirectoryHandle(
                ResultHandler<HiveDirectoryHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func createDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).createDirectory(withPath: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).createDirectory(path,
                ResultHandler<HiveDirectoryHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func getDir(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).directoryHandle(atPath: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).directoryHandle(atName: path,
                handleBy: ResultHandler<HiveDirectoryHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func createFile(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).createFile(withPath: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).createFile(withName: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func getFile(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(mapId)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).fileHandle(atPath: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).fileHandle(atName: path,
                handleBy: ResultHandler<HiveFileHandle>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func getItemInfo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(map)
        if (ObjectMap.isDriveMap(map)) {
            ObjectMap.toDriveMap(map).get(objId).getItemInfo(path,
                handleBy: ResultHandler<HiveItemInfo>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func getChildren(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(map)
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).getChildren(path,
                handleBy: ResultHandler<HiveChildren>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func moveTo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(map)
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).moveTo(path,
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            ObjectMap.toFileMap(map).get(objId).moveTo(path,
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func copyTo(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0
        let  path = command.arguments[3] as? String ?? null

        let map: ObjectMap = ObjectMap.acquire(map)
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).copyTo(path,
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            ObjectMap.toFileMap(map).get(objId).copyTo(path,
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }

    @objc func deleteItem(_ command: CDVInvokedUrlCommand) {
        let mapId = command.arguments[0] as? Int ?? 0
        let objId = command.arguments[1] as? Int ?? 0
        let hdrId = command.arguments[2] as? Int ?? 0

        let map: ObjectMap = ObjectMap.acquire(map)
        if (ObjectMap.isDirMap(map)) {
            ObjectMap.toDirMap(map).get(objId).deleteItem(
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
        if (ObjectMap.isFileMap(map)) {
            ObjectMap.toFileMap(map).get(objId).deleteItem(
                handleBy: ResultHandler<HiveVoid>(hdrId, resultCallbackCtxt)
            )
            return
        }
    }
}
