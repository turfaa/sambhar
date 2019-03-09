package com.bumptech.glide.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.bumptech.glide.ListPreloader.PreloadSizeProvider;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import java.util.Arrays;

public class ViewPreloadSizeProvider<T> implements PreloadSizeProvider<T>, SizeReadyCallback {
    private int[] size;
    private SizeViewTarget viewTarget;

    private static final class SizeViewTarget extends ViewTarget<View, Object> {
        public void onResourceReady(@NonNull Object obj, @Nullable Transition<? super Object> transition) {
        }

        SizeViewTarget(@NonNull View view, @NonNull SizeReadyCallback sizeReadyCallback) {
            super(view);
            getSize(sizeReadyCallback);
        }
    }

    public ViewPreloadSizeProvider(@NonNull View view) {
        this.viewTarget = new SizeViewTarget(view, this);
    }

    @Nullable
    public int[] getPreloadSize(@NonNull T t, int i, int i2) {
        if (this.size == null) {
            return null;
        }
        return Arrays.copyOf(this.size, this.size.length);
    }

    public void onSizeReady(int i, int i2) {
        this.size = new int[]{i, i2};
        this.viewTarget = null;
    }

    public void setView(@NonNull View view) {
        if (this.size == null && this.viewTarget == null) {
            this.viewTarget = new SizeViewTarget(view, this);
        }
    }
}
