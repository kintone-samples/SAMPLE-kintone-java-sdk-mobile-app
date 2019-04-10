## Overview

&emsp;&ensp;For kintone developers, provide an example of developing mobile application using kintone-java-sdk.  \
&emsp;&ensp;With kintone-java-sdk, you can develop Android-specific apps for kintone.\
&emsp;&ensp;Specifically, you can view records, add record, edit record, delete record, write record's comment, etc. of the kintone app.

## Requirement
        
* [kintone-java-sdk v0.2.0](https://github.com/kintone/kintone-java-sdk)
* Android SDK Version : min 15 ～ max 28 is needed
* Android Version : 7.0 or above

## Usage

1. Create a kintone App

* Create kintone App and Form with fileds as below.
  
    * Form Design
     ![overview image](./Screenshots/form_setting.PNG?raw=true)
    * Field Settings
    
        |Name|Field Code| 
        | :- | :- |
        | Summary| Summary| 
        | Notes| Notes| 
        | Photo| Photo| 
        | Status | Status| 
        | Creator | Creator| 
        | Create Date Time|CreateDateTime| 

2. Get source code

```bashshell
$ git clone https://github.com/kintone/SAMPLE-kintone-java-sdk-mobile-app.git
```

3. Open with Android Studio

* In Android Studio, select File > Open. Then choose cloned SAMPLE-kintone-java-sdk-mobile-app. \
![overview image](./Screenshots/open_source.PNG?raw=true)

4. Run The App

* In Android Studio, click run icon to start app in simulator. \
![overview image](./Screenshots/run_app.PNG?raw=true)

## Features
 * Using Password Authentication to connect with kintone
 * Be able to import client certificate file to authenticate
 * View all records.
 * Create new record.
 * View/Edit/Delete specific record.
 * Take photo and Upload images from mobile application to kintone.
 * Comment/Reply or Delete a comment belong to a record.
 
## Description

* About The App Pages 

    * Login Page \
     ![overview image](./Screenshots/login.PNG?raw=true)
    * Record List Page \
     ![overview image](./Screenshots/record_list.PNG?raw=true)
    * Record Detail Page \
     ![overview image](./Screenshots/record_detail.PNG?raw=true)
    * Record Comment Page \
     ![overview image](./Screenshots/record_comment.PNG?raw=true)
    * Edit Record Page \
     ![overview image](./Screenshots/record_edit.PNG?raw=true)
    * Add Record Page \
     ![overview image](./Screenshots/record_add.PNG?raw=true)

## Libraries used and their documentation

* kintone-java-sdk v0.2.0  [Docs](https://kintone.github.io/kintone-java-sdk)

## License

&emsp;&ensp;MIT

## Copyright

&emsp;&ensp;Copyright(c) Cybozu, Inc.

