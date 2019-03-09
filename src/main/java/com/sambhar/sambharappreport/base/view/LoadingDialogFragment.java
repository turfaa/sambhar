package com.sambhar.sambharappreport.base.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

public class LoadingDialogFragment extends AppCompatDialogFragment {
    private CharSequence mMessage;
    private ProgressDialog mProgressDialog;

    public static LoadingDialogFragment create(CharSequence charSequence) {
        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.mMessage = charSequence;
        return loadingDialogFragment;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setMessage(this.mMessage);
        setCancelable(false);
        return this.mProgressDialog;
    }
}
