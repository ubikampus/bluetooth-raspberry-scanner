### Beacon scanner for Android 6

#### Instructions

You can install [scanner](https://github.com/ubikampus/bluetooth-raspberry-scanner/blob/feature/androidScanner/androidScanner/scanner.apk) on your tablet through ADB using command:  
```adb install scanner.apk```  

Grant location permission to the scanner:   
```adb shell pm grant com.androidScanner android.permission.ACCESS_COARSE_LOCATION```


To configure the app use command:   
```adb shell am start -a android.intent.action.VIEW -d "scanner://change.scanner.settings?topic=beacons/observations\&observerId=66" com.androidScanner/com.example.androidScanner.RemoteConfigActivity``` 
  
The config variables are topic and observerId. Do not change configuration while app is running. Kill application first and only then use the configuration command. You can also configure the app from settings menu.

Start the app with command:  
```adb shell am start -a android.intent.action.MAIN com.androidScanner/com.example.androidScanner.MainActivity```

To kill the app use command: ```adb shell am force-stop com.androidScanner```

