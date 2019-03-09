package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.Tweet;

public interface TweetLinkClickListener {
    void onLinkClick(Tweet tweet, String str);
}
