package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.twitter.sdk.android.core.internal.VineCardUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.tweetui.R;

public class MediaBadgeView extends FrameLayout {
    ImageView badge;
    TextView videoDuration;

    public MediaBadgeView(Context context) {
        this(context, null);
    }

    public MediaBadgeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MediaBadgeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        initSubViews(context);
    }

    /* Access modifiers changed, original: 0000 */
    public void initSubViews(Context context) {
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.tw__media_badge, this, true);
        this.videoDuration = (TextView) inflate.findViewById(R.id.tw__video_duration);
        this.badge = (ImageView) inflate.findViewById(R.id.tw__gif_badge);
    }

    public void setMediaEntity(MediaEntity mediaEntity) {
        if ("animated_gif".equals(mediaEntity.type)) {
            setBadge(getResources().getDrawable(R.drawable.tw__gif_badge));
        } else if ("video".equals(mediaEntity.type)) {
            setText(mediaEntity.videoInfo == null ? 0 : mediaEntity.videoInfo.durationMillis);
        } else {
            setEmpty();
        }
    }

    public void setCard(Card card) {
        if (VineCardUtils.isVine(card)) {
            setBadge(getResources().getDrawable(R.drawable.tw__vine_badge));
        } else {
            setEmpty();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setText(long j) {
        this.videoDuration.setVisibility(0);
        this.badge.setVisibility(8);
        this.videoDuration.setText(MediaTimeUtils.getPlaybackTime(j));
    }

    /* Access modifiers changed, original: 0000 */
    public void setBadge(Drawable drawable) {
        this.badge.setVisibility(0);
        this.videoDuration.setVisibility(8);
        this.badge.setImageDrawable(drawable);
    }

    /* Access modifiers changed, original: 0000 */
    public void setEmpty() {
        this.videoDuration.setVisibility(8);
        this.badge.setVisibility(8);
    }
}
