package com.example.androidScanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import java.util.Set;

public class RemoteConfigActivity extends AppCompatActivity {

    final static String TERMINAL_INPUT_MQTT_TOPIC = "topic";
    final static String TERMINAL_INPUT_OBSERVER_ID = "observerId";

    public static final String LOG_TAG = "RemoteConfigActivity";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_config);
        intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                System.err.println("Intent received");
                handleCommandLineSettings(data);
            }
        } else {
            finish();
        }
    }

    private void handleCommandLineSettings(Uri data) {
        Set<String> parameters = data.getQueryParameterNames();
        if (parameters == null || parameters.isEmpty()) {
            Log.d(LOG_TAG, "Parameters are null");
            return;
        }
        String newTopic = data.getQueryParameter(TERMINAL_INPUT_MQTT_TOPIC);
        String newObserverId = data.getQueryParameter(TERMINAL_INPUT_OBSERVER_ID);
        if (newTopic != null) {
            setTopic(newTopic);
        }
        if (newObserverId != null) {
            setObserverId(newObserverId);
        }
        finish();
    }

    private void setTopic(String address) {
        PreferenceManager.getInstance(this).setTopic(address);
    }

    private void setObserverId(String observerId) {
        PreferenceManager.getInstance(this).setObserverId(observerId);
    }


}
