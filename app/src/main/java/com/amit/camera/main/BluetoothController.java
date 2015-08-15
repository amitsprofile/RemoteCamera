package com.amit.camera.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amit.camera.main.MainActivity;
import com.amit.camera.remotecamera.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by amit on 6/13/15.
 */
public class BluetoothController {

    private Button bluetoothOn;
    private Button bluetoothOff;
    private Button bluetoothScan;
    private Button bluetoothPaired;
    private BluetoothAdapter myBluetoothAdapter;
    private Context context;
    private Activity activity;

    private ArrayAdapter<String> btArrayAdapter;
    private ListView listDevicesFound;
    private Set<BluetoothDevice> pairedDevices;

    public BluetoothController(Context context, Activity activity) {
        listDevicesFound = (ListView)activity.findViewById(R.id.devicesfound);
        if(listDevicesFound == null) {
            Log.e("RemoteCamera","List view is nul");
            Toast.makeText(context, "List view is null"
                    , Toast.LENGTH_LONG).show();
        }
        btArrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1);
        activity.registerReceiver(myBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        listDevicesFound.setAdapter(btArrayAdapter);
        this.context = context;
        this.activity = activity;
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(context,"Bluetooth not found",
                    Toast.LENGTH_LONG).show();
            // Device does not support Bluetooth
        }
    }

    public void bluetoothOn() {
        // TODO Auto-generated method stub
        if(myBluetoothAdapter == null) {
            Toast.makeText(context,"Bluetooth not found",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!myBluetoothAdapter.isEnabled()) {
            //Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //activity.startActivityForResult(turnOn, 0);
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            activity.startActivity(discoverableIntent);

            Toast.makeText(context, "Bluetooth turned on"
                    , Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context,"Bluetooth already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void bluetoothOff(){

        myBluetoothAdapter.disable();
        Toast.makeText(context,"Bluetooth turned off" , Toast.LENGTH_LONG).show();
    }

    public void bluetoothScan() {

        btArrayAdapter.clear();
        myBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver myBluetoothReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void getPairedDevices() {
        // TODO Auto-generated method stub
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        @SuppressWarnings("rawtypes")
        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());

        Toast.makeText(context,"Showing Paired Devices",
                Toast.LENGTH_SHORT).show();
        @SuppressWarnings("rawtypes")
        final ArrayAdapter adapter = new ArrayAdapter(activity,android.R.layout.simple_list_item_1, list);
        listDevicesFound.setAdapter(adapter);
    }

    public void onDestroy() {

        context.unregisterReceiver(myBluetoothReceiver);
    }


}