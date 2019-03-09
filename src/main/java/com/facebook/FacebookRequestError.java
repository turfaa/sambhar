package com.facebook;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.facebook.internal.FacebookRequestErrorClassification;
import com.facebook.internal.FetchedAppSettings;
import com.facebook.internal.FetchedAppSettingsManager;
import com.facebook.internal.Utility;
import java.net.HttpURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

public final class FacebookRequestError implements Parcelable {
    private static final String BODY_KEY = "body";
    private static final String CODE_KEY = "code";
    public static final Creator<FacebookRequestError> CREATOR = new Creator<FacebookRequestError>() {
        public FacebookRequestError createFromParcel(Parcel parcel) {
            return new FacebookRequestError(parcel, null);
        }

        public FacebookRequestError[] newArray(int i) {
            return new FacebookRequestError[i];
        }
    };
    private static final String ERROR_CODE_FIELD_KEY = "code";
    private static final String ERROR_CODE_KEY = "error_code";
    private static final String ERROR_IS_TRANSIENT_KEY = "is_transient";
    private static final String ERROR_KEY = "error";
    private static final String ERROR_MESSAGE_FIELD_KEY = "message";
    private static final String ERROR_MSG_KEY = "error_msg";
    private static final String ERROR_REASON_KEY = "error_reason";
    private static final String ERROR_SUB_CODE_KEY = "error_subcode";
    private static final String ERROR_TYPE_FIELD_KEY = "type";
    private static final String ERROR_USER_MSG_KEY = "error_user_msg";
    private static final String ERROR_USER_TITLE_KEY = "error_user_title";
    static final Range HTTP_RANGE_SUCCESS = new Range(Callback.DEFAULT_DRAG_ANIMATION_DURATION, 299, null);
    public static final int INVALID_ERROR_CODE = -1;
    public static final int INVALID_HTTP_STATUS_CODE = -1;
    private final Object batchRequestResult;
    private final Category category;
    private final HttpURLConnection connection;
    private final int errorCode;
    private final String errorMessage;
    private final String errorRecoveryMessage;
    private final String errorType;
    private final String errorUserMessage;
    private final String errorUserTitle;
    private final FacebookException exception;
    private final JSONObject requestResult;
    private final JSONObject requestResultBody;
    private final int requestStatusCode;
    private final int subErrorCode;

    public enum Category {
        LOGIN_RECOVERABLE,
        OTHER,
        TRANSIENT
    }

    private static class Range {
        private final int end;
        private final int start;

        /* synthetic */ Range(int i, int i2, AnonymousClass1 anonymousClass1) {
            this(i, i2);
        }

        private Range(int i, int i2) {
            this.start = i;
            this.end = i2;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean contains(int i) {
            return this.start <= i && i <= this.end;
        }
    }

    public int describeContents() {
        return 0;
    }

    private FacebookRequestError(int i, int i2, int i3, String str, String str2, String str3, String str4, boolean z, JSONObject jSONObject, JSONObject jSONObject2, Object obj, HttpURLConnection httpURLConnection, FacebookException facebookException) {
        Object obj2;
        Category category;
        this.requestStatusCode = i;
        this.errorCode = i2;
        this.subErrorCode = i3;
        this.errorType = str;
        this.errorMessage = str2;
        this.requestResultBody = jSONObject;
        this.requestResult = jSONObject2;
        this.batchRequestResult = obj;
        this.connection = httpURLConnection;
        this.errorUserTitle = str3;
        this.errorUserMessage = str4;
        if (facebookException != null) {
            this.exception = facebookException;
            obj2 = 1;
        } else {
            this.exception = new FacebookServiceException(this, str2);
            obj2 = null;
        }
        FacebookRequestErrorClassification errorClassification = getErrorClassification();
        if (obj2 != null) {
            category = Category.OTHER;
        } else {
            category = errorClassification.classify(i2, i3, z);
        }
        this.category = category;
        this.errorRecoveryMessage = errorClassification.getRecoveryMessage(this.category);
    }

    FacebookRequestError(HttpURLConnection httpURLConnection, Exception exception) {
        Throwable th = exception;
        this(-1, -1, -1, null, null, null, null, false, null, null, null, httpURLConnection, th instanceof FacebookException ? (FacebookException) th : new FacebookException(th));
    }

    public FacebookRequestError(int i, String str, String str2) {
        this(-1, i, -1, str, str2, null, null, false, null, null, null, null, null);
    }

    public Category getCategory() {
        return this.category;
    }

