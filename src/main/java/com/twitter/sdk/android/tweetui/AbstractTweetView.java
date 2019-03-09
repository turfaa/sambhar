package com.twitter.sdk.android.tweetui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.IntentUtils;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.internal.UserUtils;
import com.twitter.sdk.android.core.internal.VineCardUtils;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.internal.AspectRatioFrameLayout;
import com.twitter.sdk.android.tweetui.internal.MediaBadgeView;
import com.twitter.sdk.android.tweetui.internal.SpanClickHandler;
import com.twitter.sdk.android.tweetui.internal.TweetMediaUtils;
import com.twitter.sdk.android.tweetui.internal.TweetMediaView;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

abstract class AbstractTweetView extends RelativeLayout {
    static final double DEFAULT_ASPECT_RATIO = 1.7777777777777777d;
    static final int DEFAULT_STYLE = R.style.tw__TweetLightStyle;
    static final String EMPTY_STRING = "";
    static final long INVALID_ID = -1;
    static final double MEDIA_BG_DARK_OPACITY = 0.12d;
    static final double MEDIA_BG_LIGHT_OPACITY = 0.08d;
    static final double SECONDARY_TEXT_COLOR_DARK_OPACITY = 0.35d;
    static final double SECONDARY_TEXT_COLOR_LIGHT_OPACITY = 0.4d;
    static final String TAG = "TweetUi";
    int actionColor;
    int actionHighlightColor;
    TextView contentView;
    final DependencyProvider dependencyProvider;
    TextView fullNameView;
    private LinkClickListener linkClickListener;
    MediaBadgeView mediaBadgeView;
    int mediaBgColor;
    AspectRatioFrameLayout mediaContainer;
    private Uri permalinkUri;
    int photoErrorResId;
    int primaryTextColor;
    TextView screenNameView;
    int secondaryTextColor;
    int styleResId;
    Tweet tweet;
    boolean tweetActionsEnabled;
    TweetLinkClickListener tweetLinkClickListener;
    TweetMediaClickListener tweetMediaClickListener;
    TweetMediaView tweetMediaView;

    static class DependencyProvider {
        TweetScribeClient tweetScribeClient;
        VideoScribeClient videoScribeClient;

        DependencyProvider() {
        }

        /* Access modifiers changed, original: 0000 */
        public TweetUi getTweetUi() {
            return TweetUi.getInstance();
        }

        /* Access modifiers changed, original: 0000 */
        public TweetScribeClient getTweetScribeClient() {
            if (this.tweetScribeClient == null) {
                this.tweetScribeClient = new TweetScribeClientImpl(getTweetUi());
            }
            return this.tweetScribeClient;
        }

        /* Access modifiers changed, original: 0000 */
        public VideoScribeClient getVideoScribeClient() {
            if (this.videoScribeClient == null) {
                this.videoScribeClient = new VideoScribeClientImpl(getTweetUi());
            }
            return this.videoScribeClient;
        }

        /* Access modifiers changed, original: 0000 */
        public Picasso getImageLoader() {
            return TweetUi.getInstance().getImageLoader();
        }
    }

    class PermalinkClickListener implements OnClickListener {
        PermalinkClickListener() {
        }

        public void onClick(View view) {
            if (AbstractTweetView.this.getPermalinkUri() != null) {
                AbstractTweetView.this.scribePermalinkClick();
                AbstractTweetView.this.launchPermalink();
            }
        }
    }

    public abstract double getAspectRatioForPhotoEntity(int i);

    public abstract int getLayout();

    public abstract String getViewTypeName();

    AbstractTweetView(Context context, AttributeSet attributeSet, int i, DependencyProvider dependencyProvider) {
        super(context, attributeSet, i);
        this.dependencyProvider = dependencyProvider;
        inflateView(context);
        findSubviews();
    }

