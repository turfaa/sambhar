package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.cache.DiskCache.Writer;
import java.io.File;

class DataCacheWriter<DataType> implements Writer {
    private final DataType data;
    private final Encoder<DataType> encoder;
    private final Options options;

    DataCacheWriter(Encoder<DataType> encoder, DataType dataType, Options options) {
        this.encoder = encoder;
        this.data = dataType;
        this.options = options;
    }

    public boolean write(@NonNull File file) {
        return this.encoder.encode(this.data, file, this.options);
    }
}
