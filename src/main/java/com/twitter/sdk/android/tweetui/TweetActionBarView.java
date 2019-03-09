package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Tweet;

public class TweetActionBarView extends LinearLayout {
    Callback<Tweet> actionCallback;
    final DependencyProvider dependencyProvider;
    ToggleImageButton likeButton;
    ImageButton shareButton;

    static class DependencyProvider {
        DependencyProvider() {
        }

        /* Access modifiers changed, original: 0000 */
        public TweetUi getTweetUi() {
            return TweetUi.getInstance();
        }
    }

    public TweetActionBarView(Context context) {
        this(context, null, new DependencyProvider());
    }

    public TweetActionBarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, new DependencyProvider());
    }

    TweetActionBarView(Context context, AttributeSet attributeSet, DependencyProvider dependencyProvider) {
        super(context, attributeSet);
        this.dependencyProvider = dependencyProvider;
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        findSubviews();
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnActionCallback(Callback<Tweet> callback) {
        this.actionCallback = callback;
    }

    /* Access modifiers changed, original: 0000 */
    public void findSubviews() {
        this.likeButton = (ToggleImageButton) findViewById(R.id.tw__tweet_like_button);
        this.shareButton = (ImageButton) findViewById(R.id.tw__tweet_share_button);
    }

    /* Access modifiers changed, original: 0000 */
    public void setTweet(Tweet tweet) {
        setLike(tweet);
        setShare(tweet);
    }

    /* Access modifiers changed, original: 0000 */
    public void setLike(Tweet tweet) {
        TweetUi tweetUi = this.dependencyProvider.getTweetUi();
        if (tweet != null) {
            this.likeButton.setToggledOn(tweet.favorited);
            this.likeButton.setOnClickListener(new LikeTweetAction(tweet, tweetUi, this.actionCallback));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setShare(Tweet tweet) {
        TweetUi tweetUi = this.dependencyProvider.getTweetUi();
        if (tweet != null) {
            this.shareButton.setOnClickListener(new ShareTweetAction(tweet, tweetUi));
        }
    }
}
