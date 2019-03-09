package com.twitter.sdk.android.tweetcomposer;

import com.twitter.sdk.android.core.internal.scribe.DefaultScribeClient;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import java.util.List;

class ScribeClientImpl implements ScribeClient {
    private final DefaultScribeClient scribeClient;

    public ScribeClientImpl(DefaultScribeClient defaultScribeClient) {
        this.scribeClient = defaultScribeClient;
    }

    public void scribe(EventNamespace eventNamespace, List<ScribeItem> list) {
        if (this.scribeClient != null) {
            this.scribeClient.scribe(eventNamespace, (List) list);
        }
    }
}
