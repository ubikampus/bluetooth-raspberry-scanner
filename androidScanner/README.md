### Beacon scanner for Android 6

#### Instructions

You can install [scanner](https://github.com/ubikampus/bluetooth-raspberry-scanner/blob/master/androidScanner/scanner.apk) on your tablet through ADB using command:  
```adb install -g scanner.apk```  

To configure the app use command:   
```adb shell am startservice -a android.intent.action.MAIN -d "scanner://change.scanner.settings?topic=beacons/observations\&observerId=16\&server=iot.ubikampus.net" com.androidScanner/com.example.androidScanner.RemoteConfigService``` 
  
The config variables are topic, observerId and server (without tcp://). Do not change configuration while app is running. Kill application first and only then use the configuration command.

Start scanning with command:  
```adb shell am startservice com.androidScanner/com.example.androidScanner.ScanningService```

To kill the app use command: ```adb shell am force-stop com.androidScanner```

