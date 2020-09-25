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

import Foundation
import ElastosHiveSDK

#if false
class ResultHandler<T>: HiveCallback<T> {
    private var handlerId: Int
    private var callbackId: String
    private var commandDelegate: CDVCommandDelegate
    private var type: ResultType

    init(_ handlerId: Int, _ type: ResultType, _ callbackId: String, _ commandDelegate:CDVCommandDelegate) {
        self.handlerId = handlerId
        self.callbackId = callbackId
        self.commandDelegate = commandDelegate
        self.type = type

        super.init()
    }

    private func sendEvent(_ ret: Dictionary<String, Any>) {
        var dict = ret;
        dict["hid"] = handlerId;
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: dict as [AnyHashable : Any]);
        result?.setKeepCallbackAs(true);
        self.commandDelegate.send(result, callbackId: self.callbackId);
    }

    public override func didSucceed(_ result: T) -> Void {
        var ret: Dictionary<String, Any>?

        switch self.type {
        case .Void:
            ret = voidToDict(result as! Void)
        case .Length:
            ret = lengthToDict(result as! UInt64)
        case .Content:
            ret = contentToDict(result as! String)
        case .Data:
            ret = dataToDict(result as! Data)
        case .CID:
            ret = hashToDict(result as! Hash)
        case .FileList:
            ret = fileListToDict(result as! Array<String>)
        case .ValueList:
            ret = valueListToDict(result as! Array<Data>)
        }

        sendEvent(ret!)
    }

    public override func runError(_ error: HiveError) -> Void {
        var ret = Dictionary<String, Any>()
        ret["error"] = error.localizedDescription
        sendEvent(ret)
    }

    private func voidToDict(_ result: Void) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success"
        ]
        return dict
    }

    private func lengthToDict(_ result: UInt64) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "length": result
        ]
        return dict
    }

    private func contentToDict(_ result: String) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "content": result
        ]
        return dict
    }

    private func dataToDict(_ result: Data) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "content": String(data: result, encoding: .utf8)!
        ]
        return dict
    }

    private func hashToDict(_ result: String) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "cid": result
        ]
        return dict
    }

    private func fileListToDict(_ result: Array<String>) -> Dictionary<String, Any> {
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "fileList": result
        ]
        return dict
    }

    private func valueListToDict(_ result: Array<Data>) -> Dictionary<String, Any> {
        var values = Array<String>()
        for item in result {
            values.append(String(data: item, encoding: .utf8)!)
        }
        let dict: Dictionary<String, Any> = [
            "status": "success",
            "valueList": values
        ]
        return dict
    }

    enum ResultType: Int {
        case Void = 0
        case Length
        case Content
        case Data
        case CID
        case FileList
        case ValueList
    }
}
#endif