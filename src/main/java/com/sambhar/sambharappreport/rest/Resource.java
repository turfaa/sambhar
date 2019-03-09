package com.sambhar.sambharappreport.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;

public class Resource<T> {
    private static final int NETWORK_LOADING = 0;
    public final T data;
    public final String message;
    public final int networkCode;
    @NonNull
    public final Status status;
    public final int statusCode;

    public Resource(Status status, T t, String str, int i, int i2) {
        this.status = status;
        this.data = t;
        this.message = str;
        this.networkCode = i;
        this.statusCode = i2;
    }

    public static <T> Resource<T> success(@Nullable T t, int i) {
        return new Resource(Status.SUCCESS, t, null, Callback.DEFAULT_DRAG_ANIMATION_DURATION, i);
    }

    public static <T> Resource<T> error(@Nullable int i, String str) {
        return new Resource(Status.ERROR, null, str, i, 0);
    }

    public static <T> Resource<T> loading(@Nullable T t) {
        return new Resource(Status.LOADING, t, null, 0, -1);
    }
}
