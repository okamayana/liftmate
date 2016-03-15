package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.net.BluetoothThread;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadHandler;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadListener;

public class FreeStyleActivity extends AppCompatActivity implements BluetoothThreadListener {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    public static final String LOG_TAG = "FreeStyleActivity";

    public static void start(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, FreeStyleActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private BluetoothThread mBluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);

        BluetoothDevice device = getIntent().getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);
        mBluetoothThread = new BluetoothThread(device, new BluetoothThreadHandler(
                FreeStyleActivity.this));
        new Thread(mBluetoothThread).start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothThread.stop();
    }

    @Override
    public void onReceiveData(char data) {
        Log.d(LOG_TAG, "Received character: " + data);
    }
}
