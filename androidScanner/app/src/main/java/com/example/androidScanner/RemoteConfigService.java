package com.example.androidScanner;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class RemoteConfigService extends Service {

    final static String TERMINAL_INPUT_MQTT_TOPIC = "topic";
    final static String TERMINAL_INPUT_OBSERVER_ID = "observerId";
    final static String TERMINAL_INPUT_SERVER = "server";

    public static final String LOG_TAG = "RemoteConfigActivity";

    @Override
    public int onStartCommand(Intent intent, int flags, int stardId) {
        String input = intent.getStringExtra("InputExtra");

        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                System.err.println("Data received");
                handleCommandLineSettings(data);
            }
        }
        return START_STICKY;
    }

    private void handleCommandLineSettings(Uri data) {
        Set<String> parameters = data.getQueryParameterNames();
        if (parameters == null || parameters.isEmpty()) {
            Log.d(LOG_TAG, "Parameters are null");
            return;
        }
        String newServer = data.getQueryParameter(TERMINAL_INPUT_SERVER);
        String newTopic = data.getQueryParameter(TERMINAL_INPUT_MQTT_TOPIC);
        String newObserverId = data.getQueryParameter(TERMINAL_INPUT_OBSERVER_ID);
        if (newTopic != null) {
            setTopic(newTopic);
        }
        if (newObserverId != null) {
            setObserverId(newObserverId);

        }
        if (newServer != null) {
            setServer(newServer);
        }
    }

    private void setTopic(String address) {
        PreferenceManager.getInstance(this).setTopic(address);
    }

    private void setObserverId(String observerId) {
        PreferenceManager.getInstance(this).setObserverId(observerId);
    }
    private void setServer(String server) {
        PreferenceManager.getInstance(this).setServer(server);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
