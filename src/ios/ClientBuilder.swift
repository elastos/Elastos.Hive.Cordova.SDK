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

class ClientBuilder {
    private static let NATIVE = 1
    private static let ONEDRIVE = 2
    private static let IPFS = 3

    private class func createForOneDrive(_ dataDir: String, _ option:Dictionary<String, String>) -> HiveClientHandle? {
        let entry = OAuthEntry(option["clientId"]!, option["scope"]!,option["redirectUrl"]!)
        HiveClientHandle.createInstance(OneDriveParameter(entry, dataDir))
        return HiveClientHandle.sharedInstance(type: .oneDrive)
    }

    private class func createForIPFS(_ dataDir: String, _ option: Dictionary<String, String>) -> HiveClientHandle? {
        let path = Bundle.main.path(forResource: "ipfsnodes", ofType: "json")
        let data = try! Data(contentsOf: URL(fileURLWithPath: path!))
        let json = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
        let ipfsNodes = json["ipfsnodes"] as! Array<Any>

        var array = [String]()
        for item in ipfsNodes {
            let node = item as! [String: String]
            array.append(node["addr"]!)
        }

        let entry = IPFSEntry(array)
        HiveClientHandle.createInstance(IPFSParameter(entry, dataDir))
        return HiveClientHandle.sharedInstance(type: .hiveIPFS)
    }

    class func createClient(_ dataDir: String, _ options: String) -> HiveClientHandle? {
        let jsonData = options.data(using: .utf8)
        let decodedDict: Dictionary<String, String> = (try! JSONSerialization.jsonObject(with: jsonData!, options: []) as? Dictionary<String, String>)!
        print("decodedJsonDict=\(decodedDict)")

        var client: HiveClientHandle?
        switch (Int(decodedDict["driveType"]!)) {
        case ONEDRIVE:
            client = createForOneDrive(dataDir, decodedDict)

        case IPFS:
            client = createForIPFS(dataDir, decodedDict)

        default:
            client = nil
        }
        return client
    }
}
