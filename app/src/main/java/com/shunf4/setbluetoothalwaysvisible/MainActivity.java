package com.shunf4.setbluetoothalwaysvisible;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_DISCOVERABLE_BT = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRefreshStatusClicked(null);
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
}