package com.github.okamayana.liftmate.ui.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.okamayana.liftmate.FlexApplication;
import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.activities.FreeStyleActivity;
import com.github.okamayana.liftmate.ui.adapters.BluetoothDeviceListAdapter.BluetoothDeviceViewHolder;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter.MainMenuItem;
import com.github.okamayana.liftmate.ui.fragments.TimeTrialSetupDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceListAdapter extends Adapter<BluetoothDeviceViewHolder> {

    public static class BluetoothDeviceViewHolder extends ViewHolder {

        TextView nameView;
        TextView macAddressView;

        public BluetoothDeviceViewHolder(View itemView) {
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.bluetooth_name);
            macAddressView = (TextView) itemView.findViewById(R.id.bluetooth_mac);
        }
    }

    private Context mContext;
    private List<BluetoothDevice> mItems;
    private MainMenuItem mMainMenuItem;

    private BluetoothSelectListener mDeviceSelectListener;

    public BluetoothDeviceListAdapter(Context context, MainMenuItem mainMenuItem,
                                      BluetoothSelectListener deviceSelectListener) {
        mContext = context;
        mItems = new ArrayList<>();
        mMainMenuItem = mainMenuItem;
        mDeviceSelectListener = deviceSelectListener;
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(
                R.layout.list_item_device, parent, false);

        return new BluetoothDeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
        final BluetoothDevice device = mItems.get(position);

        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = "?";
        }
        holder.nameView.setText(name);

        holder.macAddressView.setText(device.getAddress());
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FlexApplication application = (FlexApplication) ((Activity) mContext).getApplication();
                application.setDevice(device);

                if (mDeviceSelectListener != null) {
                    mDeviceSelectListener.onBluetoothSelect();
                }

                if (mMainMenuItem != null) {
                    startWorkoutActivity(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(BluetoothDevice device) {
        if (!mItems.contains(device)) {
            mItems.add(device);
        }
    }

    public void clear() {
        mItems.clear();
    }

    private void startWorkoutActivity(BluetoothDevice device) {
        switch (mMainMenuItem) {
            case TIME_TRIAL_MODE:
                showTimeTrialSetupDialog(device);
                break;

            case FREE_STYLE_MODE:
                FreeStyleActivity.start(mContext, device);
                break;
        }

        if (mDeviceSelectListener != null) {
            mDeviceSelectListener.onBluetoothSelect();
        }
    }

    private void showTimeTrialSetupDialog(BluetoothDevice device) {
        TimeTrialSetupDialogFragment setupDialog =
                TimeTrialSetupDialogFragment.newInstance(device);
        setupDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(),
                null);
    }

    public interface BluetoothSelectListener {
        void onBluetoothSelect();
    }
}
