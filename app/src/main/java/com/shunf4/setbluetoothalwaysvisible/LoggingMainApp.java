package com.shunf4.setbluetoothalwaysvisible;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggingMainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        File logFile = new File(getExternalFilesDir("logs"),
                "logcat_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".log"
        );

        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerReceiver(new BluetoothBroadcastReceiver(),
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        );
    }
}
