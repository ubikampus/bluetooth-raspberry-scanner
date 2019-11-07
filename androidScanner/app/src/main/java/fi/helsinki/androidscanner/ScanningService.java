package fi.helsinki.androidscanner;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanningService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;

    final static String PREFERENCES_IDENTIFIER = "Preferences";
    final static String PREFERENCES_MQTT_TOPIC = "";
    final static String PREFERENCES_SERVER = "iot";
    final static String PREFERENCES_OBSERVER_ID = "0";

    private static final String TAG = "ScanningService";
    private static final ParcelUuid EYEBUD_SERVICE_UUID = new ParcelUuid(UUID.fromString("c796f340-0000-1000-8000-00805f9b34fb"));

    MqttClient client;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int stardId) {

        Log.d(TAG, "ScanningService was started");

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        startScanning();

        return START_STICKY;
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            List<ADStructure> structures = ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
            List<ParcelUuid> serviceUUIDs = result.getScanRecord().getServiceUuids();

            if (serviceUUIDs != null && serviceUUIDs.contains(EYEBUD_SERVICE_UUID)) {
                sendMqttMessage(result.getDevice().getName(), result.getRssi());
            }

            for (int i = 0; i < structures.size(); i++) {
                ADStructure str = structures.get(i);
                String beaconId = null;

                if (str instanceof EddystoneUID) {
                    EddystoneUID eUID = (EddystoneUID) str;
                    beaconId = eUID.getNamespaceIdAsString();
                } else if (str instanceof IBeacon) {
                    IBeacon iBeacon = (IBeacon) str;

                    if (!iBeacon.getUUID().equals("50765cb7-d9ea-4e21-99a4-fa879613a492") && !iBeacon.getUUID().equals("00177756-d59f-072b-2814-142f9b041005")) { // those are static iBeacons at Ubikampus (no need to track them)
                        beaconId = iBeacon.getUUID().toString();
                    }
                }

                if (beaconId != null) {
                    sendMqttMessage(beaconId, result.getRssi());
                }
            }

        }
    };

    private void sendMqttMessage(String id, int rssi) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
        String topic = preferences.getString(PREFERENCES_MQTT_TOPIC, "beacons/observations");
        String observerId = preferences.getString(PREFERENCES_OBSERVER_ID, "default");

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("beaconId", id);
            jsonObject.put("observerId", observerId);
            jsonObject.put("rssi", rssi);
        } catch (JSONException e) {
            Log.wtf(TAG, "Unexpected JSON problem: " + e.getMessage());
            return;
        }

        MqttMessage jsonMessage = new MqttMessage(jsonObject.toString().getBytes());

        try {
            client.publish(topic, jsonMessage);
        } catch (MqttException e) {
            Log.e(TAG, "MQTT publish failed: " + e.getMessage());
        }
    }

    public void startScanning() {
        try {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
            String ip = preferences.getString(PREFERENCES_SERVER, "192.168.1.4");
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

                byte[] iBeaconFilterData = {0x4c, 0x00, 0x02};
                ScanFilter filter1 = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString("c796f340-0000-1000-8000-00805f9b34fb"))).build();
                ScanFilter filter2 = new ScanFilter.Builder().setManufacturerData(0x4c00, iBeaconFilterData).build();
                ArrayList<ScanFilter> filters = new ArrayList<>();

                mBluetoothScanner.startScan(filters, scanSettings, leScanCallback);
            } else {
                String text = "Connection to mqtt failed. Scanning was not started. Make sure that you are in UbiKampus network.";
                Log.e(TAG, text);
            }
        } catch (Exception ex) {
            String text = "Connection to mqtt failed. Error: " + ex + ". " +
                    "Scanning was not started. Make sure that you are in UbiKampus network.";
            Log.e(TAG, text);
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
