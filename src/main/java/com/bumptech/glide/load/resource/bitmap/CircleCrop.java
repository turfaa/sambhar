package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import java.security.MessageDigest;

public class CircleCrop extends BitmapTransformation {
    private static final String ID = "com.bumptech.glide.load.resource.bitmap.CircleCrop.1";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);
    private static final int VERSION = 1;

    /* Access modifiers changed, original: protected */
    public Bitmap transform(@NonNull BitmapPool bitmapPool, @NonNull Bitmap bitmap, int i, int i2) {
        return TransformationUtils.circleCrop(bitmapPool, bitmap, i, i2);
    }

    public boolean equals(Object obj) {
        return obj instanceof CircleCrop;
    }

    public int hashCode() {
        return ID.hashCode();
    }

    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
