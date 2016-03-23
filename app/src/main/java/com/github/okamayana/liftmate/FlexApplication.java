package com.github.okamayana.liftmate;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class FlexApplication extends Application {

    private BluetoothDevice mDevice;

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device) {
        mDevice = device;
    }
}
