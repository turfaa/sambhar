package com.sambhar.sambharappreport.rest;

import android.support.annotation.Nullable;
import javax.annotation.Nonnull;

public class ApiResponse<T> {
    @Nullable
    private T mBody;
    @Nonnull
    private String mErrormessage;
    private int mNetworkCode;
    @Nullable
    private boolean mResponse;
    private int statusCode;

    public T getBody() {
        return this.mBody;
    }

    public void setBody(@Nullable T t) {
        this.mBody = t;
    }

    public String getErrorMessage() {
        return this.mErrormessage;
    }

    public void setErrorMessage(String str) {
        this.mErrormessage = str;
    }

    public boolean isSuccessful() {
        return this.mResponse;
    }

    public void setSuccessful(boolean z) {
        this.mResponse = z;
    }

    public int getNetworkCode() {
        return this.mNetworkCode;
    }

    public void setNetworkCode(int i) {
        this.mNetworkCode = i;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int i) {
        this.statusCode = i;
    }
}
