package com.twitter.sdk.android.core;

import okhttp3.Headers;

public class TwitterRateLimit {
    private static final String LIMIT_KEY = "x-rate-limit-limit";
    private static final String REMAINING_KEY = "x-rate-limit-remaining";
    private static final String RESET_KEY = "x-rate-limit-reset";
    private int remainingRequest;
    private int requestLimit;
    private long resetSeconds;

    TwitterRateLimit(Headers headers) {
        if (headers != null) {
            for (int i = 0; i < headers.size(); i++) {
                if (LIMIT_KEY.equals(headers.name(i))) {
                    this.requestLimit = Integer.valueOf(headers.value(i)).intValue();
                } else if (REMAINING_KEY.equals(headers.name(i))) {
                    this.remainingRequest = Integer.valueOf(headers.value(i)).intValue();
                } else if (RESET_KEY.equals(headers.name(i))) {
                    this.resetSeconds = Long.valueOf(headers.value(i)).longValue();
                }
            }
            return;
        }
        throw new IllegalArgumentException("headers must not be null");
    }

    public int getLimit() {
        return this.requestLimit;
    }

    public int getRemaining() {
        return this.remainingRequest;
    }

    public long getReset() {
        return this.resetSeconds;
    }
}