    public int getRequestStatusCode() {
        return this.requestStatusCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public int getSubErrorCode() {
        return this.subErrorCode;
    }

    public String getErrorType() {
        return this.errorType;
    }

    public String getErrorMessage() {
        if (this.errorMessage != null) {
            return this.errorMessage;
        }
        return this.exception.getLocalizedMessage();
    }

    public String getErrorRecoveryMessage() {
        return this.errorRecoveryMessage;
    }

    public String getErrorUserMessage() {
        return this.errorUserMessage;
    }

    public String getErrorUserTitle() {
        return this.errorUserTitle;
    }

    public JSONObject getRequestResultBody() {
        return this.requestResultBody;
    }

    public JSONObject getRequestResult() {
        return this.requestResult;
    }

    public Object getBatchRequestResult() {
        return this.batchRequestResult;
    }

    public HttpURLConnection getConnection() {
        return this.connection;
    }

    public FacebookException getException() {
        return this.exception;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{HttpStatus: ");
        stringBuilder.append(this.requestStatusCode);
        stringBuilder.append(", errorCode: ");
        stringBuilder.append(this.errorCode);
        stringBuilder.append(", subErrorCode: ");
        stringBuilder.append(this.subErrorCode);
        stringBuilder.append(", errorType: ");
        stringBuilder.append(this.errorType);
        stringBuilder.append(", errorMessage: ");
        stringBuilder.append(getErrorMessage());
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static FacebookRequestError checkResponseAndCreateError(JSONObject jSONObject, Object obj, HttpURLConnection httpURLConnection) {
        JSONObject jSONObject2 = jSONObject;
        try {
            if (jSONObject2.has("code")) {
                int i = jSONObject2.getInt("code");
                Object stringPropertyAsJSON = Utility.getStringPropertyAsJSON(jSONObject2, BODY_KEY, GraphResponse.NON_JSON_RESPONSE_PROPERTY);
                if (stringPropertyAsJSON != null && (stringPropertyAsJSON instanceof JSONObject)) {
                    String optString;
                    String optString2;
                    String optString3;
                    int i2;
                    String str;
                    boolean optBoolean;
                    JSONObject jSONObject3 = (JSONObject) stringPropertyAsJSON;
                    Object obj2 = 1;
                    int i3 = -1;
                    if (jSONObject3.has("error")) {
                        JSONObject jSONObject4 = (JSONObject) Utility.getStringPropertyAsJSON(jSONObject3, "error", null);
                        optString = jSONObject4.optString("type", null);
                        optString2 = jSONObject4.optString("message", null);
                        int optInt = jSONObject4.optInt("code", -1);
                        i3 = jSONObject4.optInt("error_subcode", -1);
                        String optString4 = jSONObject4.optString(ERROR_USER_MSG_KEY, null);
                        optString3 = jSONObject4.optString(ERROR_USER_TITLE_KEY, null);
                        i2 = i3;
                        i3 = optInt;
                        str = optString4;
                        optBoolean = jSONObject4.optBoolean(ERROR_IS_TRANSIENT_KEY, false);
                    } else {
                        if (!(jSONObject3.has("error_code") || jSONObject3.has(ERROR_MSG_KEY))) {
                            if (!jSONObject3.has(ERROR_REASON_KEY)) {
                                optString = null;
                                optString2 = optString;
                                str = optString2;
                                optString3 = str;
                                obj2 = null;
                                i2 = -1;
                                optBoolean = false;
                            }
                        }
                        String optString5 = jSONObject3.optString(ERROR_REASON_KEY, null);
                        optString = jSONObject3.optString(ERROR_MSG_KEY, null);
                        int optInt2 = jSONObject3.optInt("error_code", -1);
                        i2 = jSONObject3.optInt("error_subcode", -1);
                        i3 = optInt2;
                        str = null;
                        optString3 = str;
                        optBoolean = false;
                        optString2 = optString;
                        optString = optString5;
                    }
                    if (obj2 != null) {
                        return new FacebookRequestError(i, i3, i2, optString, optString2, optString3, str, optBoolean, jSONObject3, jSONObject, obj, httpURLConnection, null);
                    }
                }
                if (!HTTP_RANGE_SUCCESS.contains(i)) {
                    return new FacebookRequestError(i, -1, -1, null, null, null, null, false, jSONObject2.has(BODY_KEY) ? (JSONObject) Utility.getStringPropertyAsJSON(jSONObject2, BODY_KEY, GraphResponse.NON_JSON_RESPONSE_PROPERTY) : null, jSONObject, obj, httpURLConnection, null);
                }
            }
        } catch (JSONException unused) {
        }
        return null;
    }

    static synchronized FacebookRequestErrorClassification getErrorClassification() {
        synchronized (FacebookRequestError.class) {
            FetchedAppSettings appSettingsWithoutQuery = FetchedAppSettingsManager.getAppSettingsWithoutQuery(FacebookSdk.getApplicationId());
            FacebookRequestErrorClassification defaultErrorClassification;
            if (appSettingsWithoutQuery == null) {
                defaultErrorClassification = FacebookRequestErrorClassification.getDefaultErrorClassification();
                return defaultErrorClassification;
            }
            defaultErrorClassification = appSettingsWithoutQuery.getErrorClassification();
            return defaultErrorClassification;
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.requestStatusCode);
        parcel.writeInt(this.errorCode);
        parcel.writeInt(this.subErrorCode);
        parcel.writeString(this.errorType);
        parcel.writeString(this.errorMessage);
        parcel.writeString(this.errorUserTitle);
        parcel.writeString(this.errorUserMessage);
    }

    private FacebookRequestError(Parcel parcel) {
        this(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), false, null, null, null, null, null);
    }
}
