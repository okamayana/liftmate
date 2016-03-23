package com.github.okamayana.liftmate.ui.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.BluetoothDeviceListAdapter;
import com.github.okamayana.liftmate.ui.adapters.BluetoothDeviceListAdapter.BluetoothSelectListener;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter.MainMenuItem;

public class BluetoothSearchDialogFragment extends AppCompatDialogFragment implements
        BluetoothSelectListener {

    public static final String LOG_TAG = "BluetoothSearchDialog";

    public static final String EXTRA_MAIN_MENU_ITEM = "extra_main_menu_item";

    public static BluetoothSearchDialogFragment newInstance(MainMenuItem mainMenuItem) {
        BluetoothSearchDialogFragment dialogFragment = new BluetoothSearchDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MAIN_MENU_ITEM, mainMenuItem);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDiscoveryReceiver mDiscoveryReceiver;
    private BluetoothDeviceListAdapter mDeviceListAdapter;

    private Button mRefreshButton;
    private Button mRefreshButtonFake;
    private View mEmptyView;

    private MainMenuItem mMainMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDiscoveryReceiver = new BluetoothDiscoveryReceiver();

        mMainMenuItem = (MainMenuItem) getArguments().getSerializable(EXTRA_MAIN_MENU_ITEM);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bluetooth_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDeviceListAdapter = new BluetoothDeviceListAdapter(getActivity(), mMainMenuItem,
                BluetoothSearchDialogFragment.this);
        RecyclerView deviceList = (RecyclerView) view.findViewById(R.id.bluetooth_list);
        deviceList.setAdapter(mDeviceListAdapter);
        deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyView = view.findViewById(R.id.bluetooth_no_devices);

        mRefreshButton = (Button) view.findViewById(R.id.btn_bluetooth_refresh);
        mRefreshButtonFake = (Button) view.findViewById(R.id.btn_bluetooth_refresh_disabled);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
                mRefreshButton.setVisibility(View.GONE);
                mRefreshButtonFake.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mDeviceListAdapter.clear();
        mDeviceListAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        getActivity().registerReceiver(mDiscoveryReceiver, filter);
        startDiscovery();
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mDiscoveryReceiver);
        stopDiscovery();
    }

    private void startDiscovery() {
        boolean started = mBluetoothAdapter.startDiscovery();
        updateDiscoveryStatusView(started);
        Log.d(LOG_TAG, "Discovery start: " + started);

        mDeviceListAdapter.clear();
        mDeviceListAdapter.notifyDataSetChanged();
        mEmptyView.setVisibility(View.GONE);
    }

    private void stopDiscovery() {
        boolean cancelled = mBluetoothAdapter.cancelDiscovery();
        updateDiscoveryStatusView(cancelled);
        Log.d(LOG_TAG, "Discovery cancel: " + cancelled);
    }

    private void updateDiscoveryStatusView(boolean started) {
        int visibility = started ? View.VISIBLE : View.GONE;

        View rootView = getView();
        if (rootView != null) {
            rootView.findViewById(R.id.discovery_scanning).setVisibility(visibility);
        }
    }

    @Override
    public void onBluetoothSelect() {
        final Activity activity = getActivity();
        if (activity instanceof OnDismissListener) {
            ((OnDismissListener) activity).onDismiss(getDialog());
        }
        dismiss();
    }

    private class BluetoothDiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                updateDiscoveryStatusView(true);

                mRefreshButton.setVisibility(View.GONE);
                mRefreshButtonFake.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                updateDiscoveryStatusView(false);

                mRefreshButton.setVisibility(View.VISIBLE);
                mRefreshButtonFake.setVisibility(View.GONE);

                if (mDeviceListAdapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();

                if ((!TextUtils.isEmpty(name) && name.startsWith("FlexBT")) ||
                        device.getAddress().startsWith("20:15:10:08:43:94")) {
                    mDeviceListAdapter.add(device);
                    mDeviceListAdapter.notifyDataSetChanged();
                }

                Log.d(LOG_TAG, "found: " + name
                        + " @(" + device.getAddress() + ")");
            }
        }
    }
}
