package com.facebook.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.facebook.FacebookActivity;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.FetchedAppSettings.DialogFeatureConfig;
import com.facebook.internal.NativeProtocol.ProtocolVersionQueryResult;

public class DialogPresenter {

    public interface ParameterProvider {
        Bundle getLegacyParameters();

        Bundle getParameters();
    }

    public static void setupAppCallForCannotShowError(AppCall appCall) {
        setupAppCallForValidationError(appCall, new FacebookException("Unable to show the provided content via the web or the installed version of the Facebook app. Some dialogs are only supported starting API 14."));
    }

    public static void setupAppCallForValidationError(AppCall appCall, FacebookException facebookException) {
        setupAppCallForErrorResult(appCall, facebookException);
    }

    public static void present(AppCall appCall, Activity activity) {
        activity.startActivityForResult(appCall.getRequestIntent(), appCall.getRequestCode());
        appCall.setPending();
    }

    public static void present(AppCall appCall, FragmentWrapper fragmentWrapper) {
        fragmentWrapper.startActivityForResult(appCall.getRequestIntent(), appCall.getRequestCode());
        appCall.setPending();
    }

    public static boolean canPresentNativeDialogWithFeature(DialogFeature dialogFeature) {
        return getProtocolVersionForNativeDialog(dialogFeature).getProtocolVersion() != -1;
    }

    public static boolean canPresentWebFallbackDialogWithFeature(DialogFeature dialogFeature) {
        return getDialogWebFallbackUri(dialogFeature) != null;
    }

    public static void setupAppCallForErrorResult(AppCall appCall, FacebookException facebookException) {
        if (facebookException != null) {
            Validate.hasFacebookActivity(FacebookSdk.getApplicationContext());
            Intent intent = new Intent();
            intent.setClass(FacebookSdk.getApplicationContext(), FacebookActivity.class);
            intent.setAction(FacebookActivity.PASS_THROUGH_CANCEL_ACTION);
            NativeProtocol.setupProtocolRequestIntent(intent, appCall.getCallId().toString(), null, NativeProtocol.getLatestKnownVersion(), NativeProtocol.createBundleForException(facebookException));
            appCall.setRequestIntent(intent);
        }
    }

    public static void setupAppCallForWebDialog(AppCall appCall, String str, Bundle bundle) {
        Validate.hasFacebookActivity(FacebookSdk.getApplicationContext());
        Validate.hasInternetPermissions(FacebookSdk.getApplicationContext());
        Bundle bundle2 = new Bundle();
        bundle2.putString(NativeProtocol.WEB_DIALOG_ACTION, str);
        bundle2.putBundle(NativeProtocol.WEB_DIALOG_PARAMS, bundle);
        Intent intent = new Intent();
        NativeProtocol.setupProtocolRequestIntent(intent, appCall.getCallId().toString(), str, NativeProtocol.getLatestKnownVersion(), bundle2);
        intent.setClass(FacebookSdk.getApplicationContext(), FacebookActivity.class);
        intent.setAction(FacebookDialogFragment.TAG);
        appCall.setRequestIntent(intent);
    }

