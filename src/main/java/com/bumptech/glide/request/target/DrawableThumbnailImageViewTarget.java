package com.bumptech.glide.request.target;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class DrawableThumbnailImageViewTarget extends ThumbnailImageViewTarget<Drawable> {
    /* Access modifiers changed, original: protected */
    public Drawable getDrawable(Drawable drawable) {
        return drawable;
    }

    public DrawableThumbnailImageViewTarget(ImageView imageView) {
        super(imageView);
    }

    @Deprecated
    public DrawableThumbnailImageViewTarget(ImageView imageView, boolean z) {
        super(imageView, z);
    }
}
