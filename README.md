---
title: hive
description: The Hive javascript API.
---

# elastos-trinity-plugins-hive

This plugin defines a global `cordova.hivePlugin` object, which provides an API for hive library.

Although in the global scope, it is not available until after the `deviceready` event.

```js
document.addEventListener("deviceready", onDeviceReady, false);
function onDeviceReady() {
    console.log(hivePlugin);
}
```
---

## Installation

```bash
    cordova plugin add elastos-trinity-plugins-hive
```

## Supported Platforms

- Android
- iOS

# API Reference <a name="reference"></a>

## Classes

<dl>
<dt><a href="#File">File</a></dt>
<dd></dd>
<dt><a href="#Directory">Directory</a></dt>
<dd></dd>
<dt><a href="#Drive">Drive</a></dt>
<dd></dd>
<dt><a href="#Client">Client</a></dt>
<dd></dd>
</dl>

<a name="File"></a>

## File
**Kind**: global class  

* [File](#File)
    * [new File()](#new_File_new)
    * [.getLastInfo(onSuccess, onError)](#File+getLastInfo) ⇒
    * [.getInfo()](#File+getInfo) ⇒
    * [.moveTo(destPath)](#File+moveTo) ⇒
    * [.copyTo(newPath)](#File+copyTo) ⇒
    * [.deleteItem()](#File+deleteItem) ⇒
    * [.readData(length)](#File+readData) ⇒
    * [.writeData(data)](#File+writeData) ⇒
    * [.commit()](#File+commit) ⇒
    * [.discard(onSuccess)](#File+discard) ⇒

<a name="new_File_new"></a>

### new File()
The class representing File.

<a name="File+getLastInfo"></a>

### file.getLastInfo(onSuccess, onError) ⇒
Get the information(ID, name size, type) of the file got last time.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |

<a name="File+getInfo"></a>

### file.getInfo() ⇒
Get the information(ID, name, size, type) of the file from the server.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains the information(ID, name, size, type) of the file
will be returned on success, otherwise a promise object that contains error
information will be returned.  
<a name="File+moveTo"></a>

### file.moveTo(destPath) ⇒
Move to a new path.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| destPath | <code>string</code> | The new path. |

<a name="File+copyTo"></a>

### file.copyTo(newPath) ⇒
Copy to a new path.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| newPath | <code>string</code> | The new path. |

<a name="File+deleteItem"></a>

### file.deleteItem() ⇒
Delete.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  
<a name="File+readData"></a>

### file.readData(length) ⇒
Read data of a specified length sequentially.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| length | <code>number</code> | The length of data to write. |

<a name="File+writeData"></a>

### file.writeData(data) ⇒
Write local change on File.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| data | <code>string</code> | The data to write. |

<a name="File+commit"></a>

### file.commit() ⇒
Commit local change on File to backend.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  
<a name="File+discard"></a>

### file.discard(onSuccess) ⇒
Discard local change on File.

**Kind**: instance method of [<code>File</code>](#File)  
**Returns**: onSuccess will be called on success.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |

<a name="Directory"></a>

## Directory
**Kind**: global class  

* [Directory](#Directory)
    * [new Directory()](#new_Directory_new)
    * [.getLastInfo(onSuccess, onError)](#Directory+getLastInfo) ⇒
    * [.getInfo()](#Directory+getInfo) ⇒
    * [.createDirectory(name)](#Directory+createDirectory) ⇒
    * [.getDirectory(name)](#Directory+getDirectory) ⇒
    * [.createFile(name)](#Directory+createFile) ⇒
    * [.getFile(name)](#Directory+getFile) ⇒
    * [.getChildren()](#Directory+getChildren) ⇒
    * [.moveTo(destPath)](#Directory+moveTo) ⇒
    * [.copyTo(newPath)](#Directory+copyTo) ⇒
    * [.deleteItem()](#Directory+deleteItem) ⇒

<a name="new_Directory_new"></a>

### new Directory()
The class representing Directory.

<a name="Directory+getLastInfo"></a>

### directory.getLastInfo(onSuccess, onError) ⇒
Get the information(ID, name, childCount) of the directory got last time.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |

<a name="Directory+getInfo"></a>

### directory.getInfo() ⇒
Get the information(ID, name, childCount) of the directory from the server.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A promise object that contains the information(ID, name, childCount) of the file
will be returned on success, otherwise a promise object that contains error
information will be returned.  
<a name="Directory+createDirectory"></a>

### directory.createDirectory(name) ⇒
Create directory with name.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A directory will be returned on success, otherwise a promise object that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| name | <code>string</code> | The directory name. |

<a name="Directory+getDirectory"></a>

### directory.getDirectory(name) ⇒
Get the directory with a specified name.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A directory will be returned on success, otherwise a promise object that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| name | <code>string</code> | The directory name. |

<a name="Directory+createFile"></a>

### directory.createFile(name) ⇒
Create file with name.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A file will be returned on success, otherwise a promise object that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| name | <code>string</code> | The file name. |

<a name="Directory+getFile"></a>

### directory.getFile(name) ⇒
Get the File with a specified name.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A file will be returned on success, otherwise a promise object that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| name | <code>string</code> | The file name. |

<a name="Directory+getChildren"></a>

### directory.getChildren() ⇒
Get children for current directory.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: The children for current directory will be returned on success, otherwise
a promise object that contains error information will be returned.  
<a name="Directory+moveTo"></a>

### directory.moveTo(destPath) ⇒
Move to a new path.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| destPath | <code>string</code> | The destination path. |

<a name="Directory+copyTo"></a>

### directory.copyTo(newPath) ⇒
Copy to a new path.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| newPath | <code>string</code> | The new path. |

<a name="Directory+deleteItem"></a>

### directory.deleteItem() ⇒
Delete.

**Kind**: instance method of [<code>Directory</code>](#Directory)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  
<a name="Drive"></a>

## Drive
**Kind**: global class  

* [Drive](#Drive)
    * [new Drive()](#new_Drive_new)
    * [.getLastInfo(onSuccess, onError)](#Drive+getLastInfo) ⇒
    * [.getInfo()](#Drive+getInfo) ⇒
    * [.rootDirctory()](#Drive+rootDirctory) ⇒
    * [.createDirectory(path)](#Drive+createDirectory) ⇒
    * [.getDirectory(path)](#Drive+getDirectory) ⇒
    * [.createFile(path)](#Drive+createFile) ⇒
    * [.getFile(path)](#Drive+getFile) ⇒
    * [.getItemInfo(path)](#Drive+getItemInfo) ⇒

<a name="new_Drive_new"></a>

### new Drive()
The class representing Drive.

<a name="Drive+getLastInfo"></a>

### drive.getLastInfo(onSuccess, onError) ⇒
Get the information(ID) of the drive got last time.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |

<a name="Drive+getInfo"></a>

### drive.getInfo() ⇒
Get the information(ID) of the drive from the server.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A promise object that contains the information(ID) of the file
will be returned on success, otherwise a promise that contains error information
will be returned.  
<a name="Drive+rootDirctory"></a>

### drive.rootDirctory() ⇒
Get the root directory.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A directory will be returned on success, otherwise a promise that contains
error information will be returned.  
<a name="Drive+createDirectory"></a>

### drive.createDirectory(path) ⇒
Create directory with path.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A directory will be returned on success, otherwise a promise that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| path | <code>string</code> | The directory path. |

<a name="Drive+getDirectory"></a>

### drive.getDirectory(path) ⇒
Get the directory with a specified path.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A directory will be returned on success, otherwise a promise that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| path | <code>string</code> | The directory path. |

<a name="Drive+createFile"></a>

### drive.createFile(path) ⇒
Create file with path.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A file will be returned on success, otherwise a promise that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| path | <code>string</code> | The file path. |

<a name="Drive+getFile"></a>

### drive.getFile(path) ⇒
Get the File with a specified path.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A file will be returned on success, otherwise a promise that contains
error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| path | <code>string</code> | The file path. |

<a name="Drive+getItemInfo"></a>

### drive.getItemInfo(path) ⇒
Get the information(ID, name, size, type) of the drive with a specified path.

**Kind**: instance method of [<code>Drive</code>](#Drive)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise that contains error information will be returned.  

| Param | Type | Description |
| --- | --- | --- |
| path | <code>string</code> | The drive path. |

<a name="Client"></a>

## Client
**Kind**: global class  

* [Client](#Client)
    * [new Client()](#new_Client_new)
    * [.login(onSuccess, onError, handler)](#Client+login) ⇒
    * [.logout(onSuccess, onError)](#Client+logout) ⇒
    * [.getLastInfo(onSuccess, onError)](#Client+getLastInfo) ⇒
    * [.getInfo()](#Client+getInfo) ⇒
    * [.getDefDrive()](#Client+getDefDrive) ⇒

<a name="new_Client_new"></a>

### new Client()
The class representing Client.

<a name="Client+login"></a>

### client.login(onSuccess, onError, handler) ⇒
Associate a user with the Client.

**Kind**: instance method of [<code>Client</code>](#Client)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |
| handler | <code>function</code> | The function to call. |

<a name="Client+logout"></a>

### client.logout(onSuccess, onError) ⇒
Dissociate the user from the Client.

**Kind**: instance method of [<code>Client</code>](#Client)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |

<a name="Client+getLastInfo"></a>

### client.getLastInfo(onSuccess, onError) ⇒
Get the last associated user's information with client information.

**Kind**: instance method of [<code>Client</code>](#Client)  
**Returns**: onSuccess will be called on success, otherwise onError will be called.  

| Param | Type | Description |
| --- | --- | --- |
| onSuccess | <code>function</code> | The function to call on success. |
| onError | <code>function</code> | The function to call on error. |

<a name="Client+getInfo"></a>

### client.getInfo() ⇒
Get associated user's information with client information.

**Kind**: instance method of [<code>Client</code>](#Client)  
**Returns**: A promise object that contains success information will be returned on success,
otherwise a promise object that contains error information will be returned.  
<a name="Client+getDefDrive"></a>

### client.getDefDrive() ⇒
Get the current backend's Drive instance associated with the client's drive.

**Kind**: instance method of [<code>Client</code>](#Client)  
**Returns**: A drive will be returned on success, otherwise a promise object that contains
error information will be returned.  

