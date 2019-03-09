package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;

public interface TweetMediaClickListener {
    void onMediaEntityClick(Tweet tweet, MediaEntity mediaEntity);
}
