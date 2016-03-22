package com.github.okamayana.liftmate.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.google.CountdownChronometer;

import java.util.concurrent.TimeUnit;

public class TimeTrialWorkoutFragment extends Fragment implements OnClickListener {

    public static final String EXTRA_TOTAL_SETS = "extra_total_sets";
    public static final String EXTRA_TOTAL_REPS = "extra_total_reps";
    public static final String EXTRA_MINS_PER_SET = "extra_mins_per_set";
    public static final String EXTRA_SECS_PER_SET = "extra_secs_per_set";
    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    private static final String FORMAT_SETS_REPS = "%d/%d";
    private static final String FORMAT_SET_TIME = "%02d:%02d";

    public static TimeTrialWorkoutFragment newInstance(int totalSets, int totalReps,
                                                       int targetMinsPerSet, int targetSecsPerSet,
                                                       BluetoothDevice device) {
        TimeTrialWorkoutFragment fragment = new TimeTrialWorkoutFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_TOTAL_SETS, totalSets);
        args.putInt(EXTRA_TOTAL_REPS, totalReps);
        args.putInt(EXTRA_MINS_PER_SET, targetMinsPerSet);
        args.putInt(EXTRA_SECS_PER_SET, targetSecsPerSet);
        args.putParcelable(EXTRA_BLUETOOTH_DEVICE, device);
        fragment.setArguments(args);

        return fragment;
    }

    private int mTargetSets;
    private int mTargetReps;
    private int mSets;
    private int mReps;

    private long mTimeInSet;
    private long mTargetSetTime;

    private TextView mRepsView;
    private TextView mSetsView;
    private CountdownChronometer mChronometer;

    private Button mPlayPauseButton;

    private boolean mStarted;
    private boolean mPaused;
    private long mTimePaused;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSets = 0;
        mReps = 0;

        Bundle args = getArguments();
        mTargetSets = args.getInt(EXTRA_TOTAL_SETS);
        mTargetReps = args.getInt(EXTRA_TOTAL_REPS);
        int targetMinsPerSet = args.getInt(EXTRA_MINS_PER_SET);
        int targetSecsPerSet = args.getInt(EXTRA_SECS_PER_SET);

        mTargetSetTime = (long) targetMinsPerSet * 60L * 1000L + (long) targetSecsPerSet * 1000L;
        mTimeInSet = mTargetSetTime;

        mStarted = false;
        mPaused = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_trial_workout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRepsView = (TextView) view.findViewById(R.id.current_reps_view);
        mSetsView = (TextView) view.findViewById(R.id.current_set_view);
        mChronometer = (CountdownChronometer) view.findViewById(R.id.set_time_view);

        mPlayPauseButton = (Button) view.findViewById(R.id.btn_play_pause);
        mPlayPauseButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        Button resetButton = (Button) view.findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        updateRepsView();
        updateSetsView();
        updateSetTimeView(false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
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
        }
    }

    private void resetWorkout() {
        mPlayPauseButton.setText("Start");
        mStarted = false;
        mPaused = false;

        mTimePaused = 0;
        mTimeInSet = mTargetSetTime;
        mChronometer.stop();
        updateSetTimeView(true);
    }

    private void startWorkout() {
        mPlayPauseButton.setText("Pause");
        mStarted = true;
        mPaused = false;

        mChronometer.setBase(System.currentTimeMillis() + mTimeInSet);
        mChronometer.start();
        for (int i = 0; i < 3; i++) {
            mTimePaused = mChronometer.getBase() - System.currentTimeMillis();
            mChronometer.stop();
            mChronometer.setBase(System.currentTimeMillis() + mTimePaused);
            mChronometer.start();
        }
    }

    private void pauseWorkout() {
        mPlayPauseButton.setText("Resume");
        mPaused = true;

        mTimePaused = mChronometer.getBase() - System.currentTimeMillis();
        mChronometer.stop();
    }

    private void resumeWorkout() {
        mPlayPauseButton.setText("Pause");
        mPaused = false;

        mChronometer.setBase(System.currentTimeMillis() + mTimePaused);
        mChronometer.start();
    }

    private void updateRepsView() {
        mRepsView.setText(String.format(FORMAT_SETS_REPS, mReps, mTargetReps));
    }

    private void updateSetsView() {
        mSetsView.setText(String.format(FORMAT_SETS_REPS, mSets, mTargetSets));
    }

    private void updateSetTimeView(boolean reset) {
        long millis = reset ? mTargetSetTime : mTimeInSet;

        mChronometer.setText(String.format(FORMAT_SET_TIME, 0, 0));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(minutes);
        mChronometer.setText(String.format(FORMAT_SET_TIME, minutes, seconds));
    }
}
