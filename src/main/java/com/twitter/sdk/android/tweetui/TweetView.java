package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.util.AttributeSet;
import com.twitter.sdk.android.core.models.Tweet;

public class TweetView extends BaseTweetView {
    private static final double DEFAULT_ASPECT_RATIO_MEDIA_CONTAINER = 1.5d;
    private static final double SQUARE_ASPECT_RATIO = 1.0d;
    private static final String VIEW_TYPE_NAME = "default";

    /* Access modifiers changed, original: protected */
    public double getAspectRatioForPhotoEntity(int i) {
        return i == 4 ? SQUARE_ASPECT_RATIO : DEFAULT_ASPECT_RATIO_MEDIA_CONTAINER;
    }

    /* Access modifiers changed, original: 0000 */
    public String getViewTypeName() {
        return VIEW_TYPE_NAME;
    }

    public TweetView(Context context, Tweet tweet) {
        super(context, tweet);
    }

    public TweetView(Context context, Tweet tweet, int i) {
        super(context, tweet, i);
    }

    TweetView(Context context, Tweet tweet, int i, DependencyProvider dependencyProvider) {
        super(context, tweet, i, dependencyProvider);
    }

    public TweetView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TweetView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* Access modifiers changed, original: protected */
    public int getLayout() {
        return R.layout.tw__tweet;
    }

    /* Access modifiers changed, original: 0000 */
    public void render() {
        super.render();
        setVerifiedCheck(this.tweet);
    }

    private void setVerifiedCheck(Tweet tweet) {
        if (tweet == null || tweet.user == null || !tweet.user.verified) {
            this.fullNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            this.fullNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tw__ic_tweet_verified, 0);
        }
    }
}
