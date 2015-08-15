package com.amit.camera.remotecamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by amit on 6/12/15.
 */
public class CameraController {
    private final String TAG = "remote_camera";
    private CameraPreview mPreview;
    private Camera mCamera;
    private Context context;

    public CameraController(CameraPreview mPreview, Camera mCamera, Context context) {
        this.mPreview = mPreview;
        this.mCamera = mCamera;
        this.context = context;
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
                    Environment.DIRECTORY_PICTURES), context.getPackageName());
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

    public Camera.PictureCallback getmPicture(){
        return mPicture;
    }

}