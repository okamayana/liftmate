package com.github.okamayana.liftmate.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.google.CountdownChronometer;
import com.github.okamayana.liftmate.util.DateTimeUtil;

public class TimeTrialWorkoutFragment extends Fragment implements OnClickListener,
        OnChronometerTickListener {

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
    private int mMinutesPerSet;
    private int mSecondsPerSet;

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

        mMinutesPerSet = targetMinsPerSet;
        mSecondsPerSet = targetSecsPerSet;

        mTargetSetTime = (long) mMinutesPerSet * 60L * 1000L + (long) mSecondsPerSet * 1000L;
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
        mChronometer.setOnChronometerTickListener(TimeTrialWorkoutFragment.this);

        mPlayPauseButton = (Button) view.findViewById(R.id.btn_play_pause);
        mPlayPauseButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        Button resetButton = (Button) view.findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        view.findViewById(R.id.current_reps_container).setOnClickListener(
                TimeTrialWorkoutFragment.this);

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

            case R.id.current_reps_container:
                onHandleRep();
                break;
        }
    }

    private void onHandleRep() {
        if (mStarted && !mPaused) {
            mReps++;
            updateRepsView();

            if (mReps >= mTargetReps) {
                mReps = 0;
                updateRepsView();

                mSets++;
                updateSetsView();
                updateSetFragment();

                if (mSets >= mTargetSets) {
                    Toast.makeText(getActivity(), "You completed the challenge!", Toast.LENGTH_SHORT).show();
                    resetWorkout();
                    return;
                }

                mChronometer.stop();
                updateSetTimeView(true);
                kickStartChronometer(System.currentTimeMillis() + 1000 + mTimeInSet);
            }
        }
    }

    @Override
    public void onChronometerTick(final Chronometer chronometer) {
        mChronometer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ("00:00".equals(mChronometer.getText().toString())) {
                    Toast.makeText(getActivity(), "You failed the challenge!", Toast.LENGTH_SHORT).show();
                    resetWorkout();
                }
            }
        }, 2000);
    }

    private void updateSetFragment() {
        long timeRemaining = DateTimeUtil.getMillisFromChronometer(mChronometer);
        long setTime = mTargetSetTime - timeRemaining;

        Intent setBroadcastIntent = new Intent();
        setBroadcastIntent.setAction(TimeTrialSetsFragment.ACTION_SET_COMPLETE);
        setBroadcastIntent.putExtra(TimeTrialSetsFragment.EXTRA_SET_NUNBER, mSets);
        setBroadcastIntent.putExtra(TimeTrialSetsFragment.EXTRA_SET_TIME, setTime);
        setBroadcastIntent.putExtra(TimeTrialSetsFragment.EXTRA_REPS_IN_SET, mTargetReps);

        getActivity().sendBroadcast(setBroadcastIntent);
    }

    private void resetSetFragment() {
        Intent setBroadcastIntent = new Intent();
        setBroadcastIntent.setAction(TimeTrialSetsFragment.ACTION_RESET);

        getActivity().sendBroadcast(setBroadcastIntent);
    }

    private void resetWorkout() {
        mPlayPauseButton.setText("Start");
        mStarted = false;
        mPaused = false;

        mTimePaused = 0;
        mTimeInSet = mTargetSetTime;
        mChronometer.stop();
        updateSetTimeView(true);

        mReps = 0;
        mSets = 0;
        updateSetsView();
        updateRepsView();
        resetSetFragment();
    }

    private void startWorkout() {
        mPlayPauseButton.setText("Pause");
        mStarted = true;
        mPaused = false;

        kickStartChronometer(System.currentTimeMillis() + mTimeInSet);
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
        String updated = DateTimeUtil.getSetTimeViewString(reset, mTargetSetTime, mTimeInSet,
                FORMAT_SET_TIME);
        mChronometer.setText(updated);
    }

    /**
     * Hack to start the CountdownChronometer to ensure it does not get stuck
     * in the first second.
     */
    private void kickStartChronometer(long base) {
        mChronometer.setBase(base);
        mChronometer.start();
        for (int i = 0; i < 3; i++) {
            mTimePaused = mChronometer.getBase() - System.currentTimeMillis();
            mChronometer.stop();
            mChronometer.setBase(System.currentTimeMillis() + mTimePaused);
            mChronometer.start();
        }
    }
}
