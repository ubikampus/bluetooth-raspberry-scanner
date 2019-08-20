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

        Button startFGServiceButton = findViewById(R.id.startServiceButton);
        startFGServiceButton.performClick();
        Button startScanButton = findViewById(R.id.startScanButton);
        startScanButton.performClick();

        moveTaskToBack(true);   // hide app screen
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
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("addr", eUID.getNamespaceIdAsString());
                        jsonObject.put("observerId", 5);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());
                        client.publish("ohtu/test/observations", jsonMessage);
                        results.append("EddystoneUID was found and sent to mqtt (ohtu/test/observations)" + "\n" +"Namespace ID (beaconId): " + eUID.getNamespaceIdAsString() +
                                ", observerId: " + 5 + ", rssi: " + result.getRssi() + "\n" + "\n");
                    }
                    if (str instanceof IBeacon) {
                        IBeacon iBeacon = (IBeacon)str;
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("beaconId", iBeacon.getUUID());
                        jsonObject.put("observerId", 5);
                        jsonObject.put("rssi", result.getRssi());
                        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());
                        client.publish("ohtu/test/observations", jsonMessage);
                        results.append("iBeacon was found and sent to mqtt (ohtu/test/observations)" + "\n" +"UUID (beaconId): "
                                + iBeacon.getUUID() + ", observerId: " + 5 + ", rssi: " + result.getRssi() + "\n" + "\n");

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
        results.setText("");
        try {

            client = new MqttClient("tcp://iot.ubikampus.net", MqttClient.generateClientId(), new MemoryPersistence());
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



    public void stopScanning(View v) {
        results.append("Scanning stopped");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(leScanCallback);
            }
        });
    }

    public void startService(View v) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Beacon scanner is running");
        startService(serviceIntent);

    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
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
