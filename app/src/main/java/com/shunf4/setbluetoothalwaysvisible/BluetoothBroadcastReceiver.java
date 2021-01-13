package com.shunf4.setbluetoothalwaysvisible;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;
            String address = device.getAddress();
            if (address.toLowerCase().equals(
                    PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_LISTENING_MAC, "")
                        .toLowerCase()
            )) {
                Log.i("SetBluetoothAlwaysVisible.BluetoothBroadcastReceiver",  address + " connected, starting job");
                MainActivity.doEndAllPingJobs(context);
                MainActivity.doStartPingJob(context, MainActivity.parseMacAddressString(address), address);
            }
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;
            String address = device.getAddress();
            if (address.toLowerCase().equals(
                    PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_LISTENING_MAC, "")
                            .toLowerCase()
            )) {
                Log.i("SetBluetoothAlwaysVisible.BluetoothBroadcastReceiver",  address + " disconnected, ending job");
                MainActivity.doEndAllPingJobs(context);
            }
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_OFF) {
                Log.i("SetBluetoothAlwaysVisible.BluetoothBroadcastReceiver",  "bluetooth off, ending job");
                MainActivity.doEndAllPingJobs(context);
            }
        }
    }
}
