package com.amit.camera.remotecamera;

import android.content.Context;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.amit.camera.main.BluetoothController;


public class RemoteCameraActivity extends ActionBarActivity {
    private final String TAG = "RemoteCamera";
    private Camera mCamera;
    private CameraPreview mPreview;
    private CameraController mCameraController;
    private Context mainActivityContext;
    private Camera.PictureCallback mPicture;
    //private BluetoothController mBluetoothController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "RemoteCameraActivity::OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_camera);

        mainActivityContext = getApplicationContext();

        // Create an instance of Camera
        mCamera = mCameraController.getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        // Create camera controller instance to control camera.
        mCameraController = new CameraController(mPreview, mCamera, mainActivityContext);
        //mPicture = mCameraController.getmPicture();

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
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

    public void captureImage(View v) {
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }
}
