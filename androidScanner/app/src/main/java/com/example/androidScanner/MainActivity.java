package com.example.androidScanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, ScanningService.class);
        serviceIntent.putExtra("inputExtra", "Beacon scanner");
        startService(serviceIntent);

        finish();
    }
}
