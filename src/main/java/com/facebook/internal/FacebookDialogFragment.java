package com.facebook.internal;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.WebDialog.Builder;
import com.facebook.internal.WebDialog.OnCompleteListener;

public class FacebookDialogFragment extends DialogFragment {
    public static final String TAG = "FacebookDialogFragment";
    private Dialog dialog;

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.dialog == null) {
            Dialog newInstance;
            FragmentActivity activity = getActivity();
            Bundle methodArgumentsFromIntent = NativeProtocol.getMethodArgumentsFromIntent(activity.getIntent());
            if (methodArgumentsFromIntent.getBoolean(NativeProtocol.WEB_DIALOG_IS_FALLBACK, false)) {
                String string = methodArgumentsFromIntent.getString("url");
                if (Utility.isNullOrEmpty(string)) {
                    Utility.logd(TAG, "Cannot start a fallback WebDialog with an empty/missing 'url'");
                    activity.finish();
                    return;
                }
                newInstance = FacebookWebFallbackDialog.newInstance(activity, string, String.format("fb%s://bridge/", new Object[]{FacebookSdk.getApplicationId()}));
                newInstance.setOnCompleteListener(new OnCompleteListener() {
                    public void onComplete(Bundle bundle, FacebookException facebookException) {
                        FacebookDialogFragment.this.onCompleteWebFallbackDialog(bundle);
                    }
                });
            } else {
                String string2 = methodArgumentsFromIntent.getString(NativeProtocol.WEB_DIALOG_ACTION);
                methodArgumentsFromIntent = methodArgumentsFromIntent.getBundle(NativeProtocol.WEB_DIALOG_PARAMS);
                if (Utility.isNullOrEmpty(string2)) {
                    Utility.logd(TAG, "Cannot start a WebDialog with an empty/missing 'actionName'");
                    activity.finish();
                    return;
                }
                newInstance = new Builder(activity, string2, methodArgumentsFromIntent).setOnCompleteListener(new OnCompleteListener() {
                    public void onComplete(Bundle bundle, FacebookException facebookException) {
                        FacebookDialogFragment.this.onCompleteWebDialog(bundle, facebookException);
                    }
                }).build();
            }
            this.dialog = newInstance;
        }
    }

    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        if (this.dialog == null) {
            onCompleteWebDialog(null, null);
            setShowsDialog(false);
        }
        return this.dialog;
    }

    public void onResume() {
        super.onResume();
        if (this.dialog instanceof WebDialog) {
            ((WebDialog) this.dialog).resize();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if ((this.dialog instanceof WebDialog) && isResumed()) {
            ((WebDialog) this.dialog).resize();
        }
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    private void onCompleteWebDialog(Bundle bundle, FacebookException facebookException) {
        FragmentActivity activity = getActivity();
        activity.setResult(facebookException == null ? -1 : 0, NativeProtocol.createProtocolResultIntent(activity.getIntent(), bundle, facebookException));
        activity.finish();
    }

    private void onCompleteWebFallbackDialog(Bundle bundle) {
        FragmentActivity activity = getActivity();
        Intent intent = new Intent();
        if (bundle == null) {
            bundle = new Bundle();
        }
        intent.putExtras(bundle);
        activity.setResult(-1, intent);
        activity.finish();
    }
}
