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

  package org.elastos.trinity.plugins.hive;

  import android.util.Base64;

  import org.apache.cordova.CordovaPlugin;
  import org.apache.cordova.CallbackContext;

  import org.apache.cordova.PluginResult;
  import org.elastos.trinity.runtime.TrinityPlugin;
  import org.json.JSONArray;
  import org.json.JSONException;
  import org.json.JSONObject;

  import java.util.List;
  import java.util.Map;
  import java.util.HashMap;

  import org.elastos.hive.*;
  import org.elastos.hive.exceptions.HiveException;

  /**
   * This class echoes a string called from JavaScript.
   */
  public class HivePlugin extends TrinityPlugin {

      private static String TAG = "HivePlugin";

      private static final int CLIENT    = 1;
      private static final int DRIVE     = 2;
      private static final int DIRECTORY = 3;
      private static final int FILE      = 4;

      private HashMap<Integer, Client> mClientMap;
      private HashMap<Integer, Drive> mDriveMap;
      private HashMap<Integer, Directory> mDirMap;
      private HashMap<Integer, File> mFileMap;

      private CallbackContext mCallbackContext = null;

      public HivePlugin() {
          mClientMap = new HashMap();
          mDriveMap  = new HashMap();
          mDirMap    = new HashMap();
          mFileMap   = new HashMap();
      }

      @Override
      public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
          try {
              switch(action) {
                  case "test":
                      this.test(args, callbackContext);
                      break;

                  case "getVersion":
                      this.getVersion(callbackContext);
                      break;

                  case "createObject":
                      this.createObject(args, callbackContext);
                      break;

                  case "getInfo":
                      this.getInfo(args, callbackContext);
                      break;

                  case "getLastInfo":
                      this.getLastInfo(args, callbackContext);
                      break;

                  case "getDefaultDrive":
                      this.getDefDrive(args, callbackContext);
                      break;

                  case "rootDirectory":
                      this.rootDirectory(args, callbackContext);
                      break;

                  case "createDirectory":
                      this.createDirectory(args, callbackContext);
                      break;

                  case "getDirectroy":
                      this.getDirectroy(args, callbackContext);
                      break;

                  case "createFile":
                      this.createFile(args, callbackContext);
                      break;

                  case "getFile":
                      this.createFile(args, callbackContext);
                      break;

                  case "getItemInfo":
                      this.getItemInfo(args, callbackContext);
                      break;

                  case "getChildren":
                      this.getChildren(args, callbackContext);
                      break;

                  case "moveTo":
                      this.moveTo(args, callbackContext);
                      break;

                  case "copyTo":
                      this.moveTo(args, callbackContext);
                      break;

                  case "deleteItem":
                      this.deleteItem(args, callbackContext);
                      break;

                  // TODO:

                  default:
                      return false;
              }
          }

          catch (HiveException e) {
             String error = String.format("%s error (0x%x)", action, e.getErrorCode());
              callbackContext.error(error);
          }
          return true;
      }

      private void test(JSONArray args, CallbackContext callbackContext) throws JSONException {
          String data = args.getString(0);
          byte[] rawData = Base64.decode(data, Base64.DEFAULT);
      }

      private void getVersion(CallbackContext callbackContext) {
          callbackContext.success("HiveSDK-v0.0.1");
      }

      private void createObject(JSONArray args, CallbackContext callbackContext) throws JSONException, HiveException {
          // TODO;
      }

      private void getInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getLastInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getDefDrive(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void rootDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void createDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void createFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getItemInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void getChildren(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void moveTo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void copyTo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }

      private void deleteItem(JSONArray args, CallbackContext callbackContext) throws JSONException {
          // TODO;
      }
  }
