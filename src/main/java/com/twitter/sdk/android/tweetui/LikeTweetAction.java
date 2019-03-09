package com.twitter.sdk.android.tweetui;

import android.view.View;
import android.view.View.OnClickListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;

class LikeTweetAction extends BaseTweetAction implements OnClickListener {
    final Tweet tweet;
    final TweetRepository tweetRepository;
    final TweetScribeClient tweetScribeClient;
    final TweetUi tweetUi;

    static class LikeCallback extends Callback<Tweet> {
        final ToggleImageButton button;
        final Callback<Tweet> cb;
        final Tweet tweet;

        LikeCallback(ToggleImageButton toggleImageButton, Tweet tweet, Callback<Tweet> callback) {
            this.button = toggleImageButton;
            this.tweet = tweet;
            this.cb = callback;
        }

        public void success(Result<Tweet> result) {
            this.cb.success(result);
        }

        public void failure(TwitterException twitterException) {
            if (twitterException instanceof TwitterApiException) {
                int errorCode = ((TwitterApiException) twitterException).getErrorCode();
                if (errorCode == Errors.ALREADY_FAVORITED) {
                    this.cb.success(new Result(new TweetBuilder().copy(this.tweet).setFavorited(true).build(), null));
                    return;
                } else if (errorCode != Errors.ALREADY_UNFAVORITED) {
                    this.button.setToggledOn(this.tweet.favorited);
                    this.cb.failure(twitterException);
                    return;
                } else {
                    this.cb.success(new Result(new TweetBuilder().copy(this.tweet).setFavorited(false).build(), null));
                    return;
                }
            }
            this.button.setToggledOn(this.tweet.favorited);
            this.cb.failure(twitterException);
        }
    }

    LikeTweetAction(Tweet tweet, TweetUi tweetUi, Callback<Tweet> callback) {
        this(tweet, tweetUi, callback, new TweetScribeClientImpl(tweetUi));
    }

    LikeTweetAction(Tweet tweet, TweetUi tweetUi, Callback<Tweet> callback, TweetScribeClient tweetScribeClient) {
        super(callback);
        this.tweet = tweet;
        this.tweetUi = tweetUi;
        this.tweetScribeClient = tweetScribeClient;
        this.tweetRepository = tweetUi.getTweetRepository();
    }

    public void onClick(View view) {
        if (view instanceof ToggleImageButton) {
            ToggleImageButton toggleImageButton = (ToggleImageButton) view;
            if (this.tweet.favorited) {
                scribeUnFavoriteAction();
                this.tweetRepository.unfavorite(this.tweet.id, new LikeCallback(toggleImageButton, this.tweet, getActionCallback()));
                return;
            }
            scribeFavoriteAction();
            this.tweetRepository.favorite(this.tweet.id, new LikeCallback(toggleImageButton, this.tweet, getActionCallback()));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeFavoriteAction() {
        this.tweetScribeClient.favorite(this.tweet);
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeUnFavoriteAction() {
        this.tweetScribeClient.unfavorite(this.tweet);
    }
}
