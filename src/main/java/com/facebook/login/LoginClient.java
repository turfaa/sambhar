package com.facebook.login;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.common.R;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

class LoginClient implements Parcelable {
    public static final Creator<LoginClient> CREATOR = new Creator<LoginClient>() {
        public LoginClient createFromParcel(Parcel parcel) {
            return new LoginClient(parcel);
        }

        public LoginClient[] newArray(int i) {
            return new LoginClient[i];
        }
    };
    BackgroundProcessingListener backgroundProcessingListener;
    boolean checkedInternetPermission;
    int currentHandler = -1;
    Map<String, String> extraData;
    Fragment fragment;
    LoginMethodHandler[] handlersToTry;
    Map<String, String> loggingExtras;
    private LoginLogger loginLogger;
    OnCompletedListener onCompletedListener;
    Request pendingRequest;

    interface BackgroundProcessingListener {
        void onBackgroundProcessingStarted();

        void onBackgroundProcessingStopped();
    }

    public interface OnCompletedListener {
        void onCompleted(Result result);
    }

    public static class Request implements Parcelable {
        public static final Creator<Request> CREATOR = new Creator<Request>() {
            public Request createFromParcel(Parcel parcel) {
                return new Request(parcel, null);
            }

            public Request[] newArray(int i) {
                return new Request[i];
            }
        };
        private final String applicationId;
        private final String authId;
        private String authType;
        private final DefaultAudience defaultAudience;
        private String deviceAuthTargetUserId;
        private String deviceRedirectUriString;
        private boolean isRerequest;
        private final LoginBehavior loginBehavior;
        private Set<String> permissions;

        public int describeContents() {
            return 0;
        }

        /* synthetic */ Request(Parcel parcel, AnonymousClass1 anonymousClass1) {
            this(parcel);
        }

        Request(LoginBehavior loginBehavior, Set<String> set, DefaultAudience defaultAudience, String str, String str2, String str3) {
            Set set2;
            this.isRerequest = false;
            this.loginBehavior = loginBehavior;
            if (set2 == null) {
                set2 = new HashSet();
            }
            this.permissions = set2;
            this.defaultAudience = defaultAudience;
            this.authType = str;
            this.applicationId = str2;
            this.authId = str3;
        }

        /* Access modifiers changed, original: 0000 */
        public Set<String> getPermissions() {
            return this.permissions;
        }

        /* Access modifiers changed, original: 0000 */
        public void setPermissions(Set<String> set) {
            Validate.notNull(set, NativeProtocol.RESULT_ARGS_PERMISSIONS);
            this.permissions = set;
        }

        /* Access modifiers changed, original: 0000 */
        public LoginBehavior getLoginBehavior() {
            return this.loginBehavior;
        }

        /* Access modifiers changed, original: 0000 */
        public DefaultAudience getDefaultAudience() {
            return this.defaultAudience;
        }

        /* Access modifiers changed, original: 0000 */
        public String getApplicationId() {
            return this.applicationId;
        }

