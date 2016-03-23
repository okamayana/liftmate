package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.github.okamayana.liftmate.FlexApplication;
import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter.MainMenuItem;
import com.github.okamayana.liftmate.ui.fragments.BluetoothSearchDialogFragment;
import com.github.okamayana.liftmate.ui.fragments.ConfirmationDialogFragment;
import com.github.okamayana.liftmate.ui.fragments.ConfirmationDialogFragment.ConfirmationDialogFragmentListener;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnDismissListener,
        ConfirmationDialogFragmentListener {

    private enum ConnectionStatus {
        CONNECTED, DISCONNECTED
    }

    private View mConnectionStatusView;
    private BluetoothSearchDialogFragment mBluetoothSearchDialog;

    private ConnectionStatus mConnectionStatus;

    private String mDisconnectDialogTitle = "Confirm disconnecting from FlexWeight";
    private String mDisconnectDialogText = "Are you sure you want to disconnect from FlexWeight? Don't worry, you can still connect to a FlexWeight unit when starting a workout.";
    private ConfirmationDialogFragment mDisconnectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FlexApplication application = (FlexApplication) getApplication();
        application.setDevice(null);
        mConnectionStatus = ConnectionStatus.DISCONNECTED;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainMenuAdapter adapter = new MainMenuAdapter(MainActivity.this);

        RecyclerView menuListView = (RecyclerView) findViewById(R.id.menu_list_view);
        menuListView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        menuListView.setAdapter(adapter);

        mConnectionStatusView = findViewById(R.id.connection_status);
        mConnectionStatusView.setOnClickListener(MainActivity.this);

        mBluetoothSearchDialog = BluetoothSearchDialogFragment.newInstance(null);

        mDisconnectDialog = new ConfirmationDialogFragment();
        setupSimpleConfirmationDialog(mDisconnectDialog, mDisconnectDialogTitle,
                mDisconnectDialogText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume()");

        updateConnectionStatusView();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.connection_status:
                if (mConnectionStatus == ConnectionStatus.DISCONNECTED) {
                    showBluetoothSearchDialog();
                } else {
                    showConfirmationDialog(mDisconnectDialog);
                }
                break;
        }
    }

    @Override
    public void onConfirm(ConfirmationDialogFragment dialog) {
        String title = dialog.getDialogTitle();

        if (mDisconnectDialogTitle.equals(title)) {
            FlexApplication application = (FlexApplication) getApplication();
            application.setDevice(null);

            updateConnectionStatusView();
        }
    }

    @Override
    public void onCancel(ConfirmationDialogFragment dialog) {
    }

    private void updateConnectionStatusView() {
        FlexApplication application = (FlexApplication) getApplication();
        BluetoothDevice device = application.getDevice();

        TextView connectionTextView = (TextView) mConnectionStatusView.findViewById(
                R.id.connection_status_text_view);
        if (device == null) {
            mConnectionStatusView.setBackground(ContextCompat.getDrawable(
                    MainActivity.this, R.drawable.selector_connection_red));
            connectionTextView.setText("Not Connected to FlexWeight");

            mConnectionStatus = ConnectionStatus.DISCONNECTED;
        } else {
            mConnectionStatusView.setBackground(ContextCompat.getDrawable(
                    MainActivity.this, R.drawable.selector_connection_green));
            connectionTextView.setText("Connected to FlexWeight");

            mConnectionStatus = ConnectionStatus.CONNECTED;
        }
    }

    private void showBluetoothSearchDialog() {
        mBluetoothSearchDialog.show(getSupportFragmentManager(), null);
    }

    public void showConfirmationDialog(ConfirmationDialogFragment dialog) {
        if (!dialog.isAdded()) {
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    private void setupSimpleConfirmationDialog(ConfirmationDialogFragment dialog,
                                               String dialogTitle, String dialogText) {
        dialog.setHasCancel(true);
        dialog.setOnConfirmationDialogFragmentListener(MainActivity.this);
        dialog.setDialogTitle(dialogTitle);
        dialog.setDialogText(dialogText);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        updateConnectionStatusView();
    }
}
