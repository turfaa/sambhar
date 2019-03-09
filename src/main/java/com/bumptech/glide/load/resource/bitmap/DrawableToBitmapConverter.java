package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import java.util.concurrent.locks.Lock;

final class DrawableToBitmapConverter {
    private static final BitmapPool NO_RECYCLE_BITMAP_POOL = new BitmapPoolAdapter() {
        public void put(Bitmap bitmap) {
        }
    };
    private static final String TAG = "DrawableToBitmap";

    private DrawableToBitmapConverter() {
    }

    @Nullable
    static Resource<Bitmap> convert(BitmapPool bitmapPool, Drawable drawable, int i, int i2) {
        Bitmap bitmap;
        drawable = drawable.getCurrent();
        Object obj = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof Animatable) {
            bitmap = null;
        } else {
            bitmap = drawToBitmap(bitmapPool, drawable, i, i2);
            obj = 1;
        }
        if (obj == null) {
            bitmapPool = NO_RECYCLE_BITMAP_POOL;
        }
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    @Nullable
    private static Bitmap drawToBitmap(BitmapPool bitmapPool, Drawable drawable, int i, int i2) {
        String str;
        StringBuilder stringBuilder;
        if (i == Integer.MIN_VALUE && drawable.getIntrinsicWidth() <= 0) {
            if (Log.isLoggable(TAG, 5)) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to draw ");
                stringBuilder.append(drawable);
                stringBuilder.append(" to Bitmap with Target.SIZE_ORIGINAL because the Drawable has no intrinsic width");
                Log.w(str, stringBuilder.toString());
            }
            return null;
        } else if (i2 != Integer.MIN_VALUE || drawable.getIntrinsicHeight() > 0) {
            if (drawable.getIntrinsicWidth() > 0) {
                i = drawable.getIntrinsicWidth();
            }
            if (drawable.getIntrinsicHeight() > 0) {
                i2 = drawable.getIntrinsicHeight();
            }
            Lock bitmapDrawableLock = TransformationUtils.getBitmapDrawableLock();
            bitmapDrawableLock.lock();
            Bitmap bitmap = bitmapPool.get(i, i2, Config.ARGB_8888);
            try {
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, i, i2);
                drawable.draw(canvas);
                canvas.setBitmap(null);
                return bitmap;
            } finally {
                bitmapDrawableLock.unlock();
            }
        } else {
            if (Log.isLoggable(TAG, 5)) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to draw ");
                stringBuilder.append(drawable);
                stringBuilder.append(" to Bitmap with Target.SIZE_ORIGINAL because the Drawable has no intrinsic height");
                Log.w(str, stringBuilder.toString());
            }
            return null;
        }
    }
}
