package com.amit.camera.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amit.camera.remotecamera.R;
import com.amit.camera.remotecamera.RemoteCameraActivity;

import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private BluetoothController mBluetoothController;
    private BluetoothServer mBluetoothServer;
    private String uuid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Get UUID of the device
        //TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //uuid = tManager.getDeviceId();


        // Create instance of Bluetooth Controller and switchOn the bluetooth
        mBluetoothController = new BluetoothController(getApplicationContext(), this);
        if(mBluetoothController != null) {
            mBluetoothController.bluetoothOn();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void localCameraButton(View v) {
        Toast.makeText(getApplicationContext(), "Starting Camera",
                Toast.LENGTH_LONG).show();
        Intent mCamera = new Intent(this, RemoteCameraActivity.class);
        mCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mCamera);
    }

    public void remoteCameraButton(View v) {
        Toast.makeText(getApplicationContext(), "Starting Remote",
                Toast.LENGTH_LONG).show();
        if(mBluetoothController == null) {
            return;
        }
        mBluetoothController.bluetoothScan();
        mBluetoothController.getPairedDevices();

        mBluetoothServer =  new BluetoothServer(getApplicationContext());
        mBluetoothServer.startServer();
    }
}
