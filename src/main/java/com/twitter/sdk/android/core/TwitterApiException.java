package com.twitter.sdk.android.core;

import android.text.TextUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.twitter.sdk.android.core.models.ApiError;
import com.twitter.sdk.android.core.models.ApiErrors;
import com.twitter.sdk.android.core.models.SafeListAdapter;
import com.twitter.sdk.android.core.models.SafeMapAdapter;
import retrofit2.Response;

public class TwitterApiException extends TwitterException {
    public static final int DEFAULT_ERROR_CODE = 0;
    private final ApiError apiError;
    private final int code;
    private final Response response;
    private final TwitterRateLimit twitterRateLimit;

    public TwitterApiException(Response response) {
        this(response, readApiError(response), readApiRateLimit(response), response.code());
    }

    TwitterApiException(Response response, ApiError apiError, TwitterRateLimit twitterRateLimit, int i) {
        super(createExceptionMessage(i));
        this.apiError = apiError;
        this.twitterRateLimit = twitterRateLimit;
        this.code = i;
        this.response = response;
    }

    public int getStatusCode() {
        return this.code;
    }

    public int getErrorCode() {
        return this.apiError == null ? 0 : this.apiError.code;
    }

    public String getErrorMessage() {
        return this.apiError == null ? null : this.apiError.message;
    }

    public TwitterRateLimit getTwitterRateLimit() {
        return this.twitterRateLimit;
    }

    public Response getResponse() {
        return this.response;
    }

    public static TwitterRateLimit readApiRateLimit(Response response) {
        return new TwitterRateLimit(response.headers());
    }

    public static ApiError readApiError(Response response) {
        try {
            String readUtf8 = response.errorBody().source().buffer().clone().readUtf8();
            if (!TextUtils.isEmpty(readUtf8)) {
                return parseApiError(readUtf8);
            }
        } catch (Exception e) {
            Twitter.getLogger().e("Twitter", "Unexpected response", e);
        }
        return null;
    }

    static ApiError parseApiError(String str) {
        try {
            ApiErrors apiErrors = (ApiErrors) new GsonBuilder().registerTypeAdapterFactory(new SafeListAdapter()).registerTypeAdapterFactory(new SafeMapAdapter()).create().fromJson(str, ApiErrors.class);
            if (!apiErrors.errors.isEmpty()) {
                return (ApiError) apiErrors.errors.get(0);
            }
        } catch (JsonSyntaxException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid json: ");
            stringBuilder.append(str);
            Twitter.getLogger().e("Twitter", stringBuilder.toString(), e);
        }
        return null;
    }

    static String createExceptionMessage(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HTTP request failed, Status: ");
        stringBuilder.append(i);
        return stringBuilder.toString();
    }
}
