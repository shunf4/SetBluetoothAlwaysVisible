package com.shunf4.setbluetoothalwaysvisible;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_DISCOVERABLE_BT = 10001;

    private final BroadcastReceiver pingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((TextView) findViewById(R.id.ping_result)).setText(getPingResultTextFromIntent(MainActivity.this, intent));
            ((Button) findViewById(R.id.start_ping)).setEnabled(true);
        }
    };

    public static String getPingResultTextFromIntent(Context context, Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

        String resultText;
        if (uuids == null) {
            resultText = context.getString(R.string.ping_result_no_uuid);
        } else {
            StringBuilder uuidsStringBuilder = new StringBuilder();
            for (Parcelable uuidRaw : uuids) {
                ParcelUuid uuid = (ParcelUuid) uuidRaw;
                uuidsStringBuilder.append(uuid.toString());
                uuidsStringBuilder.append(", ");
            }

            resultText = context.getString(R.string.ping_result_uuids, uuidsStringBuilder.toString());
        }

        return resultText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRefreshStatusClicked(null);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(pingReceiver);
        } catch (IllegalArgumentException e) {
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRefreshStatusClicked(null);
    }

    public void onRefreshStatusClicked(View view) {
        TextView statusText = findViewById(R.id.bt_status);

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        switch (ba.getState()) {
            case BluetoothAdapter.STATE_OFF:
                statusText.setText(R.string.state_off);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                statusText.setText(R.string.state_turning_on);
                break;
            case BluetoothAdapter.STATE_ON:
                switch (ba.getScanMode()) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        statusText.setText(R.string.scan_mode_none);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        statusText.setText(R.string.scan_mode_connectable);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        statusText.setText(R.string.scan_mode_connectable_discoverable);
                        break;
                }
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                statusText.setText(R.string.state_turning_off);
                break;
        }
    }

    public void onSetAlwaysVisibleClicked(View view) {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(ba, 36000);
            setScanMode.invoke(ba, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 36000);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        onRefreshStatusClicked(null);
    }

    public void onSetAlwaysVisibleNormalWayClicked(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivityForResult(intent, REQUEST_DISCOVERABLE_BT);
    }

    public void onSetAlwaysVisibleNormalWay300SecondsClicked(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(intent, REQUEST_DISCOVERABLE_BT);
    }

    public void onStartPingClicked(View view) {
        byte[] macAddress = parseMacAddressString(((TextView) findViewById(R.id.input_mac_address_to_ping)).getText());
        if (macAddress == null) {
            Toast.makeText(this, R.string.error_mac_address, Toast.LENGTH_LONG).show();
            return;
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        try {
            unregisterReceiver(pingReceiver);
        } catch (IllegalArgumentException e) {
        }
        registerReceiver(pingReceiver, intentFilter);

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bd =  ba.getRemoteDevice(macAddress);
        boolean success = bd.fetchUuidsWithSdp();

        if (success) {
            ((TextView) findViewById(R.id.ping_result)).setText(R.string.please_wait_for_ping_result);
            Toast.makeText(this, R.string.bluetooth_ping_start_success, Toast.LENGTH_SHORT).show();
            ((Button) findViewById(R.id.start_ping)).setEnabled(false);
        } else {
            ((TextView) findViewById(R.id.ping_result)).setText(R.string.bluetooth_ping_start_failure);
            Toast.makeText(this, R.string.bluetooth_ping_start_failure, Toast.LENGTH_SHORT).show();
        }
    }

    public static byte[] parseMacAddressString(CharSequence macAddressString) {
        String[] parts = macAddressString.toString().split(":");
        if (parts.length != 6) return null;
        if (!Arrays.stream(parts).allMatch(part -> {
            if (part.length() != 2) return false;
            char[] partChars = part.toCharArray();
            return (partChars[0] >= '0' && partChars[0] <= '9'
                    || partChars[0] >= 'a' && partChars[0] <= 'f'
                    || partChars[0] >= 'A' && partChars[0] <= 'F')
                && (partChars[1] >= '0' && partChars[1] <= '9'
                    || partChars[1] >= 'a' && partChars[1] <= 'f'
                    || partChars[1] >= 'A' && partChars[1] <= 'F');
        })) return null;

        byte[] result = new byte[6];
        for (int i = 0; i < 6; i++) {
            result[i] = Integer.valueOf(Integer.parseInt(parts[i], 16)).byteValue();
        }

        return result;
    }

    public static String macAddressToString(byte[] macAddress) {
        StringBuilder sb = new StringBuilder();
        for (byte part : macAddress) {
            sb.append(String.format("%02X", part));
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static final String MAC_ADDRESS_KEY = "MAC_ADDRESS";

    public static int[] byteArrayToIntArray(byte[] byteArray) {
        int[] result = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            result[i] = byteArray[i];
        }
        return result;
    }

    public static byte[] intArrayToByteArray(int[] intArray) {
        byte[] result = new byte[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            result[i] = (byte) intArray[i];
        }
        return result;
    }

    public void onStartPingJobClicked(View view) {
        byte[] macAddress = parseMacAddressString(((TextView) findViewById(R.id.input_mac_address_to_ping)).getText());
        if (macAddress == null) {
            Toast.makeText(this, R.string.error_mac_address, Toast.LENGTH_LONG).show();
            return;
        }

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray(MAC_ADDRESS_KEY, byteArrayToIntArray(macAddress));

        JobInfo.Builder builder = new JobInfo.Builder((int) System.currentTimeMillis(),
                new ComponentName(getPackageName(), PersistentPingJobService.class.getName())
        )
                .setExtras(bundle)
                .setMinimumLatency(10 * 1000);


        JobInfo jobInfo = builder.build();
        scheduler.schedule(jobInfo);
        Toast.makeText(this, R.string.start_ping_job_success, Toast.LENGTH_SHORT).show();
    }

    public void onEndAllPingJobsClicked(View view) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancelAll();
        Toast.makeText(this, R.string.end_ping_jobs_success, Toast.LENGTH_SHORT).show();
    }

    public void onQueryPingJobsClicked(View view) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.hint_list_jobs));
        sb.append("\n");
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            sb.append(jobInfo.getId());
            sb.append("\n");
        }
        ((TextView) findViewById(R.id.ping_result)).setText(sb.toString());
    }
}