package com.bumptech.glide.request.target;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class BitmapImageViewTarget extends ImageViewTarget<Bitmap> {
    public BitmapImageViewTarget(ImageView imageView) {
        super(imageView);
    }

    @Deprecated
    public BitmapImageViewTarget(ImageView imageView, boolean z) {
        super(imageView, z);
    }

    /* Access modifiers changed, original: protected */
    public void setResource(Bitmap bitmap) {
        ((ImageView) this.view).setImageBitmap(bitmap);
    }
}
