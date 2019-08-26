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
    private TextView currentMqttTopic;
    private TextView currentObserverId;
    private EditText editMqttTopic;
    private EditText editObserverId;

    final static String PREFERENCES_IDENTIFIER = "Preferences";
    final static String PREFERENCES_MQTT_TOPIC = "";
    final static String PREFERENCES_OBSERVER_ID = "0";

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

        currentMqttTopic = findViewById(R.id.mqttTopic);
        currentObserverId = findViewById(R.id.observerId);
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
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!editMqttTopic.getText().toString().isEmpty()) {
            editor.putString(PREFERENCES_MQTT_TOPIC, editMqttTopic.getText().toString());
            currentMqttTopic.setText(editMqttTopic.getText().toString());
        }
        if (!editObserverId.getText().toString().isEmpty()) {
            editor.putString(PREFERENCES_OBSERVER_ID, editObserverId.getText().toString());
            currentObserverId.setText(editObserverId.getText().toString());
        }
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_IDENTIFIER, MODE_PRIVATE);
        newTopicValue = sharedPreferences.getString(PREFERENCES_MQTT_TOPIC,"ohtu/test/observations");
        newObserverIdValue = sharedPreferences.getString(PREFERENCES_OBSERVER_ID,"0");

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