    private void inflateView(Context context) {
        LayoutInflater.from(context).inflate(getLayout(), this, true);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isTweetUiEnabled() {
        if (isInEditMode()) {
            return false;
        }
        try {
            this.dependencyProvider.getTweetUi();
            return true;
        } catch (IllegalStateException e) {
            Twitter.getLogger().e(TAG, e.getMessage());
            setEnabled(false);
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void findSubviews() {
        this.fullNameView = (TextView) findViewById(R.id.tw__tweet_author_full_name);
        this.screenNameView = (TextView) findViewById(R.id.tw__tweet_author_screen_name);
        this.mediaContainer = (AspectRatioFrameLayout) findViewById(R.id.tw__aspect_ratio_media_container);
        this.tweetMediaView = (TweetMediaView) findViewById(R.id.tweet_media_view);
        this.contentView = (TextView) findViewById(R.id.tw__tweet_text);
        this.mediaBadgeView = (MediaBadgeView) findViewById(R.id.tw__tweet_media_badge);
    }

    public long getTweetId() {
        if (this.tweet == null) {
            return -1;
        }
        return this.tweet.id;
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
        render();
    }

    public Tweet getTweet() {
        return this.tweet;
    }

    public void setTweetMediaClickListener(TweetMediaClickListener tweetMediaClickListener) {
        this.tweetMediaClickListener = tweetMediaClickListener;
        this.tweetMediaView.setTweetMediaClickListener(tweetMediaClickListener);
    }

    public void setTweetLinkClickListener(TweetLinkClickListener tweetLinkClickListener) {
        this.tweetLinkClickListener = tweetLinkClickListener;
    }

    /* Access modifiers changed, original: 0000 */
    public void render() {
        Tweet displayTweet = TweetUtils.getDisplayTweet(this.tweet);
        setName(displayTweet);
        setScreenName(displayTweet);
        setTweetMedia(displayTweet);
        setText(displayTweet);
        setContentDescription(displayTweet);
        if (TweetUtils.isTweetResolvable(this.tweet)) {
            setPermalinkUri(this.tweet.user.screenName, Long.valueOf(getTweetId()));
        } else {
            this.permalinkUri = null;
        }
        setPermalinkLauncher();
        scribeImpression();
    }

    /* Access modifiers changed, original: 0000 */
    public Uri getPermalinkUri() {
        return this.permalinkUri;
    }

    /* Access modifiers changed, original: 0000 */
    public void setPermalinkUri(String str, Long l) {
        if (l.longValue() > 0) {
            this.permalinkUri = TweetUtils.getPermalink(str, l.longValue());
        }
    }

    private void setPermalinkLauncher() {
        setOnClickListener(new PermalinkClickListener());
    }

    /* Access modifiers changed, original: 0000 */
    public void launchPermalink() {
        if (!IntentUtils.safeStartActivity(getContext(), new Intent("android.intent.action.VIEW", getPermalinkUri()))) {
            Twitter.getLogger().e(TAG, "Activity cannot be found to open permalink URI");
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeImpression() {
        if (this.tweet != null) {
            this.dependencyProvider.getTweetScribeClient().impression(this.tweet, getViewTypeName(), this.tweetActionsEnabled);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scribePermalinkClick() {
        if (this.tweet != null) {
            this.dependencyProvider.getTweetScribeClient().click(this.tweet, getViewTypeName());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeCardImpression(Long l, Card card) {
        this.dependencyProvider.getVideoScribeClient().impression(ScribeItem.fromTweetCard(l.longValue(), card));
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeMediaEntityImpression(long j, MediaEntity mediaEntity) {
        this.dependencyProvider.getVideoScribeClient().impression(ScribeItem.fromMediaEntity(j, mediaEntity));
    }

    private void setName(Tweet tweet) {
        if (tweet == null || tweet.user == null) {
            this.fullNameView.setText("");
        } else {
            this.fullNameView.setText(Utils.stringOrEmpty(tweet.user.name));
        }
    }

    private void setScreenName(Tweet tweet) {
        if (tweet == null || tweet.user == null) {
            this.screenNameView.setText("");
        } else {
            this.screenNameView.setText(UserUtils.formatScreenName(Utils.stringOrEmpty(tweet.user.screenName)));
        }
    }

    @TargetApi(16)
    private void setText(Tweet tweet) {
        if (VERSION.SDK_INT >= 16) {
            this.contentView.setImportantForAccessibility(2);
        }
        CharSequence charSeqOrEmpty = Utils.charSeqOrEmpty(getLinkifiedText(tweet));
        SpanClickHandler.enableClicksOnSpans(this.contentView);
        if (TextUtils.isEmpty(charSeqOrEmpty)) {
            this.contentView.setText("");
            this.contentView.setVisibility(8);
            return;
        }
        this.contentView.setText(charSeqOrEmpty);
        this.contentView.setVisibility(0);
    }

    /* Access modifiers changed, original: final */
    public final void setTweetMedia(Tweet tweet) {
        clearTweetMedia();
        if (tweet != null) {
            if (tweet.card != null && VineCardUtils.isVine(tweet.card)) {
                Card card = tweet.card;
                ImageValue imageValue = VineCardUtils.getImageValue(card);
                String streamUrl = VineCardUtils.getStreamUrl(card);
                if (!(imageValue == null || TextUtils.isEmpty(streamUrl))) {
                    setViewsForMedia(getAspectRatio(imageValue));
                    this.tweetMediaView.setVineCard(tweet);
                    this.mediaBadgeView.setVisibility(0);
                    this.mediaBadgeView.setCard(card);
                    scribeCardImpression(Long.valueOf(tweet.id), card);
                }
            } else if (TweetMediaUtils.hasSupportedVideo(tweet)) {
                MediaEntity videoEntity = TweetMediaUtils.getVideoEntity(tweet);
                setViewsForMedia(getAspectRatio(videoEntity));
                this.tweetMediaView.setTweetMediaEntities(this.tweet, Collections.singletonList(videoEntity));
                this.mediaBadgeView.setVisibility(0);
                this.mediaBadgeView.setMediaEntity(videoEntity);
                scribeMediaEntityImpression(tweet.id, videoEntity);
            } else if (TweetMediaUtils.hasPhoto(tweet)) {
                List photoEntities = TweetMediaUtils.getPhotoEntities(tweet);
                setViewsForMedia(getAspectRatioForPhotoEntity(photoEntities.size()));
                this.tweetMediaView.setTweetMediaEntities(tweet, photoEntities);
                this.mediaBadgeView.setVisibility(8);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setViewsForMedia(double d) {
        this.mediaContainer.setVisibility(0);
        this.mediaContainer.setAspectRatio(d);
        this.tweetMediaView.setVisibility(0);
    }

    /* Access modifiers changed, original: protected */
    public double getAspectRatio(MediaEntity mediaEntity) {
        if (mediaEntity == null || mediaEntity.sizes == null || mediaEntity.sizes.medium == null || mediaEntity.sizes.medium.w == 0 || mediaEntity.sizes.medium.h == 0) {
            return DEFAULT_ASPECT_RATIO;
        }
        double d = (double) mediaEntity.sizes.medium.w;
        double d2 = (double) mediaEntity.sizes.medium.h;
        Double.isNaN(d);
        Double.isNaN(d2);
        return d / d2;
    }

    /* Access modifiers changed, original: protected */
    public double getAspectRatio(ImageValue imageValue) {
        if (imageValue == null || imageValue.width == 0 || imageValue.height == 0) {
            return DEFAULT_ASPECT_RATIO;
        }
        double d = (double) imageValue.width;
        double d2 = (double) imageValue.height;
        Double.isNaN(d);
        Double.isNaN(d2);
        return d / d2;
    }

    /* Access modifiers changed, original: protected */
    public void clearTweetMedia() {
        this.mediaContainer.setVisibility(8);
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getLinkifiedText(Tweet tweet) {
        FormattedTweetText formatTweetText = this.dependencyProvider.getTweetUi().getTweetRepository().formatTweetText(tweet);
        if (formatTweetText == null) {
            return null;
        }
        boolean z = tweet.card != null && VineCardUtils.isVine(tweet.card);
        return TweetTextLinkifier.linkifyUrls(formatTweetText, getLinkClickListener(), this.actionColor, this.actionHighlightColor, TweetUtils.showQuoteTweet(tweet), z);
    }

    /* Access modifiers changed, original: 0000 */
    public void setContentDescription(Tweet tweet) {
        if (TweetUtils.isTweetResolvable(tweet)) {
            FormattedTweetText formatTweetText = this.dependencyProvider.getTweetUi().getTweetRepository().formatTweetText(tweet);
            String str = null;
            String str2 = formatTweetText != null ? formatTweetText.text : null;
            long apiTimeToLong = TweetDateUtils.apiTimeToLong(tweet.createdAt);
            if (apiTimeToLong != -1) {
                str = DateFormat.getDateInstance().format(new Date(apiTimeToLong));
            }
            setContentDescription(getResources().getString(R.string.tw__tweet_content_description, new Object[]{Utils.stringOrEmpty(tweet.user.name), Utils.stringOrEmpty(str2), Utils.stringOrEmpty(str)}));
            return;
        }
        setContentDescription(getResources().getString(R.string.tw__loading_tweet));
    }

    /* Access modifiers changed, original: protected */
    public LinkClickListener getLinkClickListener() {
        if (this.linkClickListener == null) {
            this.linkClickListener = new LinkClickListener() {
                public void onUrlClicked(String str) {
                    if (!TextUtils.isEmpty(str)) {
                        if (AbstractTweetView.this.tweetLinkClickListener != null) {
                            AbstractTweetView.this.tweetLinkClickListener.onLinkClick(AbstractTweetView.this.tweet, str);
                        } else {
                            if (!IntentUtils.safeStartActivity(AbstractTweetView.this.getContext(), new Intent("android.intent.action.VIEW", Uri.parse(str)))) {
                                Twitter.getLogger().e(AbstractTweetView.TAG, "Activity cannot be found to open URL");
                            }
                        }
                    }
                }
            };
        }
        return this.linkClickListener;
    }
}
