package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.internal.scribe.ScribeItem;

public interface GalleryScribeClient {
    void dismiss();

    void impression(ScribeItem scribeItem);

    void navigate();

    void show();
}
