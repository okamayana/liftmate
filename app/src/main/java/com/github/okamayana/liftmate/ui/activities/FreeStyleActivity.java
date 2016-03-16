package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.net.BluetoothThread;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadHandler;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadListener;

public class FreeStyleActivity extends AppCompatActivity implements BluetoothThreadListener,
        OnClickListener {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    public static final String LOG_TAG = "FreeStyleActivity";

    public static void start(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, FreeStyleActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private BluetoothThread mBluetoothThread;

    private int mRepCount;
    private long mTimerMillis;

    private Chronometer mChronometer;
    private TextView mRepCountView;
    private Button mPlayPauseButton;

    private boolean mPaused = false;
    private boolean mStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);

        BluetoothDevice device = getIntent().getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);
        BluetoothThreadHandler handler = new BluetoothThreadHandler(FreeStyleActivity.this);
        mBluetoothThread = new BluetoothThread(device, handler);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mRepCountView = (TextView) findViewById(R.id.current_reps_view);

        Button resetButton = (Button) findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(FreeStyleActivity.this);

        mPlayPauseButton = (Button) findViewById(R.id.btn_play_pause);
        mPlayPauseButton.setOnClickListener(FreeStyleActivity.this);

        Button doneButton = (Button) findViewById(R.id.btn_done);
        doneButton.setOnClickListener(FreeStyleActivity.this);

        mRepCount = 0;
        mTimerMillis = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothThread.stop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                mBluetoothThread.stop();
                resetWorkout();
                break;

            case R.id.btn_play_pause:
                if (!mStarted) {
                    new Thread(mBluetoothThread).start();
                    startWorkout();
                } else {
                    if (!mPaused) {
                        mBluetoothThread.stop();
                        pauseWorkout();
                    } else {
                        resumeWorkout();
                    }
                }
                break;

            case R.id.btn_done:
                break;
        }
    }

    private void resetWorkout() {
        mStarted = false;
        mPaused = false;

        mRepCount = 0;
        mRepCountView.setText(String.valueOf(mRepCount));

        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());

        mPlayPauseButton.setText("Start");
    }

    private void startWorkout() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        mPlayPauseButton.setText("Pause");
        mStarted = true;
    }

    private void pauseWorkout() {
        mPlayPauseButton.setText("Resume");
        mChronometer.stop();
        mPaused = true;
    }

    private void resumeWorkout() {
        mPlayPauseButton.setText("Pause");
        mChronometer.start();
        mPaused = false;
    }

    @Override
    public void onReceiveData(char data) {
        if (mStarted && !mPaused) {
            Log.d(LOG_TAG, "Received character: " + data);

            if (data == '1') {
                mRepCount++;
                mRepCountView.setText(String.valueOf(mRepCount));
            }
        }
    }
}