        /* Access modifiers changed, original: 0000 */
        public String getAuthId() {
            return this.authId;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isRerequest() {
            return this.isRerequest;
        }

        /* Access modifiers changed, original: 0000 */
        public void setRerequest(boolean z) {
            this.isRerequest = z;
        }

        /* Access modifiers changed, original: 0000 */
        public String getDeviceRedirectUriString() {
            return this.deviceRedirectUriString;
        }

        /* Access modifiers changed, original: 0000 */
        public void setDeviceRedirectUriString(String str) {
            this.deviceRedirectUriString = str;
        }

        /* Access modifiers changed, original: 0000 */
        public String getDeviceAuthTargetUserId() {
            return this.deviceAuthTargetUserId;
        }

        /* Access modifiers changed, original: 0000 */
        public void setDeviceAuthTargetUserId(String str) {
            this.deviceAuthTargetUserId = str;
        }

        /* Access modifiers changed, original: 0000 */
        public String getAuthType() {
            return this.authType;
        }

        /* Access modifiers changed, original: 0000 */
        public void setAuthType(String str) {
            this.authType = str;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean hasPublishPermission() {
            for (String isPublishPermission : this.permissions) {
                if (LoginManager.isPublishPermission(isPublishPermission)) {
                    return true;
                }
            }
            return false;
        }

        private Request(Parcel parcel) {
            boolean z = false;
            this.isRerequest = false;
            String readString = parcel.readString();
            DefaultAudience defaultAudience = null;
            this.loginBehavior = readString != null ? LoginBehavior.valueOf(readString) : null;
            ArrayList arrayList = new ArrayList();
            parcel.readStringList(arrayList);
            this.permissions = new HashSet(arrayList);
            readString = parcel.readString();
            if (readString != null) {
                defaultAudience = DefaultAudience.valueOf(readString);
            }
            this.defaultAudience = defaultAudience;
            this.applicationId = parcel.readString();
            this.authId = parcel.readString();
            if (parcel.readByte() != (byte) 0) {
                z = true;
            }
            this.isRerequest = z;
            this.deviceRedirectUriString = parcel.readString();
            this.authType = parcel.readString();
            this.deviceAuthTargetUserId = parcel.readString();
        }

        public void writeToParcel(Parcel parcel, int i) {
            String str = null;
            parcel.writeString(this.loginBehavior != null ? this.loginBehavior.name() : null);
            parcel.writeStringList(new ArrayList(this.permissions));
            if (this.defaultAudience != null) {
                str = this.defaultAudience.name();
            }
            parcel.writeString(str);
            parcel.writeString(this.applicationId);
            parcel.writeString(this.authId);
            parcel.writeByte((byte) this.isRerequest);
            parcel.writeString(this.deviceRedirectUriString);
            parcel.writeString(this.authType);
            parcel.writeString(this.deviceAuthTargetUserId);
        }
    }

    public static class Result implements Parcelable {
        public static final Creator<Result> CREATOR = new Creator<Result>() {
            public Result createFromParcel(Parcel parcel) {
                return new Result(parcel, null);
            }

            public Result[] newArray(int i) {
                return new Result[i];
            }
        };
        final Code code;
        final String errorCode;
        final String errorMessage;
        public Map<String, String> extraData;
        public Map<String, String> loggingExtras;
        final Request request;
        final AccessToken token;

        enum Code {
            SUCCESS(GraphResponse.SUCCESS_KEY),
            CANCEL("cancel"),
            ERROR("error");
            
            private final String loggingValue;

            private Code(String str) {
                this.loggingValue = str;
            }

            /* Access modifiers changed, original: 0000 */
            public String getLoggingValue() {
                return this.loggingValue;
            }
        }

        public int describeContents() {
            return 0;
        }

        /* synthetic */ Result(Parcel parcel, AnonymousClass1 anonymousClass1) {
            this(parcel);
        }

        Result(Request request, Code code, AccessToken accessToken, String str, String str2) {
            Validate.notNull(code, "code");
            this.request = request;
            this.token = accessToken;
            this.errorMessage = str;
            this.code = code;
            this.errorCode = str2;
        }

        static Result createTokenResult(Request request, AccessToken accessToken) {
            return new Result(request, Code.SUCCESS, accessToken, null, null);
        }

        static Result createCancelResult(Request request, String str) {
            return new Result(request, Code.CANCEL, null, str, null);
        }

        static Result createErrorResult(Request request, String str, String str2) {
            return createErrorResult(request, str, str2, null);
        }

        static Result createErrorResult(Request request, String str, String str2, String str3) {
            return new Result(request, Code.ERROR, null, TextUtils.join(": ", Utility.asListNoNulls(str, str2)), str3);
        }

        private Result(Parcel parcel) {
            this.code = Code.valueOf(parcel.readString());
            this.token = (AccessToken) parcel.readParcelable(AccessToken.class.getClassLoader());
            this.errorMessage = parcel.readString();
            this.errorCode = parcel.readString();
            this.request = (Request) parcel.readParcelable(Request.class.getClassLoader());
            this.loggingExtras = Utility.readStringMapFromParcel(parcel);
            this.extraData = Utility.readStringMapFromParcel(parcel);
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.code.name());
            parcel.writeParcelable(this.token, i);
            parcel.writeString(this.errorMessage);
            parcel.writeString(this.errorCode);
            parcel.writeParcelable(this.request, i);
            Utility.writeStringMapToParcel(parcel, this.loggingExtras);
            Utility.writeStringMapToParcel(parcel, this.extraData);
        }
    }

    public int describeContents() {
        return 0;
    }

