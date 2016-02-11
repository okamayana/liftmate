package com.github.okamayana.liftmate.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.activities.FreeStyleActivity;
import com.github.okamayana.liftmate.ui.activities.TimeTrialActivity;

public class TimeTrialSetupDialogFragment extends AppCompatDialogFragment {

    public static TimeTrialSetupDialogFragment newInstance() {
        return new TimeTrialSetupDialogFragment();
    }

    private EditText mTargetSetsEdit;
    private EditText mTargetRepsEdit;
    private EditText mTargetTimeEdit;

    private Button mStartButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mTargetTimeEdit = (EditText) view.findViewById(R.id.target_time_input);
        mStartButton = (Button) view.findViewById(R.id.btn_start_workout);

        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    dismiss();
                    TimeTrialActivity.start(getActivity());
                }
            }
        });
    }

    private boolean validateInput() {
        return mTargetSetsEdit.length() > 0 && mTargetRepsEdit.length() > 0
                && mTargetTimeEdit.length() > 0;
    }
}
