package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener.Callback;

public class GalleryImageView extends FrameLayout implements Target {
    final MultiTouchImageView imageView;
    final ProgressBar progressBar;

    public void onBitmapFailed(Drawable drawable) {
    }

    public GalleryImageView(Context context) {
        this(context, new MultiTouchImageView(context), new ProgressBar(context));
    }

    GalleryImageView(Context context, MultiTouchImageView multiTouchImageView, ProgressBar progressBar) {
        super(context);
        this.imageView = multiTouchImageView;
        this.progressBar = progressBar;
        progressBar.setLayoutParams(new LayoutParams(-2, -2, 17));
        addView(progressBar);
        multiTouchImageView.setLayoutParams(new LayoutParams(-1, -1, 17));
        addView(multiTouchImageView);
    }

    public void setSwipeToDismissCallback(Callback callback) {
        this.imageView.setOnTouchListener(SwipeToDismissTouchListener.createFromView(this.imageView, callback));
    }

    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom loadedFrom) {
        this.imageView.setImageBitmap(bitmap);
        this.progressBar.setVisibility(8);
    }

    public void onPrepareLoad(Drawable drawable) {
        this.imageView.setImageResource(17170445);
        this.progressBar.setVisibility(0);
    }
}
