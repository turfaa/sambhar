package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.IntentUtils;
import com.twitter.sdk.android.core.internal.VineCardUtils;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.GalleryActivity;
import com.twitter.sdk.android.tweetui.GalleryActivity.GalleryItem;
import com.twitter.sdk.android.tweetui.PlayerActivity;
import com.twitter.sdk.android.tweetui.PlayerActivity.PlayerItem;
import com.twitter.sdk.android.tweetui.R;
import com.twitter.sdk.android.tweetui.TweetMediaClickListener;
import com.twitter.sdk.android.tweetui.TweetUi;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class TweetMediaView extends ViewGroup implements OnClickListener {
    static final int MAX_IMAGE_VIEW_COUNT = 4;
    static final String SIZED_IMAGE_SMALL = ":small";
    final DependencyProvider dependencyProvider;
    private int imageCount;
    private final OverlayImageView[] imageViews;
    boolean internalRoundedCornersEnabled;
    int mediaBgColor;
    private final int mediaDividerSize;
    private List<MediaEntity> mediaEntities;
    private final Path path;
    int photoErrorResId;
    final float[] radii;
    private final RectF rect;
    Tweet tweet;
    TweetMediaClickListener tweetMediaClickListener;

    static class DependencyProvider {
        DependencyProvider() {
        }

        /* Access modifiers changed, original: 0000 */
        public Picasso getImageLoader() {
            return TweetUi.getInstance().getImageLoader();
        }
    }

    static class Size {
        static final Size EMPTY = new Size();
        final int height;
        final int width;

        private Size() {
            this(0, 0);
        }

        private Size(int i, int i2) {
            this.width = i;
            this.height = i2;
        }

        static Size fromSize(int i, int i2) {
            i = Math.max(i, 0);
            i2 = Math.max(i2, 0);
            return (i == 0 && i2 == 0) ? EMPTY : new Size(i, i2);
        }
    }

    static class PicassoCallback implements Callback {
        final WeakReference<ImageView> imageViewWeakReference;

        public void onError() {
        }

        PicassoCallback(ImageView imageView) {
            this.imageViewWeakReference = new WeakReference(imageView);
        }

        public void onSuccess() {
            ImageView imageView = (ImageView) this.imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setBackgroundResource(17170445);
            }
        }
    }

    public TweetMediaView(Context context) {
        this(context, null);
    }

    public TweetMediaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, new DependencyProvider());
    }

    TweetMediaView(Context context, AttributeSet attributeSet, DependencyProvider dependencyProvider) {
        super(context, attributeSet);
        this.imageViews = new OverlayImageView[4];
        this.mediaEntities = Collections.emptyList();
        this.path = new Path();
        this.rect = new RectF();
        this.radii = new float[8];
        this.mediaBgColor = ViewCompat.MEASURED_STATE_MASK;
        this.dependencyProvider = dependencyProvider;
        this.mediaDividerSize = getResources().getDimensionPixelSize(R.dimen.tw__media_view_divider_size);
        this.photoErrorResId = R.drawable.tw__ic_tweet_photo_error_dark;
    }

    public void setRoundedCornersRadii(int i, int i2, int i3, int i4) {
        float f = (float) i;
        this.radii[0] = f;
        this.radii[1] = f;
        float f2 = (float) i2;
        this.radii[2] = f2;
        this.radii[3] = f2;
        f2 = (float) i3;
        this.radii[4] = f2;
        this.radii[5] = f2;
        f2 = (float) i4;
        this.radii[6] = f2;
        this.radii[7] = f2;
        requestLayout();
    }

    public void setMediaBgColor(int i) {
        this.mediaBgColor = i;
    }

    public void setTweetMediaClickListener(TweetMediaClickListener tweetMediaClickListener) {
        this.tweetMediaClickListener = tweetMediaClickListener;
    }

    public void setPhotoErrorResId(int i) {
        this.photoErrorResId = i;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.imageCount > 0) {
            layoutImages();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        Size measureImages;
        if (this.imageCount > 0) {
            measureImages = measureImages(i, i2);
        } else {
            measureImages = Size.EMPTY;
        }
        setMeasuredDimension(measureImages.width, measureImages.height);
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.path.reset();
        this.rect.set(0.0f, 0.0f, (float) i, (float) i2);
        this.path.addRoundRect(this.rect, this.radii, Direction.CW);
        this.path.close();
    }

    /* Access modifiers changed, original: protected */
    public void dispatchDraw(Canvas canvas) {
        if (!this.internalRoundedCornersEnabled || VERSION.SDK_INT < 18) {
            super.dispatchDraw(canvas);
            return;
        }
        int save = canvas.save();
        canvas.clipPath(this.path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    public void onClick(View view) {
        Integer num = (Integer) view.getTag(R.id.tw__entity_index);
        if (this.tweetMediaClickListener != null) {
            this.tweetMediaClickListener.onMediaEntityClick(this.tweet, !this.mediaEntities.isEmpty() ? (MediaEntity) this.mediaEntities.get(num.intValue()) : null);
        } else if (this.mediaEntities.isEmpty()) {
            launchVideoPlayer(this.tweet);
        } else {
            MediaEntity mediaEntity = (MediaEntity) this.mediaEntities.get(num.intValue());
            if (TweetMediaUtils.isVideoType(mediaEntity)) {
                launchVideoPlayer(mediaEntity);
            } else if (TweetMediaUtils.isPhotoType(mediaEntity)) {
                launchPhotoGallery(num.intValue());
            }
        }
    }

    public void launchVideoPlayer(MediaEntity mediaEntity) {
        if (TweetMediaUtils.getSupportedVariant(mediaEntity) != null) {
            Intent intent = new Intent(getContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.PLAYER_ITEM, new PlayerItem(TweetMediaUtils.getSupportedVariant(mediaEntity).url, TweetMediaUtils.isLooping(mediaEntity), TweetMediaUtils.showVideoControls(mediaEntity), null, null));
            IntentUtils.safeStartActivity(getContext(), intent);
        }
    }

    public void launchVideoPlayer(Tweet tweet) {
        Card card = tweet.card;
        Intent intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.PLAYER_ITEM, new PlayerItem(VineCardUtils.getStreamUrl(card), true, false, null, null));
        intent.putExtra(PlayerActivity.SCRIBE_ITEM, ScribeItem.fromTweetCard(tweet.id, card));
        IntentUtils.safeStartActivity(getContext(), intent);
    }

    public void launchPhotoGallery(int i) {
        Intent intent = new Intent(getContext(), GalleryActivity.class);
        intent.putExtra(GalleryActivity.GALLERY_ITEM, new GalleryItem(this.tweet.id, i, this.mediaEntities));
        IntentUtils.safeStartActivity(getContext(), intent);
    }

    public void setTweetMediaEntities(Tweet tweet, List<MediaEntity> list) {
        if (tweet != null && list != null && !list.isEmpty() && !list.equals(this.mediaEntities)) {
            this.tweet = tweet;
            this.mediaEntities = list;
            clearImageViews();
            initializeImageViews((List) list);
            if (TweetMediaUtils.isPhotoType((MediaEntity) list.get(0))) {
                this.internalRoundedCornersEnabled = true;
            } else {
                this.internalRoundedCornersEnabled = false;
            }
            requestLayout();
        }
    }

    public void setVineCard(Tweet tweet) {
        if (tweet != null && tweet.card != null && VineCardUtils.isVine(tweet.card)) {
            this.tweet = tweet;
            this.mediaEntities = Collections.emptyList();
            clearImageViews();
            initializeImageViews(tweet.card);
            this.internalRoundedCornersEnabled = false;
            requestLayout();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Size measureImages(int i, int i2) {
        i = MeasureSpec.getSize(i);
        i2 = MeasureSpec.getSize(i2);
        int i3 = (i - this.mediaDividerSize) / 2;
        int i4 = (i2 - this.mediaDividerSize) / 2;
        switch (this.imageCount) {
            case 1:
                measureImageView(0, i, i2);
                break;
            case 2:
                measureImageView(0, i3, i2);
                measureImageView(1, i3, i2);
                break;
            case 3:
                measureImageView(0, i3, i2);
                measureImageView(1, i3, i4);
                measureImageView(2, i3, i4);
                break;
            case 4:
                measureImageView(0, i3, i4);
                measureImageView(1, i3, i4);
                measureImageView(2, i3, i4);
                measureImageView(3, i3, i4);
                break;
        }
        return Size.fromSize(i, i2);
    }

    /* Access modifiers changed, original: 0000 */
    public void measureImageView(int i, int i2, int i3) {
        this.imageViews[i].measure(MeasureSpec.makeMeasureSpec(i2, 1073741824), MeasureSpec.makeMeasureSpec(i3, 1073741824));
    }

    /* Access modifiers changed, original: 0000 */
    public void layoutImages() {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int i = (measuredWidth - this.mediaDividerSize) / 2;
        int i2 = (measuredHeight - this.mediaDividerSize) / 2;
        int i3 = i + this.mediaDividerSize;
        int i4;
        int i5;
        switch (this.imageCount) {
            case 1:
                layoutImage(0, 0, 0, measuredWidth, measuredHeight);
                return;
            case 2:
                int i6 = measuredHeight;
                layoutImage(0, 0, 0, i, i6);
                layoutImage(1, i + this.mediaDividerSize, 0, measuredWidth, i6);
                return;
            case 3:
                layoutImage(0, 0, 0, i, measuredHeight);
                i4 = i3;
                i5 = measuredWidth;
                layoutImage(1, i4, 0, i5, i2);
                layoutImage(2, i4, i2 + this.mediaDividerSize, i5, measuredHeight);
                return;
            case 4:
                i5 = i;
                layoutImage(0, 0, 0, i5, i2);
                layoutImage(2, 0, i2 + this.mediaDividerSize, i5, measuredHeight);
                i4 = i3;
                i5 = measuredWidth;
                layoutImage(1, i4, 0, i5, i2);
                layoutImage(3, i4, i2 + this.mediaDividerSize, i5, measuredHeight);
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void layoutImage(int i, int i2, int i3, int i4, int i5) {
        ImageView imageView = this.imageViews[i];
        if (imageView.getLeft() != i2 || imageView.getTop() != i3 || imageView.getRight() != i4 || imageView.getBottom() != i5) {
            imageView.layout(i2, i3, i4, i5);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void clearImageViews() {
        for (int i = 0; i < this.imageCount; i++) {
            ImageView imageView = this.imageViews[i];
            if (imageView != null) {
                imageView.setVisibility(8);
            }
        }
        this.imageCount = 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void initializeImageViews(List<MediaEntity> list) {
        this.imageCount = Math.min(4, list.size());
        for (int i = 0; i < this.imageCount; i++) {
            OverlayImageView orCreateImageView = getOrCreateImageView(i);
            MediaEntity mediaEntity = (MediaEntity) list.get(i);
            setAltText(orCreateImageView, mediaEntity.altText);
            setMediaImage(orCreateImageView, getSizedImagePath(mediaEntity));
            setOverlayImage(orCreateImageView, TweetMediaUtils.isVideoType(mediaEntity));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void initializeImageViews(Card card) {
        this.imageCount = 1;
        OverlayImageView orCreateImageView = getOrCreateImageView(0);
        ImageValue imageValue = VineCardUtils.getImageValue(card);
        setAltText(orCreateImageView, imageValue.alt);
        setMediaImage(orCreateImageView, imageValue.url);
        setOverlayImage(orCreateImageView, true);
    }

    /* Access modifiers changed, original: 0000 */
    public OverlayImageView getOrCreateImageView(int i) {
        OverlayImageView overlayImageView = this.imageViews[i];
        if (overlayImageView == null) {
            overlayImageView = new OverlayImageView(getContext());
            overlayImageView.setLayoutParams(generateDefaultLayoutParams());
            overlayImageView.setOnClickListener(this);
            this.imageViews[i] = overlayImageView;
            addView(overlayImageView, i);
        } else {
            measureImageView(i, 0, 0);
            layoutImage(i, 0, 0, 0, 0);
        }
        overlayImageView.setVisibility(0);
        overlayImageView.setBackgroundColor(this.mediaBgColor);
        overlayImageView.setTag(R.id.tw__entity_index, Integer.valueOf(i));
        return overlayImageView;
    }

    /* Access modifiers changed, original: 0000 */
    public String getSizedImagePath(MediaEntity mediaEntity) {
        if (this.imageCount <= 1) {
            return mediaEntity.mediaUrlHttps;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mediaEntity.mediaUrlHttps);
        stringBuilder.append(SIZED_IMAGE_SMALL);
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public void setAltText(ImageView imageView, String str) {
        if (TextUtils.isEmpty(str)) {
            imageView.setContentDescription(getResources().getString(R.string.tw__tweet_media));
        } else {
            imageView.setContentDescription(str);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setOverlayImage(OverlayImageView overlayImageView, boolean z) {
        if (z) {
            overlayImageView.setOverlayDrawable(getContext().getResources().getDrawable(R.drawable.tw__player_overlay));
        } else {
            overlayImageView.setOverlayDrawable(null);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setMediaImage(ImageView imageView, String str) {
        Picasso imageLoader = this.dependencyProvider.getImageLoader();
        if (imageLoader != null) {
            imageLoader.load(str).fit().centerCrop().error(this.photoErrorResId).into(imageView, new PicassoCallback(imageView));
        }
    }
}
