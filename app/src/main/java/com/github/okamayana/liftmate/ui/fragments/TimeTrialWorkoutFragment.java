package com.github.okamayana.liftmate.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;

public class TimeTrialWorkoutFragment extends Fragment {

    public static final String EXTRA_TOTAL_SETS = "extra_total_sets";
    public static final String EXTRA_TOTAL_REPS = "extra_total_reps";
    public static final String EXTRA_MINS_PER_SET = "extra_mins_per_set";
    public static final String EXTRA_SECS_PER_SET = "extra_secs_per_set";
    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    private static final String FORMAT_SETS_REPS = "%d/%d";

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

    private TextView mRepsView;
    private TextView mSetsView;
    private TextView mSetTimeView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSets = 0;
        mReps = 0;

        Bundle args = getArguments();
        mTargetSets = args.getInt(EXTRA_TOTAL_SETS);
        mTargetReps = args.getInt(EXTRA_TOTAL_REPS);
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
        mSetTimeView = (TextView) view.findViewById(R.id.set_time_view);

        updateRepsView();
        updateSetsView();
    }

    private void updateRepsView() {
        mRepsView.setText(String.format(FORMAT_SETS_REPS, mReps, mTargetReps));
    }

    private void updateSetsView() {
        mSetsView.setText(String.format(FORMAT_SETS_REPS, mSets, mTargetSets));
    }
}
