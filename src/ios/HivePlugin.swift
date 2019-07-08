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

    @objc func test(_ command: CDVInvokedUrlCommand) {
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

    @objc func getInfo(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getLastInfo(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getDefaultDrive(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func rootDirectory(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func createDirectory(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getDirectory(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func createFile(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getFile(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getItemInfo(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func getChildren(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func moveTo(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func copyTo(_ command: CDVInvokedUrlCommand) {
        // TODO
    }

    @objc func deleteItem(_ command: CDVInvokedUrlCommand) {
        // TODO
    }
}
