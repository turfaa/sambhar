package com.twitter.sdk.android.tweetui;

import android.content.Context;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;

public class QuoteTweetView extends AbstractTweetView {
    private static final double DEFAULT_ASPECT_RATIO_MEDIA_CONTAINER = 1.6d;
    private static final double MAX_LANDSCAPE_ASPECT_RATIO = 3.0d;
    private static final double MIN_LANDSCAPE_ASPECT_RATIO = 1.3333333333333333d;
    private static final double SQUARE_ASPECT_RATIO = 1.0d;
    private static final String VIEW_TYPE_NAME = "quote";

    /* Access modifiers changed, original: protected */
    public double getAspectRatioForPhotoEntity(int i) {
        return DEFAULT_ASPECT_RATIO_MEDIA_CONTAINER;
    }

    /* Access modifiers changed, original: 0000 */
    public String getViewTypeName() {
        return "quote";
    }

    public /* bridge */ /* synthetic */ Tweet getTweet() {
        return super.getTweet();
    }

    public /* bridge */ /* synthetic */ long getTweetId() {
        return super.getTweetId();
    }

    public /* bridge */ /* synthetic */ void setTweet(Tweet tweet) {
        super.setTweet(tweet);
    }

    public /* bridge */ /* synthetic */ void setTweetLinkClickListener(TweetLinkClickListener tweetLinkClickListener) {
        super.setTweetLinkClickListener(tweetLinkClickListener);
    }

    public /* bridge */ /* synthetic */ void setTweetMediaClickListener(TweetMediaClickListener tweetMediaClickListener) {
        super.setTweetMediaClickListener(tweetMediaClickListener);
    }

    public QuoteTweetView(Context context) {
        this(context, new DependencyProvider());
    }

    QuoteTweetView(Context context, DependencyProvider dependencyProvider) {
        super(context, null, 0, dependencyProvider);
    }

    public void setStyle(int i, int i2, int i3, int i4, int i5, int i6) {
        this.primaryTextColor = i;
        this.secondaryTextColor = i2;
        this.actionColor = i3;
        this.actionHighlightColor = i4;
        this.mediaBgColor = i5;
        this.photoErrorResId = i6;
        applyStyles();
    }

    /* Access modifiers changed, original: protected */
    public int getLayout() {
        return R.layout.tw__tweet_quote;
    }

    /* Access modifiers changed, original: 0000 */
    public void render() {
        super.render();
        this.screenNameView.requestLayout();
    }

    /* Access modifiers changed, original: protected */
    public void applyStyles() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.tw__media_view_radius);
        this.tweetMediaView.setRoundedCornersRadii(0, 0, dimensionPixelSize, dimensionPixelSize);
        setBackgroundResource(R.drawable.tw__quote_tweet_border);
        this.fullNameView.setTextColor(this.primaryTextColor);
        this.screenNameView.setTextColor(this.secondaryTextColor);
        this.contentView.setTextColor(this.primaryTextColor);
        this.tweetMediaView.setMediaBgColor(this.mediaBgColor);
        this.tweetMediaView.setPhotoErrorResId(this.photoErrorResId);
    }

    /* Access modifiers changed, original: protected */
    public double getAspectRatio(MediaEntity mediaEntity) {
        double aspectRatio = super.getAspectRatio(mediaEntity);
        if (aspectRatio <= SQUARE_ASPECT_RATIO) {
            return SQUARE_ASPECT_RATIO;
        }
        if (aspectRatio > MAX_LANDSCAPE_ASPECT_RATIO) {
            return MAX_LANDSCAPE_ASPECT_RATIO;
        }
        return aspectRatio < MIN_LANDSCAPE_ASPECT_RATIO ? MIN_LANDSCAPE_ASPECT_RATIO : aspectRatio;
    }
}
