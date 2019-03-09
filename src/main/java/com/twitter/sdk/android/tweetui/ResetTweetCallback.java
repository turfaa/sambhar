package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

class ResetTweetCallback extends Callback<Tweet> {
    final BaseTweetView baseTweetView;
    final Callback<Tweet> cb;
    final TweetRepository tweetRepository;

    ResetTweetCallback(BaseTweetView baseTweetView, TweetRepository tweetRepository, Callback<Tweet> callback) {
        this.baseTweetView = baseTweetView;
        this.tweetRepository = tweetRepository;
        this.cb = callback;
    }

    public void success(Result<Tweet> result) {
        this.tweetRepository.updateCache((Tweet) result.data);
        this.baseTweetView.setTweet((Tweet) result.data);
        if (this.cb != null) {
            this.cb.success(result);
        }
    }

    public void failure(TwitterException twitterException) {
        if (this.cb != null) {
            this.cb.failure(twitterException);
        }
    }
}
