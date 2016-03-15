package com.github.okamayana.liftmate.net;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class BluetoothThread implements Runnable {

    public static final int INCOMING_BYTES = 1;
    public static final UUID STD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String LOG_TAG = "BluetoothThread";

    private BluetoothSocket mSocket;
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;

    private BluetoothThreadHandler mHandler;
    private boolean mRunning;

    public BluetoothThread(BluetoothDevice device, BluetoothThreadHandler handler) {
        mHandler = handler;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            mSocket = device.createRfcommSocketToServiceRecord(STD_UUID);
            mSocket.connect();

            tmpIn = mSocket.getInputStream();
            tmpOut = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInputStream = tmpIn;
        mOutputStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        mRunning = true;
        while (mRunning) {
            try {
                bytes = mInputStream.read(buffer);
                mHandler.obtainMessage(INCOMING_BYTES, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                stop();
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        mRunning = false;

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] buffer) {
        try {
            mOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class BluetoothThreadHandler extends Handler {

        private final WeakReference<BluetoothThreadListener> mListenerWeakRef;

        public BluetoothThreadHandler(BluetoothThreadListener listener) {
            mListenerWeakRef = new WeakReference<>(listener);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothThreadListener listener = mListenerWeakRef.get();
            if (listener != null) {
                listener.onReceiveData((byte[]) msg.obj);
            }
        }
    }

    public interface BluetoothThreadListener {
        void onReceiveData(byte[] data);
    }
}
