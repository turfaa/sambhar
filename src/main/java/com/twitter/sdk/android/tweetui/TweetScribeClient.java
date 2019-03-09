package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.Tweet;

public interface TweetScribeClient {
    void click(Tweet tweet, String str);

    void favorite(Tweet tweet);

    void impression(Tweet tweet, String str, boolean z);

    void share(Tweet tweet);

    void unfavorite(Tweet tweet);
}
