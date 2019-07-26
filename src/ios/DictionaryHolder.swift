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

typealias HiveObjectInfo = Dictionary<String, String>

class DictionaryHolder<T> {
    private var dict = HiveObjectInfo()
    private let result: T

    init(_ result: T) {
        self.result = result
    }

    func get() -> HiveObjectInfo {
        return dict
    }

    func put(_ propId: String) -> DictionaryHolder<T> {
        if (result is HiveClientInfo) {
            put(propId, result as! HiveClientInfo)
        } else if (result is HiveDriveInfo) {
            put(propId, result as! HiveDriveInfo)
        } else if (result is  HiveDirectoryInfo) {
            put(propId, result as! HiveDirectoryInfo)
        } else if (result is  HiveFileInfo) {
            put(propId, result as! HiveFileInfo)
        } else if (result is  HiveItemInfo) {
            put(propId, result as! HiveItemInfo)
        } else if (result is  HiveChildren) {
            put(propId, result as! HiveChildren)
        }
        return self
    }

    private func put(_ propId: String, _ info: HiveClientInfo) {
        if (info.hasKey(propId)) {
            dict[propId] = info.getValue(propId)
        }
    }

    private func put(_ propId: String, _ info: HiveDriveInfo) {
        if (info.hasKey(propId)) {
            dict[propId] = info.getValue(propId)
        }
    }

    private func put(_ propId: String, _ info: HiveDirectoryInfo) {
        if (info.hasKey(propId)) {
            dict[propId] = info.getValue(propId)
        }
    }

    private func put(_ propId: String, _ info: HiveFileInfo) {
        if (info.hasKey(propId)) {
            dict[propId] = info.getValue(propId)
        }
    }

    private func put(_ propId: String, _ info: HiveItemInfo) {
        if (info.hasKey(propId)) {
            dict[propId] = info.getValue(propId)
        }
    }

    private func put(_ propId: String, _ info: HiveChildren) {
        // TODO
    }
}
