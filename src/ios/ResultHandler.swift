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

class ResultHandler<T>: HiveCallback<T> {
    private var handlerId: Int
    private var callbackId: String
    private var commandDelegate: CDVCommandDelegate

    init(_ handlerId: Int, _ callbackId: String, _ commandDelegate:CDVCommandDelegate) {
        self.handlerId = handlerId
        self.callbackId = callbackId
        self.commandDelegate = commandDelegate
        super.init()
    }

    private func sendEvent(_ ret: Dictionary<String, Any>) {
        var dict = ret;
        dict["did"] = handlerId;
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: dict as [AnyHashable : Any]);
        result?.setKeepCallbackAs(true);
        self.commandDelegate.send(result, callbackId: self.callbackId);
    }

    public override func didSucceed(_ result: T) -> Void {
        var ret:Dictionary<String, Any>?

        if (result is HiveDriveHandle) {
            ret = driveHandleToDict(result as! HiveDriveHandle)
        } else if (result is HiveDirectoryHandle) {
            ret = directoryHandleToDict(result as! HiveDirectoryHandle)
        } else if (result is HiveFileHandle) {
            ret =  fileHandleToDict(result as! HiveFileHandle)
        } else if (result is HiveClientInfo) {
            ret =  clientInfoToDict(result as! HiveClientInfo)
        } else if (result is HiveDriveInfo) {
            ret =  driveInfoToDict(result as! HiveDriveInfo)
        } else if (result is HiveDirectoryInfo) {
            ret =  directoryInfoToDict(result as! HiveDirectoryInfo)
        } else if (result is HiveFileInfo) {
            ret =  fileInfoToDict(result as! HiveFileInfo)
        } else if (result is HiveItemInfo) {
            ret =  itemInfoToDict(result as! HiveItemInfo)
        } else if (result is HiveChildren) {
            ret =  childrenToDict(result as! HiveChildren)
        } else if (result is Void) {
            ret =  voidToDict(result as! Void)
        }
        sendEvent(ret!)
    }

    public override func runError(_ error: HiveError) -> Void {
        var ret = Dictionary<String, Any>()
        ret["error"] = error.localizedDescription
        sendEvent(ret)
    }

    private func driveHandleToDict(_ drive: HiveDriveHandle) -> Dictionary<String, Any> {
        let map = ObjectMap<HiveClientHandle>.acquire(DRIVE)
        let objId = DriveMap.nextObjId()

        ObjectMap.asDriveMap(map).put(objId, drive)

        var ret = Dictionary<String, Any>()
        ret["id"] = objId
        return ret
    }

    private func directoryHandleToDict(_ dir: HiveDirectoryHandle) -> Dictionary<String, Any> {
        let map = ObjectMap<HiveClientHandle>.acquire(DIR)
        let objId = DriveMap.nextObjId()

        ObjectMap.asDirMap(map).put(objId, dir)

        var ret = Dictionary<String, Any>()
        ret["id"] = objId
        return ret
    }

    private func fileHandleToDict(_ file: HiveFileHandle) -> Dictionary<String, Any> {
        let map = ObjectMap<HiveClientHandle>.acquire(FILE)
        let objId = FileMap.nextObjId()

        ObjectMap.asFileMap(map).put(objId, file)

        var ret = Dictionary<String, Any>()
        ret["id"] = objId
        return ret
    }

    private func clientInfoToDict(_ info: HiveClientInfo) -> Dictionary<String, Any> {
        let holder = DictionaryHolder<HiveClientInfo>(info)
        return holder.put(HiveClientInfo.userId)
            .put(HiveClientInfo.name)
            .put(HiveClientInfo.email)
            .put(HiveClientInfo.phoneNo)
            .put(HiveClientInfo.region)
            .get()
    }

    private func driveInfoToDict(_ info: HiveDriveInfo) -> Dictionary<String, Any> {
        let holder = DictionaryHolder<HiveDriveInfo>(info)
        return holder.put(HiveDriveInfo.driveId).get()
    }

    private func directoryInfoToDict(_ info: HiveDirectoryInfo) -> Dictionary<String, Any> {
        let holder = DictionaryHolder<HiveDirectoryInfo>(info)
        return holder.put(HiveDirectoryInfo.itemId)
            .put(HiveDirectoryInfo.name)
            .put(HiveDirectoryInfo.childCount)
            .get()
    }

    private func fileInfoToDict(_ info: HiveFileInfo) -> Dictionary<String, Any> {
        let holder = DictionaryHolder<HiveFileInfo>(info)
        return holder.put(HiveFileInfo.itemId)
            .put(HiveFileInfo.name)
            .put(HiveFileInfo.size)
            .get()
    }

    private func itemInfoToDict(_ result: HiveItemInfo) -> Dictionary<String, Any> {
        // TODO
        return Dictionary<String, Any>()
    }

    private func childrenToDict(_ result: HiveChildren) -> Dictionary<String, Any> {
        // TODO
        return Dictionary<String, Any>()
    }

    private func voidToDict(_ result: Void) -> Dictionary<String, Any> {
        // TODO
        return Dictionary<String, Any>()
    }
}
