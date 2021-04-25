/*
 * Copyright (c) 2021 Elastos Foundation
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

public class HivePluginHelper: NSObject {

    public class func jsonFindOptionsToNative(_ optionsJson: Dictionary<String, Any>) -> FindOptions {
        let options = FindOptions()
        if let limit = optionsJson["limit"] {
            _ = options.limit(limit as! Int)
        }
        if let skip = optionsJson["skip"] {
            _ = options.skip(skip as! Int)
        }
        if let sort = optionsJson["sort"] {
            _ = options.sort(HivePluginHelper.jsonSortFieldsToNative(sort as! Dictionary<String, Any>))
        }
        if let projection = optionsJson["projection"] {
            _ = options.projection(projection as! [String : Any])
        }
        return options
    }

    public class func jsonSortFieldsToNative(_ jsonSort: Dictionary<String, Any>) -> [VaultIndex] {
        var arrayIndex: Array<VaultIndex> = []
        let keys = jsonSort.keys
        for fieldName in keys {
            let order = jsonSort[fieldName]
            if order as! Int == 1 {
                arrayIndex.append(VaultIndex(fieldName, VaultIndex.Order.ASCENDING))
            }
            else {
                arrayIndex.append(VaultIndex(fieldName, VaultIndex.Order.DESCENDING))
            }
        }

        return arrayIndex
    }


    public class func jsonUpdateOptionsToNative(_ optionsJson: Dictionary<String, Any>) -> UpdateOptions {
        let options = UpdateOptions()
        return options
    }

    public class func jsonDeleteOptionsToNative(_ optionsJson: Dictionary<String, Any>) -> DeleteOptions {
        let options = DeleteOptions()
        return options
    }

    public class func hiveFileInfoToPluginJson(_ fileInfo: FileInfo) -> [String: Any] {
        var dict = ["name": fileInfo.name, "size": fileInfo.size as Any, "lastModified": fileInfo.lastModify] as [String : Any]
        if fileInfo.type == "FOLDER" {
            dict["type"] = 1
        }
        else {
            dict["type"] = 0
        }
        return dict
    }
}
