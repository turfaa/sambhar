package com.twitter.sdk.android.tweetcomposer;

import java.util.Collections;

class ComposerScribeClientImpl implements ComposerScribeClient {
    private final ScribeClient scribeClient;

    ComposerScribeClientImpl(ScribeClient scribeClient) {
        if (scribeClient != null) {
            this.scribeClient = scribeClient;
            return;
        }
        throw new NullPointerException("scribeClient must not be null");
    }

    public void impression() {
        this.scribeClient.scribe(ScribeConstants.ComposerEventBuilder.setComponent("").setElement("").setAction("impression").builder(), Collections.EMPTY_LIST);
    }

    public void click(String str) {
        this.scribeClient.scribe(ScribeConstants.ComposerEventBuilder.setComponent("").setElement(str).setAction("click").builder(), Collections.EMPTY_LIST);
    }
}
