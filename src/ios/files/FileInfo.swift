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

public class PlugInFileInfo: NSObject {

    var name: String?
    var size: Int = -1
    var lastModified: Date?
    var type: FileType?

    public override init() {

    }

    public class func fromJsonObject(_ json: Dictionary<String, Any>) -> PlugInFileInfo {
        let options = PlugInFileInfo()
        if let _ = json["name"] {
            options.name = (json["name"]! as! String)
        }
        if let _ = json["size"] {
            options.size = json["size"]! as! Int
        }
        if let _ = json["lastModified"] {
             options.lastModified = Date.getDateFromTimeStamp((json["lastModified"] as! Int))
        }
        if let _ = json["type"] {
            options.type = FileType.fromId(json["type"] as! Int)
        }

        return options
    }
}

extension Date {
    static func getDateFromTimeStamp(_ timeStamp: Int?) -> Date? {
        guard timeStamp != nil else {
            return nil
        }
        let interval = TimeInterval.init(timeStamp!)

        return Date(timeIntervalSince1970: interval)
    }
}

