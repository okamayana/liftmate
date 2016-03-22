package com.github.okamayana.liftmate.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.activities.TimeTrialActivity;

public class TimeTrialSetupDialogFragment extends AppCompatDialogFragment implements
        OnClickListener {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";

    public static TimeTrialSetupDialogFragment newInstance(BluetoothDevice device) {
        TimeTrialSetupDialogFragment dialogFragment = new TimeTrialSetupDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_BLUETOOTH_DEVICE, device);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    private static final String[] SPINNER_VALUES = getSpinnerValues();

    private EditText mTargetSetsEdit;
    private EditText mTargetRepsEdit;

    private Spinner mMinutesSpinner;
    private Spinner mSecondsSpinner;

    private BluetoothDevice mBluetoothDevice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothDevice = getArguments().getParcelable(EXTRA_BLUETOOTH_DEVICE);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_time_trial_setup, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTargetSetsEdit = (EditText) view.findViewById(R.id.target_sets_input);
        mTargetRepsEdit = (EditText) view.findViewById(R.id.target_reps_input);

        mMinutesSpinner = (Spinner) view.findViewById(R.id.mins_per_set_spinner);
        ArrayAdapter<String> minutesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, SPINNER_VALUES);
        mMinutesSpinner.setAdapter(minutesAdapter);
        mMinutesSpinner.setSelection(0);

        mSecondsSpinner = (Spinner) view.findViewById(R.id.secs_per_set_spinner);
        ArrayAdapter<String> secondsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, SPINNER_VALUES);
        mSecondsSpinner.setAdapter(secondsAdapter);
        mSecondsSpinner.setSelection(0);

        Button startButton = (Button) view.findViewById(R.id.btn_start_workout);
        startButton.setOnClickListener(TimeTrialSetupDialogFragment.this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_start_workout:
                if (!validateInput()) {
                    Toast.makeText(getActivity(), "Please populate all fields", Toast.LENGTH_SHORT).show();
                } else {
                    startWorkout();
                }
                break;
        }
    }

    private void startWorkout() {
        int targetSets = Integer.parseInt(mTargetSetsEdit.getText().toString());
        int targetReps = Integer.parseInt(mTargetRepsEdit.getText().toString());
        int targetMinsPerSet = Integer.parseInt((String) mMinutesSpinner.getSelectedItem());
        int targetSecsPerSet = Integer.parseInt((String) mSecondsSpinner.getSelectedItem());

        dismiss();
        TimeTrialActivity.start(getActivity(), targetSets, targetReps, targetMinsPerSet,
                targetSecsPerSet, mBluetoothDevice);
    }

    private boolean validateInput() {
        return mTargetRepsEdit.getText().length() > 0
                && mTargetSetsEdit.getText().length() > 0
                && (mMinutesSpinner.getSelectedItemPosition() > 0
                || mSecondsSpinner.getSelectedItemPosition() > 0);
    }

    private static String[] getSpinnerValues() {
        String[] spinnerValues = new String[60];
        for (int i = 0; i < 60; i++) {
            spinnerValues[i] = String.valueOf(i);
        }

        return spinnerValues;
    }
}