    public LoginClient(Fragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    /* Access modifiers changed, original: 0000 */
    public void setFragment(Fragment fragment) {
        if (this.fragment == null) {
            this.fragment = fragment;
            return;
        }
        throw new FacebookException("Can't set fragment once it is already set.");
    }

    /* Access modifiers changed, original: 0000 */
    public FragmentActivity getActivity() {
        return this.fragment.getActivity();
    }

    public Request getPendingRequest() {
        return this.pendingRequest;
    }

    public static int getLoginRequestCode() {
        return RequestCodeOffset.Login.toRequestCode();
    }

    /* Access modifiers changed, original: 0000 */
    public void startOrContinueAuth(Request request) {
        if (!getInProgress()) {
            authorize(request);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void authorize(Request request) {
        if (request != null) {
            if (this.pendingRequest != null) {
                throw new FacebookException("Attempted to authorize while a request is pending.");
            } else if (!AccessToken.isCurrentAccessTokenActive() || checkInternetPermission()) {
                this.pendingRequest = request;
                this.handlersToTry = getHandlersToTry(request);
                tryNextHandler();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean getInProgress() {
        return this.pendingRequest != null && this.currentHandler >= 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void cancelCurrentHandler() {
        if (this.currentHandler >= 0) {
            getCurrentHandler().cancel();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public LoginMethodHandler getCurrentHandler() {
        return this.currentHandler >= 0 ? this.handlersToTry[this.currentHandler] : null;
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        return this.pendingRequest != null ? getCurrentHandler().onActivityResult(i, i2, intent) : false;
    }

    /* Access modifiers changed, original: protected */
    public LoginMethodHandler[] getHandlersToTry(Request request) {
        ArrayList arrayList = new ArrayList();
        LoginBehavior loginBehavior = request.getLoginBehavior();
        if (loginBehavior.allowsGetTokenAuth()) {
            arrayList.add(new GetTokenLoginMethodHandler(this));
        }
        if (loginBehavior.allowsKatanaAuth()) {
            arrayList.add(new KatanaProxyLoginMethodHandler(this));
        }
        if (loginBehavior.allowsFacebookLiteAuth()) {
            arrayList.add(new FacebookLiteLoginMethodHandler(this));
        }
        if (loginBehavior.allowsCustomTabAuth()) {
            arrayList.add(new CustomTabLoginMethodHandler(this));
        }
        if (loginBehavior.allowsWebViewAuth()) {
            arrayList.add(new WebViewLoginMethodHandler(this));
        }
        if (loginBehavior.allowsDeviceAuth()) {
            arrayList.add(new DeviceAuthMethodHandler(this));
        }
        LoginMethodHandler[] loginMethodHandlerArr = new LoginMethodHandler[arrayList.size()];
        arrayList.toArray(loginMethodHandlerArr);
        return loginMethodHandlerArr;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean checkInternetPermission() {
        if (this.checkedInternetPermission) {
            return true;
        }
        if (checkPermission("android.permission.INTERNET") != 0) {
            FragmentActivity activity = getActivity();
            complete(Result.createErrorResult(this.pendingRequest, activity.getString(R.string.com_facebook_internet_permission_error_title), activity.getString(R.string.com_facebook_internet_permission_error_message)));
            return false;
        }
        this.checkedInternetPermission = true;
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void tryNextHandler() {
        if (this.currentHandler >= 0) {
            logAuthorizationMethodComplete(getCurrentHandler().getNameForLogging(), "skipped", null, null, getCurrentHandler().methodLoggingExtras);
        }
        while (this.handlersToTry != null && this.currentHandler < this.handlersToTry.length - 1) {
            this.currentHandler++;
            if (tryCurrentHandler()) {
                return;
            }
        }
        if (this.pendingRequest != null) {
            completeWithFailure();
        }
    }

    private void completeWithFailure() {
        complete(Result.createErrorResult(this.pendingRequest, "Login attempt failed.", null));
    }

    private void addLoggingExtra(String str, String str2, boolean z) {
        Object str22;
        if (this.loggingExtras == null) {
            this.loggingExtras = new HashMap();
        }
        if (this.loggingExtras.containsKey(str) && z) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append((String) this.loggingExtras.get(str));
            stringBuilder.append(",");
            stringBuilder.append(str22);
            str22 = stringBuilder.toString();
        }
        this.loggingExtras.put(str, str22);
    }

    /* Access modifiers changed, original: 0000 */
    public void addExtraData(String str, String str2, boolean z) {
        Object str22;
        if (this.extraData == null) {
            this.extraData = new HashMap();
        }
        if (this.extraData.containsKey(str) && z) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append((String) this.extraData.get(str));
            stringBuilder.append(",");
            stringBuilder.append(str22);
            str22 = stringBuilder.toString();
        }
        this.extraData.put(str, str22);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean tryCurrentHandler() {
        LoginMethodHandler currentHandler = getCurrentHandler();
        if (!currentHandler.needsInternetPermission() || checkInternetPermission()) {
            boolean tryAuthorize = currentHandler.tryAuthorize(this.pendingRequest);
            if (tryAuthorize) {
                getLogger().logAuthorizationMethodStart(this.pendingRequest.getAuthId(), currentHandler.getNameForLogging());
            } else {
                getLogger().logAuthorizationMethodNotTried(this.pendingRequest.getAuthId(), currentHandler.getNameForLogging());
                addLoggingExtra("not_tried", currentHandler.getNameForLogging(), true);
            }
            return tryAuthorize;
        }
        addLoggingExtra("no_internet_permission", AppEventsConstants.EVENT_PARAM_VALUE_YES, false);
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void completeAndValidate(Result result) {
        if (result.token == null || !AccessToken.isCurrentAccessTokenActive()) {
            complete(result);
        } else {
            validateSameFbidAndFinish(result);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void complete(Result result) {
        LoginMethodHandler currentHandler = getCurrentHandler();
        if (currentHandler != null) {
            logAuthorizationMethodComplete(currentHandler.getNameForLogging(), result, currentHandler.methodLoggingExtras);
        }
        if (this.loggingExtras != null) {
            result.loggingExtras = this.loggingExtras;
        }
        if (this.extraData != null) {
            result.extraData = this.extraData;
        }
        this.handlersToTry = null;
        this.currentHandler = -1;
        this.pendingRequest = null;
        this.loggingExtras = null;
        notifyOnCompleteListener(result);
    }

    /* Access modifiers changed, original: 0000 */
    public OnCompletedListener getOnCompletedListener() {
        return this.onCompletedListener;
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnCompletedListener(OnCompletedListener onCompletedListener) {
        this.onCompletedListener = onCompletedListener;
    }

    /* Access modifiers changed, original: 0000 */
    public BackgroundProcessingListener getBackgroundProcessingListener() {
        return this.backgroundProcessingListener;
    }

    /* Access modifiers changed, original: 0000 */
    public void setBackgroundProcessingListener(BackgroundProcessingListener backgroundProcessingListener) {
        this.backgroundProcessingListener = backgroundProcessingListener;
    }

    /* Access modifiers changed, original: 0000 */
    public int checkPermission(String str) {
        return getActivity().checkCallingOrSelfPermission(str);
    }

    /* Access modifiers changed, original: 0000 */
    public void validateSameFbidAndFinish(Result result) {
        if (result.token != null) {
            AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
            AccessToken accessToken = result.token;
            if (!(currentAccessToken == null || accessToken == null)) {
                try {
                    if (currentAccessToken.getUserId().equals(accessToken.getUserId())) {
                        result = Result.createTokenResult(this.pendingRequest, result.token);
                        complete(result);
                        return;
                    }
                } catch (Exception e) {
                    complete(Result.createErrorResult(this.pendingRequest, "Caught exception", e.getMessage()));
                    return;
                }
            }
            result = Result.createErrorResult(this.pendingRequest, "User logged in as different Facebook user.", null);
            complete(result);
            return;
        }
        throw new FacebookException("Can't validate without a token");
    }

    private LoginLogger getLogger() {
        if (this.loginLogger == null || !this.loginLogger.getApplicationId().equals(this.pendingRequest.getApplicationId())) {
            this.loginLogger = new LoginLogger(getActivity(), this.pendingRequest.getApplicationId());
        }
        return this.loginLogger;
    }

    private void notifyOnCompleteListener(Result result) {
        if (this.onCompletedListener != null) {
            this.onCompletedListener.onCompleted(result);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void notifyBackgroundProcessingStart() {
        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStarted();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void notifyBackgroundProcessingStop() {
        if (this.backgroundProcessingListener != null) {
            this.backgroundProcessingListener.onBackgroundProcessingStopped();
        }
    }

    private void logAuthorizationMethodComplete(String str, Result result, Map<String, String> map) {
        logAuthorizationMethodComplete(str, result.code.getLoggingValue(), result.errorMessage, result.errorCode, map);
    }

    private void logAuthorizationMethodComplete(String str, String str2, String str3, String str4, Map<String, String> map) {
        if (this.pendingRequest == null) {
            getLogger().logUnexpectedError("fb_mobile_login_method_complete", "Unexpected call to logCompleteLogin with null pendingAuthorizationRequest.", str);
        } else {
            getLogger().logAuthorizationMethodComplete(this.pendingRequest.getAuthId(), str, str2, str3, str4, map);
        }
    }

    static String getE2E() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("init", System.currentTimeMillis());
        } catch (JSONException unused) {
        }
        return jSONObject.toString();
    }

    public LoginClient(Parcel parcel) {
        Parcelable[] readParcelableArray = parcel.readParcelableArray(LoginMethodHandler.class.getClassLoader());
        this.handlersToTry = new LoginMethodHandler[readParcelableArray.length];
        for (int i = 0; i < readParcelableArray.length; i++) {
            this.handlersToTry[i] = (LoginMethodHandler) readParcelableArray[i];
            this.handlersToTry[i].setLoginClient(this);
        }
        this.currentHandler = parcel.readInt();
        this.pendingRequest = (Request) parcel.readParcelable(Request.class.getClassLoader());
        this.loggingExtras = Utility.readStringMapFromParcel(parcel);
        this.extraData = Utility.readStringMapFromParcel(parcel);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelableArray(this.handlersToTry, i);
        parcel.writeInt(this.currentHandler);
        parcel.writeParcelable(this.pendingRequest, i);
        Utility.writeStringMapToParcel(parcel, this.loggingExtras);
        Utility.writeStringMapToParcel(parcel, this.extraData);
    }
}