    public static void setupAppCallForWebFallbackDialog(AppCall appCall, Bundle bundle, DialogFeature dialogFeature) {
        Validate.hasFacebookActivity(FacebookSdk.getApplicationContext());
        Validate.hasInternetPermissions(FacebookSdk.getApplicationContext());
        String name = dialogFeature.name();
        Uri dialogWebFallbackUri = getDialogWebFallbackUri(dialogFeature);
        if (dialogWebFallbackUri != null) {
            bundle = ServerProtocol.getQueryParamsForPlatformActivityIntentWebFallback(appCall.getCallId().toString(), NativeProtocol.getLatestKnownVersion(), bundle);
            if (bundle != null) {
                Uri buildUri;
                if (dialogWebFallbackUri.isRelative()) {
                    buildUri = Utility.buildUri(ServerProtocol.getDialogAuthority(), dialogWebFallbackUri.toString(), bundle);
                } else {
                    buildUri = Utility.buildUri(dialogWebFallbackUri.getAuthority(), dialogWebFallbackUri.getPath(), bundle);
                }
                Bundle bundle2 = new Bundle();
                bundle2.putString("url", buildUri.toString());
                bundle2.putBoolean(NativeProtocol.WEB_DIALOG_IS_FALLBACK, true);
                Intent intent = new Intent();
                NativeProtocol.setupProtocolRequestIntent(intent, appCall.getCallId().toString(), dialogFeature.getAction(), NativeProtocol.getLatestKnownVersion(), bundle2);
                intent.setClass(FacebookSdk.getApplicationContext(), FacebookActivity.class);
                intent.setAction(FacebookDialogFragment.TAG);
                appCall.setRequestIntent(intent);
                return;
            }
            throw new FacebookException("Unable to fetch the app's key-hash");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unable to fetch the Url for the DialogFeature : '");
        stringBuilder.append(name);
        stringBuilder.append("'");
        throw new FacebookException(stringBuilder.toString());
    }

    public static void setupAppCallForNativeDialog(AppCall appCall, ParameterProvider parameterProvider, DialogFeature dialogFeature) {
        Context applicationContext = FacebookSdk.getApplicationContext();
        String action = dialogFeature.getAction();
        ProtocolVersionQueryResult protocolVersionForNativeDialog = getProtocolVersionForNativeDialog(dialogFeature);
        int protocolVersion = protocolVersionForNativeDialog.getProtocolVersion();
        if (protocolVersion != -1) {
            Bundle parameters;
            if (NativeProtocol.isVersionCompatibleWithBucketedIntent(protocolVersion)) {
                parameters = parameterProvider.getParameters();
            } else {
                parameters = parameterProvider.getLegacyParameters();
            }
            if (parameters == null) {
                parameters = new Bundle();
            }
            Intent createPlatformActivityIntent = NativeProtocol.createPlatformActivityIntent(applicationContext, appCall.getCallId().toString(), action, protocolVersionForNativeDialog, parameters);
            if (createPlatformActivityIntent != null) {
                appCall.setRequestIntent(createPlatformActivityIntent);
                return;
            }
            throw new FacebookException("Unable to create Intent; this likely means theFacebook app is not installed.");
        }
        throw new FacebookException("Cannot present this dialog. This likely means that the Facebook app is not installed.");
    }

    private static Uri getDialogWebFallbackUri(DialogFeature dialogFeature) {
        DialogFeatureConfig dialogFeatureConfig = FetchedAppSettings.getDialogFeatureConfig(FacebookSdk.getApplicationId(), dialogFeature.getAction(), dialogFeature.name());
        return dialogFeatureConfig != null ? dialogFeatureConfig.getFallbackUrl() : null;
    }

    public static ProtocolVersionQueryResult getProtocolVersionForNativeDialog(DialogFeature dialogFeature) {
        String applicationId = FacebookSdk.getApplicationId();
        String action = dialogFeature.getAction();
        return NativeProtocol.getLatestAvailableProtocolVersionForAction(action, getVersionSpecForFeature(applicationId, action, dialogFeature));
    }

    private static int[] getVersionSpecForFeature(String str, String str2, DialogFeature dialogFeature) {
        DialogFeatureConfig dialogFeatureConfig = FetchedAppSettings.getDialogFeatureConfig(str, str2, dialogFeature.name());
        if (dialogFeatureConfig != null) {
            return dialogFeatureConfig.getVersionSpec();
        }
        return new int[]{dialogFeature.getMinVersion()};
    }

    public static void logDialogActivity(Context context, String str, String str2) {
        AppEventsLogger newLogger = AppEventsLogger.newLogger(context);
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsEvents.PARAMETER_DIALOG_OUTCOME, str2);
        newLogger.logSdkEvent(str, null, bundle);
    }
}
