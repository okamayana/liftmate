package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.okamayana.liftmate.R;

public class FreeStyleActivity extends AppCompatActivity {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    public static void start(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, FreeStyleActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);

        mBluetoothDevice = getIntent().getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
