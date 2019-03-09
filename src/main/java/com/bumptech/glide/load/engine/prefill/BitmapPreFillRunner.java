package com.bumptech.glide.load.engine.prefill;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.util.Util;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class BitmapPreFillRunner implements Runnable {
    static final int BACKOFF_RATIO = 4;
    private static final Clock DEFAULT_CLOCK = new Clock();
    static final long INITIAL_BACKOFF_MS = 40;
    static final long MAX_BACKOFF_MS = TimeUnit.SECONDS.toMillis(1);
    static final long MAX_DURATION_MS = 32;
    @VisibleForTesting
    static final String TAG = "PreFillRunner";
    private final BitmapPool bitmapPool;
    private final Clock clock;
    private long currentDelay;
    private final Handler handler;
    private boolean isCancelled;
    private final MemoryCache memoryCache;
    private final Set<PreFillType> seenTypes;
    private final PreFillQueue toPrefill;

    @VisibleForTesting
    static class Clock {
        Clock() {
        }

        /* Access modifiers changed, original: 0000 */
        public long now() {
            return SystemClock.currentThreadTimeMillis();
        }
    }

    private static final class UniqueKey implements Key {
        UniqueKey() {
        }

        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            throw new UnsupportedOperationException();
        }
    }

    public BitmapPreFillRunner(BitmapPool bitmapPool, MemoryCache memoryCache, PreFillQueue preFillQueue) {
        this(bitmapPool, memoryCache, preFillQueue, DEFAULT_CLOCK, new Handler(Looper.getMainLooper()));
    }

    @VisibleForTesting
    BitmapPreFillRunner(BitmapPool bitmapPool, MemoryCache memoryCache, PreFillQueue preFillQueue, Clock clock, Handler handler) {
        this.seenTypes = new HashSet();
        this.currentDelay = INITIAL_BACKOFF_MS;
        this.bitmapPool = bitmapPool;
        this.memoryCache = memoryCache;
        this.toPrefill = preFillQueue;
        this.clock = clock;
        this.handler = handler;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean allocate() {
        long now = this.clock.now();
        while (!this.toPrefill.isEmpty() && !isGcDetected(now)) {
            Bitmap createBitmap;
            PreFillType remove = this.toPrefill.remove();
            if (this.seenTypes.contains(remove)) {
                createBitmap = Bitmap.createBitmap(remove.getWidth(), remove.getHeight(), remove.getConfig());
            } else {
                this.seenTypes.add(remove);
                createBitmap = this.bitmapPool.getDirty(remove.getWidth(), remove.getHeight(), remove.getConfig());
            }
            int bitmapByteSize = Util.getBitmapByteSize(createBitmap);
            if (getFreeMemoryCacheBytes() >= ((long) bitmapByteSize)) {
                this.memoryCache.put(new UniqueKey(), BitmapResource.obtain(createBitmap, this.bitmapPool));
            } else {
                this.bitmapPool.put(createBitmap);
            }
            if (Log.isLoggable(TAG, 3)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("allocated [");
                stringBuilder.append(remove.getWidth());
                stringBuilder.append("x");
                stringBuilder.append(remove.getHeight());
                stringBuilder.append("] ");
                stringBuilder.append(remove.getConfig());
                stringBuilder.append(" size: ");
                stringBuilder.append(bitmapByteSize);
                Log.d(str, stringBuilder.toString());
            }
        }
        return (this.isCancelled || this.toPrefill.isEmpty()) ? false : true;
    }

    private boolean isGcDetected(long j) {
        return this.clock.now() - j >= 32;
    }

    private long getFreeMemoryCacheBytes() {
        return this.memoryCache.getMaxSize() - this.memoryCache.getCurrentSize();
    }

    public void run() {
        if (allocate()) {
            this.handler.postDelayed(this, getNextDelay());
        }
    }

    private long getNextDelay() {
        long j = this.currentDelay;
        this.currentDelay = Math.min(this.currentDelay * 4, MAX_BACKOFF_MS);
        return j;
    }
}
