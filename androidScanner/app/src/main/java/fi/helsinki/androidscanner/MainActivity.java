package fi.helsinki.androidscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
