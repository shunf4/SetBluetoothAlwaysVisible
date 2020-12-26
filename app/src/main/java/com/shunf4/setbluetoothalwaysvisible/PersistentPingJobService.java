package com.shunf4.setbluetoothalwaysvisible;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PersistentPingJobService extends JobService {

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        int[] macAddressIntArray = jobParameters.getExtras().getIntArray(MainActivity.MAC_ADDRESS_KEY);
        byte[] macAddress = MainActivity.intArrayToByteArray(macAddressIntArray);

        final BroadcastReceiver[] pingReceiver = new BroadcastReceiver[1];
        pingReceiver[0] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("SetBluetoothAlwaysVisible.PersistentPingJobService", "Ping result: " + MainActivity.getPingResultTextFromIntent(PersistentPingJobService.this, intent));
                try {
                    unregisterReceiver(pingReceiver[0]);
                } catch (IllegalArgumentException e) {
                }
                jobFinished(jobParameters, false);
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        try {
            unregisterReceiver(pingReceiver[0]);
        } catch (IllegalArgumentException e) {
        }
        registerReceiver(pingReceiver[0], intentFilter);

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bd =  ba.getRemoteDevice(macAddress);
        boolean success = bd.fetchUuidsWithSdp();

        Log.i("SetBluetoothAlwaysVisible.PersistentPingJobService", "Pinged " + MainActivity.macAddressToString(macAddress) + (success ? "" : " (Failed)"));
        scheduleRefresh(macAddressIntArray);

        if (!success) {
            try {
                unregisterReceiver(pingReceiver[0]);
            } catch (IllegalArgumentException e) {
            }
            jobFinished(jobParameters, false);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private void scheduleRefresh(int[] macAddressIntArray) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray(MainActivity.MAC_ADDRESS_KEY, macAddressIntArray);

        JobInfo.Builder builder = new JobInfo.Builder((int) System.currentTimeMillis(),
                new ComponentName(getPackageName(), PersistentPingJobService.class.getName())
        )
                .setExtras(bundle)
                .setMinimumLatency(10 * 1000);


        JobInfo jobInfo = builder.build();
        scheduler.schedule(jobInfo);

        Log.i("SetBluetoothAlwaysVisible.PersistentPingJobService", "Rescheduled job");
    }
}
