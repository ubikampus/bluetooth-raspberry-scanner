package com.example.androidScanner;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    private static final String CHANNEL_ID = "exampleChannel1";
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onCreate() {
        super.onCreate();
//        createNotificationChannel();
    }

    /*public void createNotificationChannel() {         // notificationChannels are used in Android 8+
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "example", NotificationManager.IMPORTANCE_LOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceChannel = new NotificationChannel(CHANNEL_ID, "example", NotificationManager.IMPORTANCE_LOW);
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }*/
}
