package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.BluetoothDeviceListAdapter;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter.MainMenuItem;

public class BluetoothSearchActivity extends AppCompatActivity {

    public static final String LOG_TAG = "BluetoothSearchActivity";

    public static final String EXTRA_MAIN_MENU_ITEM = "extra_main_menu_item";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDiscoveryReceiver mDiscoveryReceiver;

    private BluetoothDeviceListAdapter mDeviceListAdapter;

    public static void start(Context context, MainMenuItem mainMenuItem) {
        Intent intent = new Intent(context, BluetoothSearchActivity.class);
        intent.putExtra(EXTRA_MAIN_MENU_ITEM, mainMenuItem);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainMenuItem mainMenuItem = (MainMenuItem) getIntent().getSerializableExtra(
                EXTRA_MAIN_MENU_ITEM);

        mDeviceListAdapter = new BluetoothDeviceListAdapter(BluetoothSearchActivity.this, mainMenuItem);
        RecyclerView deviceList = (RecyclerView) findViewById(R.id.bluetooth_list);
        deviceList.setAdapter(mDeviceListAdapter);
        deviceList.setLayoutManager(new LinearLayoutManager(BluetoothSearchActivity.this));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDiscoveryReceiver = new BluetoothDiscoveryReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDeviceListAdapter.clear();
        mDeviceListAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDiscoveryReceiver, filter);

        boolean started = mBluetoothAdapter.startDiscovery();
        updateDiscoveryStatusView(started);
        Log.d(LOG_TAG, "Discovery start: " + started);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mDiscoveryReceiver);

        boolean cancelled = mBluetoothAdapter.cancelDiscovery();
        updateDiscoveryStatusView(cancelled);
        Log.d(LOG_TAG, "Discovery cancel: " + cancelled);
    }

    private void updateDiscoveryStatusView(boolean started) {
        int visibility = started ? View.VISIBLE : View.GONE;
        findViewById(R.id.discovery_scanning).setVisibility(visibility);
    }

    private class BluetoothDiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                updateDiscoveryStatusView(true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                updateDiscoveryStatusView(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceListAdapter.add(device);
                mDeviceListAdapter.notifyDataSetChanged();

                Log.d(LOG_TAG, "found device: " + device.getName()
                        + " @ (" + device.getAddress() + ")");
            }
        }
    }
}
