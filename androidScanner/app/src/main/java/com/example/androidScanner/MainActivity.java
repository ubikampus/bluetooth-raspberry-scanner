package com.example.androidScanner;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView results;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    MqttClient client;
    private Boolean scanningIsActive;

    final static String PREFERENCES_IDENTIFIER = "Preferences";

    final static String PREFERENCES_MQTT_TOPIC = "";
    final static String PREFERENCES_SERVER = "";
    final static String PREFERENCES_OBSERVER_ID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        results = findViewById(R.id.results);

        Button startScanButton = findViewById(R.id.startScanButton);
        startScanButton.performClick();
        scanningIsActive = true;

        moveTaskToBack(true);
    }

    private ScanCallback leScanCallback = new ScanCallback() {      //callback called upon received scan result
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                List<ADStructure> structures = ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                for (int i = 0; i < structures.size(); i++) {       //make sure that signal comes from iBeacon or EddystoneUID
                    ADStructure str = structures.get(i);
                    if (str instanceof EddystoneUID) {
                        EddystoneUID eUID = (EddystoneUID)str;

                        SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
                        String topic = preferences.getString(PREFERENCES_MQTT_TOPIC,"ohtu/test/observations");
                        int observerId = Integer.parseInt(preferences.getString(PREFERENCES_OBSERVER_ID,"0"));

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("addr", eUID.getNamespaceIdAsString());
                        jsonObject.put("observerId", observerId);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());

                        client.publish(topic, jsonMessage);
                        results.append("EddystoneUID was found and sent to mqtt topic "+ topic.toString() + "\n" +"Namespace ID (beaconId): "
                                + eUID.getNamespaceIdAsString() + ", observerId: " + observerId + ", rssi: " + result.getRssi() + "\n" + "\n");
                    }
                    if (str instanceof IBeacon) {
                        IBeacon iBeacon = (IBeacon)str;

                        SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
                        String topic = preferences.getString(PREFERENCES_MQTT_TOPIC,"ohtu/test/observations");
                        int observerId = Integer.parseInt(preferences.getString(PREFERENCES_OBSERVER_ID,"0"));

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("beaconId", iBeacon.getUUID());
                        jsonObject.put("observerId", observerId);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());

                        client.publish(topic, jsonMessage);
                        results.append("iBeacon was found and sent to mqtt topic "+ topic + "\n" +"UUID (beaconId): "
                                + iBeacon.getUUID() + ", observerId: " + observerId + ", rssi: " + result.getRssi() + "\n" + "\n");

                    }
                }
                final int scrollAmount = results.getLayout().getLineTop(results.getLineCount()) - results.getHeight(); //auto scroll
                if (scrollAmount > 0)
                    results.scrollTo(0, scrollAmount);
            } catch (Exception ex) {
                Context context = getApplicationContext();
                CharSequence text = "Connection to mqtt succeeded, but error emerged while sending data to mqtt. Error: " + ex;
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, 15);
                toast.show();
            }
        }
    };

    public void startScanning(View v) {
        startForegroundService(v);
        results.setText("");
        try {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
            String server = preferences.getString(PREFERENCES_SERVER,"tcp://iot.ubikampus.net");
            String uri = "tcp://" + server;
            client = new MqttClient(uri, MqttClient.generateClientId(), new MemoryPersistence());
//            client = new MqttClient("tcp://iot.ubikampus.net", MqttClient.generateClientId(), new MemoryPersistence());
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
                scanningIsActive = true;
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

    public void stopScanning(View v) {
        results.append("Scanning stopped");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(leScanCallback);
            }
        });
        stopForegroundService(v);
        scanningIsActive = false;
    }

    public void startForegroundService(View v) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Beacon scanner is running");
        startService(serviceIntent);

    }

    public void stopForegroundService(View v) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    public void openSettings(View v) {
        if (scanningIsActive) {
            Toast toast = Toast.makeText(getApplicationContext(), "Settings cannot be changed while scanning. Please turn scan off", 15);
            toast.show();
        } else {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("coarse location permission granted");
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                builder.show();
            }
        }
    }
}
