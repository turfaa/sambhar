package com.bumptech.glide.request.transition;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class BitmapTransitionFactory extends BitmapContainerTransitionFactory<Bitmap> {
    /* Access modifiers changed, original: protected */
    @NonNull
    public Bitmap getBitmap(@NonNull Bitmap bitmap) {
        return bitmap;
    }

    public BitmapTransitionFactory(@NonNull TransitionFactory<Drawable> transitionFactory) {
        super(transitionFactory);
    }
}
