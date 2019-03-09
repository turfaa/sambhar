package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import android.support.v4.util.Pools.Pool;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LoadPath<Data, ResourceType, Transcode> {
    private final Class<Data> dataClass;
    private final List<? extends DecodePath<Data, ResourceType, Transcode>> decodePaths;
    private final String failureMessage;
    private final Pool<List<Throwable>> listPool;

    public LoadPath(Class<Data> cls, Class<ResourceType> cls2, Class<Transcode> cls3, List<DecodePath<Data, ResourceType, Transcode>> list, Pool<List<Throwable>> pool) {
        this.dataClass = cls;
        this.listPool = pool;
        this.decodePaths = (List) Preconditions.checkNotEmpty((Collection) list);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Failed LoadPath{");
        stringBuilder.append(cls.getSimpleName());
        stringBuilder.append("->");
        stringBuilder.append(cls2.getSimpleName());
        stringBuilder.append("->");
        stringBuilder.append(cls3.getSimpleName());
        stringBuilder.append("}");
        this.failureMessage = stringBuilder.toString();
    }

    public Resource<Transcode> load(DataRewinder<Data> dataRewinder, @NonNull Options options, int i, int i2, DecodeCallback<ResourceType> decodeCallback) throws GlideException {
        List list = (List) Preconditions.checkNotNull(this.listPool.acquire());
        try {
            Resource<Transcode> loadWithExceptionList = loadWithExceptionList(dataRewinder, options, i, i2, decodeCallback, list);
            return loadWithExceptionList;
        } finally {
            this.listPool.release(list);
        }
    }

    private Resource<Transcode> loadWithExceptionList(DataRewinder<Data> dataRewinder, @NonNull Options options, int i, int i2, DecodeCallback<ResourceType> decodeCallback, List<Throwable> list) throws GlideException {
        List<Throwable> list2 = list;
        int size = this.decodePaths.size();
        Resource<Transcode> resource = null;
        for (int i3 = 0; i3 < size; i3++) {
            try {
                resource = ((DecodePath) this.decodePaths.get(i3)).decode(dataRewinder, i, i2, options, decodeCallback);
            } catch (GlideException e) {
                list2.add(e);
            }
            if (resource != null) {
                break;
            }
        }
        if (resource != null) {
            return resource;
        }
        throw new GlideException(this.failureMessage, new ArrayList(list2));
    }

    public Class<Data> getDataClass() {
        return this.dataClass;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LoadPath{decodePaths=");
        stringBuilder.append(Arrays.toString(this.decodePaths.toArray()));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
