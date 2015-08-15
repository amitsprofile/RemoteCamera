package com.amit.camera.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by amit on 6/14/15.
 */
public class BluetoothServer {
    private BluetoothAdapter mBluetoothAdapter;
    private final String NAME = "RemoteCamera";
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private UUID MY_UUID;
    private Context context;
    public BluetoothServer(Context context){
        this.context = context;
        acceptThread = new AcceptThread();

    }

    public void manageConnectedSocket(BluetoothSocket socket){
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void startServer() {
        acceptThread.run();
    }

    public void stopServer() {
        acceptThread.cancel();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread() {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
            }

            Method getUuidsMethod = null;
            try {
                getUuidsMethod = mBluetoothAdapter.getClass().getDeclaredMethod("getUuids", null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            ParcelUuid[] uuids = new ParcelUuid[0];
            try {
                uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            for (ParcelUuid uuid: uuids) {
                Log.e("RemoteCamera", "UUID: " + uuid.getUuid().toString());
            }

            MY_UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b1234");
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final int MESSAGE_READ = 0;
        private Handler mHandler = new Handler();

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
