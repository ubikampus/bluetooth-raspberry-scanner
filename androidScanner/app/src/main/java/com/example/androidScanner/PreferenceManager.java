package com.example.androidScanner;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static PreferenceManager sharedInstance = null;
    final SharedPreferences preferences;
    final static String PREFERENCES_IDENTIFIER = "Preferences";
    final static String PREFERENCES_MQTT_TOPIC = "";
    final static String PREFERENCES_OBSERVER_ID = "0";
    final static String PREFERENCES_SERVER = "iot";

    private PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_IDENTIFIER, Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new PreferenceManager(context);
        }
        return sharedInstance;
    }

    public void setServer(String server) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_SERVER, server);
        editor.apply();
    }

    public void setTopic(String topic) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_MQTT_TOPIC, topic);
        editor.apply();
    }

    public String getTopic() {
        return preferences.getString(PREFERENCES_MQTT_TOPIC, "ips/observations");
    }

    public void setObserverId(String ObserverId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_OBSERVER_ID, ObserverId);
        editor.apply();
    }

    public String getObserverId() {
        return preferences.getString(PREFERENCES_OBSERVER_ID, "0");
    }

}
