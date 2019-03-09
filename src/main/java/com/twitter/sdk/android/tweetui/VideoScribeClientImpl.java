package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace.Builder;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.internal.scribe.SyndicationClientEvent;
import java.util.ArrayList;

class VideoScribeClientImpl implements VideoScribeClient {
    static final String SCRIBE_IMPRESSION_ACTION = "impression";
    static final String SCRIBE_PLAY_ACTION = "play";
    static final String TFW_CLIENT_EVENT_PAGE = "android";
    static final String TFW_CLIENT_EVENT_SECTION = "video";
    final TweetUi tweetUi;

    VideoScribeClientImpl(TweetUi tweetUi) {
        this.tweetUi = tweetUi;
    }

    public void impression(ScribeItem scribeItem) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(scribeItem);
        this.tweetUi.scribe(getTfwImpressionNamespace(), arrayList);
    }

    public void play(ScribeItem scribeItem) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(scribeItem);
        this.tweetUi.scribe(getTfwPlayNamespace(), arrayList);
    }

    static EventNamespace getTfwImpressionNamespace() {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("video").setAction(SCRIBE_IMPRESSION_ACTION).builder();
    }

    static EventNamespace getTfwPlayNamespace() {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("video").setAction(SCRIBE_PLAY_ACTION).builder();
    }
}
