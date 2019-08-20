### Beacon scanner for Android 6

Installation for Android tablet

* Download project
* Change server adress, topic and abserverId if needed
* Build apk file (for instance: in Android Studio build button -> app/build/outputs/apk/debug/file.apk)
* Connect PC and tablet 
* Install .apk file on tablet:   ```adb install file.apk``` 
* Start app:  ```adb shell am start -a android.intent.action.MAIN com.androidScanner/com.example.androidScanner.MainActivity``` 
* Kill app:  ```adb shell am force-stop com.androidScanner```  
