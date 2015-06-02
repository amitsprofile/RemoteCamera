package com.amit.camera.remotecamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    private final String TAG = "RemoteCamera";
    private Camera mCamera;
    private CameraPreview mPreview;

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here
            setOrientation();

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        public void setOrientation(){
            Camera.Parameters p = mCamera.getParameters();
            if (Integer.parseInt(Build.VERSION.SDK) >= 8)
                setDisplayOrientation(mCamera, 90);
            else
            {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    p.set("orientation", "portrait");
                    p.set("rotation", 90);
                }
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    p.set("orientation", "landscape");
                    p.set("rotation", 90);
                }
            }
        }

        protected void setDisplayOrientation(Camera camera, int angle) {
            Method downPolymorphic;
            try {
                downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if (downPolymorphic != null)
                    downPolymorphic.invoke(camera, new Object[]{angle});
            } catch (Exception e1) {
            }
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        protected static final int MEDIA_TYPE_IMAGE = 0;
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "Picture taken");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            Log.i(TAG, "File name : " + pictureFile.toString());
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                Log.i(TAG, "Writing image to file");
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            try {
                mCamera.stopPreview();
            } catch (Exception e){
            }
            // start preview with new settings
            try {
                //mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        private File getOutputMediaFile(int type) {
            Log.i(TAG, "getOutputMediaFile");
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), getPackageName());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create storage directory.");
                    return null;
                }
            }
            String timeStamp =
                    new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
            if (type == MEDIA_TYPE_IMAGE) {
                return new File(dir.getPath() + File.separator + "IMG_"
                        + timeStamp + ".jpg");
            } else {
                return null;
            }
        }
    };

    public void captureImage(View v) {
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
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
}
