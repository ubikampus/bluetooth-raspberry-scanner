package com.example.androidScanner;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.List;

public class ScanningService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;

    final static String PREFERENCES_IDENTIFIER = "Preferences";
    final static String PREFERENCES_MQTT_TOPIC = "";
    final static String PREFERENCES_SERVER = "iot";
    final static String PREFERENCES_OBSERVER_ID = "0";

    private static final String TAG = "ScanningService";

    MqttClient client;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int stardId) {

        Log.d(TAG,"ScanningService was started");

        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        startScanning();

        return START_REDELIVER_INTENT;
    }

    private ScanCallback leScanCallback = new ScanCallback() {      //callback called upon received scan result
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                List<ADStructure> structures = ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                for (int i = 0; i < structures.size(); i++) {
                    ADStructure str = structures.get(i);
                    if (str instanceof EddystoneUID) {
                        EddystoneUID eUID = (EddystoneUID)str;

                        SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
                        String topic = preferences.getString(PREFERENCES_MQTT_TOPIC,"beacons/observations");
                        String observerId = preferences.getString(PREFERENCES_OBSERVER_ID,"default");

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("beaconId", eUID.getNamespaceIdAsString());
                        jsonObject.put("observerId", observerId);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());

                        client.publish(topic, jsonMessage);

                    } else if (str instanceof IBeacon) {
                        IBeacon iBeacon = (IBeacon)str;

                        SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
                        String topic = preferences.getString(PREFERENCES_MQTT_TOPIC,"beacons/observations");
                        String observerId = preferences.getString(PREFERENCES_OBSERVER_ID,"default");

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("beaconId", iBeacon.getUUID());
                        jsonObject.put("observerId", observerId);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());

                        client.publish(topic, jsonMessage);
                    }
                }
            } catch (Exception ex) {
                Context context = getApplicationContext();
                CharSequence text = "Connection to mqtt succeeded, but error emerged while sending data to mqtt. Error: " + ex;
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, 15);
                toast.show();
            }
        }
    };

    public void startScanning() {
        try {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
            String ip = preferences.getString(PREFERENCES_SERVER,"192.168.1.4");
            String server = "tcp://" + ip;
            client = new MqttClient(server, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            if (client.isConnected()) {
                final ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)            // setup continuous scan
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                        .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
                        .setReportDelay(0L)
                        .build();

                mBluetoothScanner.startScan(null, scanSettings, leScanCallback);
            } else {
                Context context = getApplicationContext();
                CharSequence text = "Connection to mqtt failed. Scanning was not started. Make sure that you are in UbiKampus network.";
                int duration = Toast.LENGTH_LONG;
                Toast mToast = Toast.makeText(context, text, 15);
                mToast.show();
            }
        } catch (Exception ex) {
            Context context = getApplicationContext();
            CharSequence text = "Connection to mqtt failed. Error: " + ex +". " +
                    "Scanning was not started. Make sure that you are in UbiKampus network.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, 15);
            toast.show();
        }
    }

    public void stopScanning() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(leScanCallback);
            }
        });
    }
}
