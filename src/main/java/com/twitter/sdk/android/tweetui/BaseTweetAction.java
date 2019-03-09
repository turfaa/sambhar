package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Tweet;

class BaseTweetAction {
    protected final Callback<Tweet> actionCallback;

    BaseTweetAction(Callback<Tweet> callback) {
        this.actionCallback = callback;
    }

    /* Access modifiers changed, original: 0000 */
    public Callback<Tweet> getActionCallback() {
        return this.actionCallback;
    }
}
