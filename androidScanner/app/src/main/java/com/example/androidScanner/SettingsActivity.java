package com.example.androidScanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    private Button saveButton;
    private TextView currentMqttServer;
    private TextView currentMqttTopic;
    private TextView currentObserverId;
    private EditText editMqttServer;
    private EditText editMqttTopic;
    private EditText editObserverId;

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String MQTT_SERVER = "tcp://iot.ubikampus.net";
    public static final String MQTT_TOPIC = "ohtu/test/observations";
    public static final String OBSERVER_ID = "5";

    private String newServerValue;
    private String newTopicValue;
    private String newObserverIdValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        currentMqttServer = findViewById(R.id.mqttServer);
        currentMqttTopic = findViewById(R.id.mqttTopic);
        currentObserverId = findViewById(R.id.observerId);
        editMqttServer = findViewById(R.id.newMqttServer);
        editMqttTopic = findViewById(R.id.newMqttTopic);
        editObserverId = findViewById(R.id.newObserverId);
        saveButton = findViewById(R.id.saveChanges);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        loadData();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!editMqttServer.getText().toString().isEmpty()) {
            editor.putString(MQTT_SERVER, editMqttServer.getText().toString());
            currentMqttServer.setText(editMqttServer.getText().toString());
        }
        if (!editMqttTopic.getText().toString().isEmpty()) {
            editor.putString(MQTT_TOPIC, editMqttTopic.getText().toString());
            currentMqttTopic.setText(editMqttTopic.getText().toString());
        }
        if (!editObserverId.getText().toString().isEmpty()) {
            editor.putString(OBSERVER_ID, editObserverId.getText().toString());
            currentObserverId.setText(editObserverId.getText().toString());
        }
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        newServerValue = sharedPreferences.getString(MQTT_SERVER,"tcp://iot.ubikampus.net");
        newTopicValue = sharedPreferences.getString(MQTT_TOPIC,"ohtu/test/observations");
        newObserverIdValue = sharedPreferences.getString(OBSERVER_ID,"5");
        currentMqttServer.setText(newServerValue);
        currentMqttTopic.setText(newTopicValue);
        currentObserverId.setText(newObserverIdValue);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}