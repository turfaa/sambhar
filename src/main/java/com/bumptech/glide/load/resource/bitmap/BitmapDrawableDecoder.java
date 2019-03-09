package com.bumptech.glide.load.resource.bitmap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Preconditions;
import java.io.IOException;

public class BitmapDrawableDecoder<DataType> implements ResourceDecoder<DataType, BitmapDrawable> {
    private final ResourceDecoder<DataType, Bitmap> decoder;
    private final Resources resources;

    public BitmapDrawableDecoder(Context context, ResourceDecoder<DataType, Bitmap> resourceDecoder) {
        this(context.getResources(), (ResourceDecoder) resourceDecoder);
    }

    @Deprecated
    public BitmapDrawableDecoder(Resources resources, BitmapPool bitmapPool, ResourceDecoder<DataType, Bitmap> resourceDecoder) {
        this(resources, (ResourceDecoder) resourceDecoder);
    }

    public BitmapDrawableDecoder(@NonNull Resources resources, @NonNull ResourceDecoder<DataType, Bitmap> resourceDecoder) {
        this.resources = (Resources) Preconditions.checkNotNull(resources);
        this.decoder = (ResourceDecoder) Preconditions.checkNotNull(resourceDecoder);
    }

    public boolean handles(@NonNull DataType dataType, @NonNull Options options) throws IOException {
        return this.decoder.handles(dataType, options);
    }

    public Resource<BitmapDrawable> decode(@NonNull DataType dataType, int i, int i2, @NonNull Options options) throws IOException {
        return LazyBitmapDrawableResource.obtain(this.resources, this.decoder.decode(dataType, i, i2, options));
    }
}
