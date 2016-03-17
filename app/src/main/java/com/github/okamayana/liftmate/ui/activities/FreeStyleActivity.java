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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.net.BluetoothThread;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadHandler;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadListener;

public class FreeStyleActivity extends AppCompatActivity implements BluetoothThreadListener,
        OnClickListener, OnSeekBarChangeListener {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    public static final String LOG_TAG = "FreeStyleActivity";

    public static void start(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, FreeStyleActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private BluetoothThread mBluetoothThread;

    private int mRepCount;
    private long mTimePaused;

    private Chronometer mChronometer;
    private TextView mRepCountView;
    private TextView mPushResistanceView;
    private TextView mPullResistanceView;
    private Button mPlayPauseButton;

    private SeekBar mResistanceSlider;

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

        mResistanceSlider = (SeekBar) findViewById(R.id.resistance_slider);
        mResistanceSlider.setOnSeekBarChangeListener(FreeStyleActivity.this);

        mPullResistanceView = (TextView) findViewById(R.id.pull_resistance_view);
        mPushResistanceView = (TextView) findViewById(R.id.push_resistance_view);

        int initialProgress = mResistanceSlider.getProgress();
        mPushResistanceView.setText(String.valueOf(initialProgress - 2));
        mPullResistanceView.setText(String.valueOf(initialProgress + 2));

        mRepCount = 0;
        mTimePaused = 0;

        new Thread(mBluetoothThread).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothThread.stop();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int pushResistance = progress - 2;
        mPushResistanceView.setText(String.valueOf(pushResistance));

        int pullResistance = progress + 2;
        mPullResistanceView.setText(String.valueOf(pullResistance));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        String resistanceStr = String.valueOf(mResistanceSlider.getProgress());
        Log.d(LOG_TAG, "Sending resistance value: " + resistanceStr);
        mBluetoothThread.write(resistanceStr.getBytes());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                resetWorkout();
                break;

            case R.id.btn_play_pause:
                if (!mStarted) {
                    startWorkout();         // start button
                } else {
                    if (!mPaused) {
                        pauseWorkout();     // pause button
                    } else {
                        resumeWorkout();    // resume button
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

        mTimePaused = 0;
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
        mPaused = false;
    }

    private void pauseWorkout() {
        mPlayPauseButton.setText("Resume");
        mTimePaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();
        mPaused = true;
    }

    private void resumeWorkout() {
        mPlayPauseButton.setText("Pause");
        mChronometer.setBase(SystemClock.elapsedRealtime() + mTimePaused);
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

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
}
