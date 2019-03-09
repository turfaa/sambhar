package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Logger;
import com.twitter.sdk.android.core.TwitterException;

abstract class LoggingCallback<T> extends Callback<T> {
    private final Callback cb;
    private final Logger logger;

    LoggingCallback(Callback callback, Logger logger) {
        this.cb = callback;
        this.logger = logger;
    }

    public void failure(TwitterException twitterException) {
        this.logger.e("TweetUi", twitterException.getMessage(), twitterException);
        if (this.cb != null) {
            this.cb.failure(twitterException);
        }
    }
}
