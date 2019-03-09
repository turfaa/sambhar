package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;

public class BitmapResource implements Resource<Bitmap>, Initializable {
    private final Bitmap bitmap;
    private final BitmapPool bitmapPool;

    @Nullable
    public static BitmapResource obtain(@Nullable Bitmap bitmap, @NonNull BitmapPool bitmapPool) {
        return bitmap == null ? null : new BitmapResource(bitmap, bitmapPool);
    }

    public BitmapResource(@NonNull Bitmap bitmap, @NonNull BitmapPool bitmapPool) {
        this.bitmap = (Bitmap) Preconditions.checkNotNull(bitmap, "Bitmap must not be null");
        this.bitmapPool = (BitmapPool) Preconditions.checkNotNull(bitmapPool, "BitmapPool must not be null");
    }

    @NonNull
    public Class<Bitmap> getResourceClass() {
        return Bitmap.class;
    }

    @NonNull
    public Bitmap get() {
        return this.bitmap;
    }

    public int getSize() {
        return Util.getBitmapByteSize(this.bitmap);
    }

    public void recycle() {
        this.bitmapPool.put(this.bitmap);
    }

    public void initialize() {
        this.bitmap.prepareToDraw();
    }
}
