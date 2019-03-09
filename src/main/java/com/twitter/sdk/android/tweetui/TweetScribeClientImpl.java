package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace.Builder;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.internal.scribe.SyndicationClientEvent;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.ArrayList;

class TweetScribeClientImpl implements TweetScribeClient {
    static final String SCRIBE_ACTIONS_ELEMENT = "actions";
    static final String SCRIBE_CLICK_ACTION = "click";
    static final String SCRIBE_FAVORITE_ACTION = "favorite";
    static final String SCRIBE_IMPRESSION_ACTION = "impression";
    static final String SCRIBE_SHARE_ACTION = "share";
    static final String SCRIBE_UNFAVORITE_ACTION = "unfavorite";
    static final String SYNDICATED_SDK_IMPRESSION_COMPONENT = "";
    static final String SYNDICATED_SDK_IMPRESSION_ELEMENT = "";
    static final String SYNDICATED_SDK_IMPRESSION_PAGE = "tweet";
    static final String TFW_CLIENT_EVENT_ELEMENT = "";
    static final String TFW_CLIENT_EVENT_PAGE = "android";
    static final String TFW_CLIENT_EVENT_SECTION = "tweet";
    final TweetUi tweetUi;

    TweetScribeClientImpl(TweetUi tweetUi) {
        this.tweetUi = tweetUi;
    }

    public void impression(Tweet tweet, String str, boolean z) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ScribeItem.fromTweet(tweet));
        this.tweetUi.scribe(getTfwImpressionNamespace(str, z), arrayList);
        this.tweetUi.scribe(getSyndicatedImpressionNamespace(str), arrayList);
    }

    public void share(Tweet tweet) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ScribeItem.fromTweet(tweet));
        this.tweetUi.scribe(getTfwShareNamespace(), arrayList);
    }

    public void favorite(Tweet tweet) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ScribeItem.fromTweet(tweet));
        this.tweetUi.scribe(getTfwFavoriteNamespace(), arrayList);
    }

    public void unfavorite(Tweet tweet) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ScribeItem.fromTweet(tweet));
        this.tweetUi.scribe(getTfwUnfavoriteNamespace(), arrayList);
    }

    public void click(Tweet tweet, String str) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ScribeItem.fromTweet(tweet));
        this.tweetUi.scribe(getTfwClickNamespace(str), arrayList);
    }

    static EventNamespace getTfwImpressionNamespace(String str, boolean z) {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("tweet").setComponent(str).setElement(z ? SCRIBE_ACTIONS_ELEMENT : "").setAction(SCRIBE_IMPRESSION_ACTION).builder();
    }

    static EventNamespace getTfwUnfavoriteNamespace() {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("tweet").setElement(SCRIBE_ACTIONS_ELEMENT).setAction(SCRIBE_UNFAVORITE_ACTION).builder();
    }

    static EventNamespace getTfwFavoriteNamespace() {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("tweet").setElement(SCRIBE_ACTIONS_ELEMENT).setAction(SCRIBE_FAVORITE_ACTION).builder();
    }

    static EventNamespace getTfwShareNamespace() {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("tweet").setElement(SCRIBE_ACTIONS_ELEMENT).setAction("share").builder();
    }

    static EventNamespace getTfwClickNamespace(String str) {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("tweet").setComponent(str).setElement("").setAction(SCRIBE_CLICK_ACTION).builder();
    }

    static EventNamespace getSyndicatedImpressionNamespace(String str) {
        return new Builder().setClient("android").setPage("tweet").setSection(str).setComponent("").setElement("").setAction(SCRIBE_IMPRESSION_ACTION).builder();
    }
}
