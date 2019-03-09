package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.internal.scribe.ScribeItem;

public interface VideoScribeClient {
    void impression(ScribeItem scribeItem);

    void play(ScribeItem scribeItem);
}
