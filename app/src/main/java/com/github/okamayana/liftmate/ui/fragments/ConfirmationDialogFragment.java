package com.github.okamayana.liftmate.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;

public class ConfirmationDialogFragment extends AppCompatDialogFragment
        implements OnClickListener {

    private ConfirmationDialogFragmentListener mListener;

    private String mDialogTitle;
    private String mDialogText;

    private String mConfirmButtonText;
    private String mCancelButtonText;

    private boolean mHasCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int layoutResId = mHasCancel ? R.layout.dialog_confirmation_with_cancel
                : R.layout.dialog_confirmation_without_cancel;
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setCanceledOnTouchOutside(false);

        TextView dialogTitleView = (TextView) view.findViewById(R.id.dialog_title);
        dialogTitleView.setText(mDialogTitle);

        TextView dialogTextView = (TextView) view.findViewById(R.id.dialog_text);
        dialogTextView.setText(mDialogText);

        Button confirmButton = (Button) view.findViewById(R.id.btn_confirm);
        confirmButton.setOnClickListener(ConfirmationDialogFragment.this);
        if (!TextUtils.isEmpty(mConfirmButtonText)) {
            confirmButton.setText(mConfirmButtonText);
        }

        if (mHasCancel) {
            Button cancelButton = (Button) view.findViewById(R.id.btn_cancel);
            cancelButton.setOnClickListener(ConfirmationDialogFragment.this);
            if (!TextUtils.isEmpty(mCancelButtonText)) {
                cancelButton.setText(mCancelButtonText);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_confirm:
                if (mListener != null) {
                    mListener.onConfirm(ConfirmationDialogFragment.this);
                    dismiss();
                }
                break;

            case R.id.btn_cancel:
                if (mListener != null) {
                    mListener.onCancel(ConfirmationDialogFragment.this);
                    dismiss();
                }
                break;
        }
    }

    public String getDialogTitle() {
        return mDialogTitle;
    }

    public void setHasCancel(boolean hasCancel) {
        mHasCancel = hasCancel;
    }

    public void setOnConfirmationDialogFragmentListener(
            ConfirmationDialogFragmentListener listener) {
        mListener = listener;
    }

    public void setDialogTitle(String title) {
        mDialogTitle = title;
    }

    public void setDialogText(String text) {
        mDialogText = text;
    }

    public void setConfirmButtonText(String confirmButtonText) {
        mConfirmButtonText = confirmButtonText;
    }

    public void setCancelButtonText(String cancelButtonText) {
        mCancelButtonText = cancelButtonText;
    }

    public interface ConfirmationDialogFragmentListener {

        void onConfirm(ConfirmationDialogFragment dialog);

        void onCancel(ConfirmationDialogFragment dialog);
    }
}
