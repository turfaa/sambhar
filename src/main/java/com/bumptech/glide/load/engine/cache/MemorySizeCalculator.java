package com.bumptech.glide.load.engine.cache;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import com.bumptech.glide.util.Preconditions;

public final class MemorySizeCalculator {
    @VisibleForTesting
    static final int BYTES_PER_ARGB_8888_PIXEL = 4;
    private static final int LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR = 2;
    private static final String TAG = "MemorySizeCalculator";
    private final int arrayPoolSize;
    private final int bitmapPoolSize;
    private final Context context;
    private final int memoryCacheSize;

    public static final class Builder {
        static final int ARRAY_POOL_SIZE_BYTES = 4194304;
        static final int BITMAP_POOL_TARGET_SCREENS = (VERSION.SDK_INT < 26 ? 4 : 1);
        static final float LOW_MEMORY_MAX_SIZE_MULTIPLIER = 0.33f;
        static final float MAX_SIZE_MULTIPLIER = 0.4f;
        @VisibleForTesting
        static final int MEMORY_CACHE_TARGET_SCREENS = 2;
        ActivityManager activityManager;
        int arrayPoolSizeBytes = 4194304;
        float bitmapPoolScreens = ((float) BITMAP_POOL_TARGET_SCREENS);
        final Context context;
        float lowMemoryMaxSizeMultiplier = LOW_MEMORY_MAX_SIZE_MULTIPLIER;
        float maxSizeMultiplier = MAX_SIZE_MULTIPLIER;
        float memoryCacheScreens = 2.0f;
        ScreenDimensions screenDimensions;

        public Builder(Context context) {
            this.context = context;
            this.activityManager = (ActivityManager) context.getSystemService("activity");
            this.screenDimensions = new DisplayMetricsScreenDimensions(context.getResources().getDisplayMetrics());
            if (VERSION.SDK_INT >= 26 && MemorySizeCalculator.isLowMemoryDevice(this.activityManager)) {
                this.bitmapPoolScreens = 0.0f;
            }
        }

        public Builder setMemoryCacheScreens(float f) {
            Preconditions.checkArgument(f >= 0.0f, "Memory cache screens must be greater than or equal to 0");
            this.memoryCacheScreens = f;
            return this;
        }

        public Builder setBitmapPoolScreens(float f) {
            Preconditions.checkArgument(f >= 0.0f, "Bitmap pool screens must be greater than or equal to 0");
            this.bitmapPoolScreens = f;
            return this;
        }

        public Builder setMaxSizeMultiplier(float f) {
            boolean z = f >= 0.0f && f <= 1.0f;
            Preconditions.checkArgument(z, "Size multiplier must be between 0 and 1");
            this.maxSizeMultiplier = f;
            return this;
        }

        public Builder setLowMemoryMaxSizeMultiplier(float f) {
            boolean z = f >= 0.0f && f <= 1.0f;
            Preconditions.checkArgument(z, "Low memory max size multiplier must be between 0 and 1");
            this.lowMemoryMaxSizeMultiplier = f;
            return this;
        }

        public Builder setArrayPoolSize(int i) {
            this.arrayPoolSizeBytes = i;
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public Builder setActivityManager(ActivityManager activityManager) {
            this.activityManager = activityManager;
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public Builder setScreenDimensions(ScreenDimensions screenDimensions) {
            this.screenDimensions = screenDimensions;
            return this;
        }

        public MemorySizeCalculator build() {
            return new MemorySizeCalculator(this);
        }
    }

    interface ScreenDimensions {
        int getHeightPixels();

        int getWidthPixels();
    }

    private static final class DisplayMetricsScreenDimensions implements ScreenDimensions {
        private final DisplayMetrics displayMetrics;

        DisplayMetricsScreenDimensions(DisplayMetrics displayMetrics) {
            this.displayMetrics = displayMetrics;
        }

        public int getWidthPixels() {
            return this.displayMetrics.widthPixels;
        }

        public int getHeightPixels() {
            return this.displayMetrics.heightPixels;
        }
    }

    MemorySizeCalculator(Builder builder) {
        int i;
        this.context = builder.context;
        if (isLowMemoryDevice(builder.activityManager)) {
            i = builder.arrayPoolSizeBytes / 2;
        } else {
            i = builder.arrayPoolSizeBytes;
        }
        this.arrayPoolSize = i;
        i = getMaxSize(builder.activityManager, builder.maxSizeMultiplier, builder.lowMemoryMaxSizeMultiplier);
        float widthPixels = (float) ((builder.screenDimensions.getWidthPixels() * builder.screenDimensions.getHeightPixels()) * 4);
        int round = Math.round(builder.bitmapPoolScreens * widthPixels);
        int round2 = Math.round(widthPixels * builder.memoryCacheScreens);
        int i2 = i - this.arrayPoolSize;
        int i3 = round2 + round;
        if (i3 <= i2) {
            this.memoryCacheSize = round2;
            this.bitmapPoolSize = round;
        } else {
            widthPixels = ((float) i2) / (builder.bitmapPoolScreens + builder.memoryCacheScreens);
            this.memoryCacheSize = Math.round(builder.memoryCacheScreens * widthPixels);
            this.bitmapPoolSize = Math.round(widthPixels * builder.bitmapPoolScreens);
        }
        if (Log.isLoggable(TAG, 3)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Calculation complete, Calculated memory cache size: ");
            stringBuilder.append(toMb(this.memoryCacheSize));
            stringBuilder.append(", pool size: ");
            stringBuilder.append(toMb(this.bitmapPoolSize));
            stringBuilder.append(", byte array size: ");
            stringBuilder.append(toMb(this.arrayPoolSize));
            stringBuilder.append(", memory class limited? ");
            stringBuilder.append(i3 > i);
            stringBuilder.append(", max size: ");
            stringBuilder.append(toMb(i));
            stringBuilder.append(", memoryClass: ");
            stringBuilder.append(builder.activityManager.getMemoryClass());
            stringBuilder.append(", isLowMemoryDevice: ");
            stringBuilder.append(isLowMemoryDevice(builder.activityManager));
            Log.d(str, stringBuilder.toString());
        }
    }

    public int getMemoryCacheSize() {
        return this.memoryCacheSize;
    }

    public int getBitmapPoolSize() {
        return this.bitmapPoolSize;
    }

    public int getArrayPoolSizeInBytes() {
        return this.arrayPoolSize;
    }

    private static int getMaxSize(ActivityManager activityManager, float f, float f2) {
        float memoryClass = (float) ((activityManager.getMemoryClass() * 1024) * 1024);
        if (isLowMemoryDevice(activityManager)) {
            f = f2;
        }
        return Math.round(memoryClass * f);
    }

    private String toMb(int i) {
        return Formatter.formatFileSize(this.context, (long) i);
    }

    @TargetApi(19)
    static boolean isLowMemoryDevice(ActivityManager activityManager) {
        return VERSION.SDK_INT >= 19 ? activityManager.isLowRamDevice() : true;
    }
}
