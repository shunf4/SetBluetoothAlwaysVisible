package com.shunf4.setbluetoothalwaysvisible;

import android.app.Application;

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
    }
}
