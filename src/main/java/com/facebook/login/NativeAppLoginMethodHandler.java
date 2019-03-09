package com.facebook.login;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookException;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.login.LoginClient.Request;
import com.facebook.login.LoginClient.Result;

abstract class NativeAppLoginMethodHandler extends LoginMethodHandler {
    public abstract boolean tryAuthorize(Request request);

    NativeAppLoginMethodHandler(LoginClient loginClient) {
        super(loginClient);
    }

    NativeAppLoginMethodHandler(Parcel parcel) {
        super(parcel);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean onActivityResult(int i, int i2, Intent intent) {
        Result createCancelResult;
        Request pendingRequest = this.loginClient.getPendingRequest();
        if (intent == null) {
            createCancelResult = Result.createCancelResult(pendingRequest, "Operation canceled");
        } else if (i2 == 0) {
            createCancelResult = handleResultCancel(pendingRequest, intent);
        } else if (i2 != -1) {
            createCancelResult = Result.createErrorResult(pendingRequest, "Unexpected resultCode from authorization.", null);
        } else {
            createCancelResult = handleResultOk(pendingRequest, intent);
        }
        if (createCancelResult != null) {
            this.loginClient.completeAndValidate(createCancelResult);
        } else {
            this.loginClient.tryNextHandler();
        }
        return true;
    }

    private Result handleResultOk(Request request, Intent intent) {
        Bundle extras = intent.getExtras();
        String error = getError(extras);
        String obj = extras.get(NativeProtocol.BRIDGE_ARG_ERROR_CODE) != null ? extras.get(NativeProtocol.BRIDGE_ARG_ERROR_CODE).toString() : null;
        String errorMessage = getErrorMessage(extras);
        String string = extras.getString("e2e");
        if (!Utility.isNullOrEmpty(string)) {
            logWebLoginCompleted(string);
        }
        if (error == null && obj == null && errorMessage == null) {
            try {
                return Result.createTokenResult(request, LoginMethodHandler.createAccessTokenFromWebBundle(request.getPermissions(), extras, AccessTokenSource.FACEBOOK_APPLICATION_WEB, request.getApplicationId()));
            } catch (FacebookException e) {
                return Result.createErrorResult(request, null, e.getMessage());
            }
        } else if (ServerProtocol.errorsProxyAuthDisabled.contains(error)) {
            return null;
        } else {
            if (ServerProtocol.errorsUserCanceled.contains(error)) {
                return Result.createCancelResult(request, null);
            }
            return Result.createErrorResult(request, error, errorMessage, obj);
        }
    }

    private Result handleResultCancel(Request request, Intent intent) {
        Bundle extras = intent.getExtras();
        String error = getError(extras);
        Object obj = extras.get(NativeProtocol.BRIDGE_ARG_ERROR_CODE) != null ? extras.get(NativeProtocol.BRIDGE_ARG_ERROR_CODE).toString() : null;
        if (ServerProtocol.errorConnectionFailure.equals(obj)) {
            return Result.createErrorResult(request, error, getErrorMessage(extras), obj);
        }
        return Result.createCancelResult(request, error);
    }

    private String getError(Bundle bundle) {
        String string = bundle.getString("error");
        return string == null ? bundle.getString(NativeProtocol.BRIDGE_ARG_ERROR_TYPE) : string;
    }

    private String getErrorMessage(Bundle bundle) {
        String string = bundle.getString(AnalyticsEvents.PARAMETER_SHARE_ERROR_MESSAGE);
        return string == null ? bundle.getString(NativeProtocol.BRIDGE_ARG_ERROR_DESCRIPTION) : string;
    }

    /* Access modifiers changed, original: protected */
    public boolean tryIntent(Intent intent, int i) {
        if (intent == null) {
            return false;
        }
        try {
            this.loginClient.getFragment().startActivityForResult(intent, i);
            return true;
        } catch (ActivityNotFoundException unused) {
            return false;
        }
    }
}
