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
  import org.elastos.hive.Void;
  import org.elastos.hive.File;
  import org.elastos.hive.HiveException;

  /**
   * This class echoes a string called from JavaScript.
   */
  public class HivePlugin extends TrinityPlugin {
      private static final int LOGIN  = 1;
      private static final int RESULT = 2;

      private CallbackContext loginCallbackCtxt  = null;
      private CallbackContext resultCallbackCtxt = null;

      @Override
      public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
          try {
              switch(action) {
              case "getVersion":
                  this.getVersion(args, callbackContext);
                  break;

              case "setListener":
                  this.setListener(args, callbackContext);
                  break;

              case "createClient":
                  this.createClient(args, callbackContext);
                  break;

              case "login":
                  this.login(args, callbackContext);
                  break;

              case "logout":
                  this.logout(args, callbackContext);
                  break;

              case "getLastInfo":
                  this.getLastInfo(args, callbackContext);
                  break;

              case "getInfo":
                  this.getInfo(args, callbackContext);
                  break;

              case "getDefDrive":
                  this.getDefDrive(args, callbackContext);
                  break;

              case "rootDir":
                  this.rootDirectory(args, callbackContext);
                  break;

              case "createDir":
                  this.createDirectory(args, callbackContext);
                  break;

              case "getDir":
                  this.getDirectory(args, callbackContext);
                  break;

              case "createFile":
                  this.createFile(args, callbackContext);
                  break;

              case "getFile":
                  this.getFile(args, callbackContext);
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

              default:
                  return false;
              }
          } catch (JSONException e) {
              //e.printStack();
          }
          return true;
      }

      private void getVersion(JSONArray args, CallbackContext callbackContext) throws JSONException {
          String version = "ElastosHiveSDK-v0.1";
          callbackContext.success(version);
      }

      private void setListener(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer type = args.getInt(0);

          switch(type) {
          case LOGIN:
              loginCallbackCtxt = callbackContext;
              break;

          case RESULT:
              resultCallbackCtxt = callbackContext;
              break;
          }

          PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
          result.setKeepCallback(true);
          callbackContext.sendPluginResult(result);
      }

      private void createClient(JSONArray args, CallbackContext callbackContext) throws JSONException {
          String dataDir = args.getString(0);
          String options = args.getString(1);

          java.io.File dirFile = new java.io.File(cordova.getActivity().getFilesDir() + "/data/hive" + dataDir);
          if (!dirFile.exists())
               dirFile.mkdirs();

          ObjectMap map = ObjectMap.acquire(1);
          Client client = ClientBuilder.create(dataDir, options);
          Integer objId = System.identityHashCode(client);
          ObjectMap.toClientMap(map).put(objId, client);

          JSONObject ret= new JSONObject();
          ret.put("id", objId);
          callbackContext.success(ret);
      }

      private void login(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (!ObjectMap.isClientMap(map))
              return;

          JSONObject ret = new JSONObject();
          new Thread(() -> {
              try {
                  ObjectMap.toClientMap(map).get(objId).login(
                      new LoginHandler(handlerId, loginCallbackCtxt)
                  );

                  ret.put("result", "success");
              } catch(JSONException e) {
                  callbackContext.error("error");
              } catch(HiveException e) {
                  callbackContext.error("error");
              }
          }).start();
          callbackContext.success(ret);
      }

      private void logout(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (!ObjectMap.isClientMap(map))
              return;

          new Thread(() -> {
              try {
                  ObjectMap.toClientMap(map).get(objId).logout();

                  JSONObject ret = new JSONObject();
                  ret.put("result", "success");
                  callbackContext.success(ret);
              } catch (JSONException e) {
                  callbackContext.error("error");
              } catch (HiveException e) {
                  callbackContext.error("error");
              }
          }).start();
      }

      private void getLastInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isClientMap(map)) {
              JSONObjectHolder<Client.Info> holder;
              Client.Info info;

              info = ObjectMap.toClientMap(map).get(objId).getLastInfo();
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

              info = ObjectMap.toDriveMap(map).get(objId).getLastInfo();
              holder = new JSONObjectHolder<Drive.Info>(info);
              holder.put(Drive.Info.driveId);

              callbackContext.success(holder.get());
              return;
          }

          if (ObjectMap.isDirMap(map)) {
              JSONObjectHolder<Directory.Info> holder;
              Directory.Info info;

              info = ObjectMap.toDirMap(map).get(objId).getLastInfo();
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

              info = ObjectMap.toFileMap(map).get(objId).getLastInfo();
              holder = new JSONObjectHolder<File.Info>(info);
              holder.put(File.Info.itemId)
                    .put(File.Info.name)
                    .put(File.Info.size);

              callbackContext.success(holder.get());
              return;
          }
      }

      private void getInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isClientMap(map)) {
              ObjectMap.toClientMap(map).get(objId).getInfo(
                  new ResultHandler<Client.Info>(handlerId, resultCallbackCtxt)
              );
              return;
          }

          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).getInfo(
                  new ResultHandler<Drive.Info>(handlerId, resultCallbackCtxt)
              );
              return;
          }

          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).getInfo(
                  new ResultHandler<Directory.Info>(handlerId, resultCallbackCtxt)
              );
              return;
          }

          if (ObjectMap.isFileMap(map)) {
              ObjectMap.toFileMap(map).get(objId).getInfo(
                  new ResultHandler<File.Info>(handlerId, resultCallbackCtxt)
              );
          }
      }

      private void getDefDrive(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isClientMap(map)) {
              ObjectMap.toClientMap(map).get(objId).getDefaultDrive(
                  new ResultHandler<Drive>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void rootDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).getRootDir(
                  new ResultHandler<Directory>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void createDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).createDirectory(path,
                  new ResultHandler<Directory>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).createDirectory(path,
                  new ResultHandler<Directory>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void getDirectory(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).getDirectory(path,
                  new ResultHandler<Directory>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).getDirectory(path,
                  new ResultHandler<Directory>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void createFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).createFile(path,
                  new ResultHandler<File>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).createFile(path,
                  new ResultHandler<File>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void getFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).getFile(path,
                  new ResultHandler<File>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).getFile(path,
                  new ResultHandler<File>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void getItemInfo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDriveMap(map)) {
              ObjectMap.toDriveMap(map).get(objId).getItemInfo(path,
                  new ResultHandler<ItemInfo>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void getChildren(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).getChildren(
                  new ResultHandler<Children>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void moveTo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).moveTo(path,
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isFileMap(map)) {
              ObjectMap.toFileMap(map).get(objId).moveTo(path,
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void copyTo(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);
          String  path  = args.getString(3);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).copyTo(path,
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isFileMap(map)) {
              ObjectMap.toFileMap(map).get(objId).copyTo(path,
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }

      private void deleteItem(JSONArray args, CallbackContext callbackContext) throws JSONException {
          Integer mapId = args.getInt(0);
          Integer objId = args.getInt(1);
          int handlerId = args.getInt(2);

          ObjectMap map = ObjectMap.acquire(mapId);
          if (ObjectMap.isDirMap(map)) {
              ObjectMap.toDirMap(map).get(objId).deleteItem(
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
          if (ObjectMap.isFileMap(map)) {
              ObjectMap.toFileMap(map).get(objId).deleteItem(
                  new ResultHandler<Void>(handlerId, resultCallbackCtxt)
              );
              return;
          }
      }
  }
