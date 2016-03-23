package com.github.okamayana.liftmate.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.google.CountdownChronometer;
import com.github.okamayana.liftmate.net.BluetoothThread;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadHandler;
import com.github.okamayana.liftmate.net.BluetoothThread.BluetoothThreadListener;
import com.github.okamayana.liftmate.ui.activities.TimeTrialActivity.OnBackPressedListener;
import com.github.okamayana.liftmate.ui.fragments.ConfirmationDialogFragment.ConfirmationDialogFragmentListener;
import com.github.okamayana.liftmate.util.DateTimeUtil;

public class TimeTrialWorkoutFragment extends Fragment implements
        OnClickListener, OnChronometerTickListener, ConfirmationDialogFragmentListener,
        BluetoothThreadListener, OnSeekBarChangeListener, OnBackPressedListener {

    public static final String EXTRA_TOTAL_SETS = "extra_total_sets";
    public static final String EXTRA_TOTAL_REPS = "extra_total_reps";
    public static final String EXTRA_MINS_PER_SET = "extra_mins_per_set";
    public static final String EXTRA_SECS_PER_SET = "extra_secs_per_set";
    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    private static final String FORMAT_SETS_REPS = "%d/%d";
    private static final String FORMAT_SET_TIME = "%02d:%02d";

    private static final String LOG_TAG = "TimeTrialWorkout";

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

    private String mResetDialogTitle = "Confirm workout reset";
    private String mResetDialogText = "Are you sure you want to reset your workout? This will erase all workout progress.";
    private ConfirmationDialogFragment mResetDialog;

    private String mQuitDialogTitle = "Confirm workout exit";
    private String mQuitDialogText = "Are you sure you want to leave your workout? This will erase all workout progress.";
    private ConfirmationDialogFragment mQuitDialog;

    private String mSuccessDialogTitle = "Time trial challenge complete";
    private String mSuccessDialogText = "Congratulations! You have successfully completed your Time Trial workout challenge!\n\nYou may choose to retry your challenge, or go back to the home screen.";
    private String mSuccessDialogConfirmText = "Retry";
    private String mSuccessDialogCancelText = "Leave";
    private ConfirmationDialogFragment mSuccessDialog;

    private String mFailDialogTitle = "Time trial challenge failed";
    private String mFailDialogText = "Unfortunately, you were not able to successfully complete your Time Trial workout challenge.\n\nYou may choose to retry the challenge, or go back to the home screen.";
    private String mFailDialogConfirmText = "Retry";
    private String mFailDialogCancelText = "Leave";
    private ConfirmationDialogFragment mFailDialog;

    private TextView mPushResistanceView;
    private TextView mPullResistanceView;

    private BluetoothThread mBluetoothThread;
    private SeekBar mResistanceSlider;

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

        mResetDialog = new ConfirmationDialogFragment();
        setupSimpleConfirmationDialog(mResetDialog, mResetDialogTitle, mResetDialogText);

        mQuitDialog = new ConfirmationDialogFragment();
        setupSimpleConfirmationDialog(mQuitDialog, mQuitDialogTitle, mQuitDialogText);

        mSuccessDialog = new ConfirmationDialogFragment();
        setupConfirmationDialog(mSuccessDialog, mSuccessDialogTitle, mSuccessDialogText,
                mSuccessDialogConfirmText, mSuccessDialogCancelText);

        mFailDialog = new ConfirmationDialogFragment();
        setupConfirmationDialog(mFailDialog, mFailDialogTitle, mFailDialogText,
                mFailDialogConfirmText, mFailDialogCancelText);

        BluetoothDevice device = args.getParcelable(EXTRA_BLUETOOTH_DEVICE);
        BluetoothThreadHandler handler = new BluetoothThreadHandler(TimeTrialWorkoutFragment.this);
        mBluetoothThread = new BluetoothThread(device, handler);

        new Thread(mBluetoothThread).start();
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

        mPullResistanceView = (TextView) view.findViewById(R.id.pull_resistance_view);
        mPushResistanceView = (TextView) view.findViewById(R.id.push_resistance_view);

        mResistanceSlider = (SeekBar) view.findViewById(R.id.resistance_slider);
        mResistanceSlider.setOnSeekBarChangeListener(TimeTrialWorkoutFragment.this);

        mChronometer = (CountdownChronometer) view.findViewById(R.id.set_time_view);
        mChronometer.setOnChronometerTickListener(TimeTrialWorkoutFragment.this);

        mPlayPauseButton = (Button) view.findViewById(R.id.btn_play_pause);
        mPlayPauseButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        Button resetButton = (Button) view.findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        Button quitButton = (Button) view.findViewById(R.id.btn_quit);
        quitButton.setOnClickListener(TimeTrialWorkoutFragment.this);

        updateRepsView();
        updateSetsView();
        updateSetTimeView(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothThread.stop();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_reset:
                if (mStarted) {
                    showConfirmationDialog(mResetDialog);
                    if (!mPaused) {
                        pauseWorkout();
                    }
                }
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

            case R.id.btn_quit:
                showConfirmationDialog(mQuitDialog);
                if (mStarted && !mPaused) {
                    pauseWorkout();
                }
                break;
        }
    }

    public void showConfirmationDialog(ConfirmationDialogFragment dialog) {
        if (!dialog.isAdded()) {
            dialog.show(getActivity().getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onConfirm(ConfirmationDialogFragment dialog) {
        String title = dialog.getDialogTitle();

        if (mResetDialogTitle.equals(title)) {
            resetWorkout();
        } else if (mQuitDialogTitle.equals(title)) {
            getActivity().finish();
        } else if (mSuccessDialogTitle.equals(title)) {
            resetWorkout();
        } else if (mFailDialogTitle.equals(title)) {
            resetWorkout();
        }
    }

    @Override
    public void onCancel(ConfirmationDialogFragment dialog) {
        String title = dialog.getDialogTitle();

        if (mFailDialogTitle.equals(title)) {
            getActivity().finish();
        } else if (mSuccessDialogTitle.equals(title)) {
            getActivity().finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (hasNoDialogs()) {
            showConfirmationDialog(mQuitDialog);
            if (mStarted && !mPaused) {
                pauseWorkout();
            }
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
                    showConfirmationDialog(mSuccessDialog);
                    pauseWorkout();
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
                    showConfirmationDialog(mFailDialog);
                    pauseWorkout();
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

    private void setupSimpleConfirmationDialog(ConfirmationDialogFragment dialog,
                                               String dialogTitle, String dialogText) {
        dialog.setHasCancel(true);
        dialog.setOnConfirmationDialogFragmentListener(TimeTrialWorkoutFragment.this);
        dialog.setDialogTitle(dialogTitle);
        dialog.setDialogText(dialogText);
    }

    public void setupConfirmationDialog(ConfirmationDialogFragment dialog, String dialogTitle,
                                        String dialogText, String confirmButtonText,
                                        String cancelButtonText) {
        dialog.setHasCancel(true);
        dialog.setOnConfirmationDialogFragmentListener(TimeTrialWorkoutFragment.this);
        dialog.setDialogTitle(dialogTitle);
        dialog.setDialogText(dialogText);
        dialog.setConfirmButtonText(confirmButtonText);
        dialog.setCancelButtonText(cancelButtonText);
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
    public void onReceiveData(char data) {
        if (mStarted && !mPaused) {

            if (data == '1') {
                onHandleRep();
            }
        }
    }

    private boolean hasNoDialogs() {
        return !mResetDialog.isVisible() && !mQuitDialog.isVisible()
                && !mSuccessDialog.isVisible() && !mFailDialog.isVisible();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
}
