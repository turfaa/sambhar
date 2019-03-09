package com.bumptech.glide.provider;

import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import com.bumptech.glide.load.engine.DecodePath;
import com.bumptech.glide.load.engine.LoadPath;
import com.bumptech.glide.load.resource.transcode.UnitTranscoder;
import com.bumptech.glide.util.MultiClassKey;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class LoadPathCache {
    private static final LoadPath<?, ?, ?> NO_PATHS_SIGNAL = new LoadPath(Object.class, Object.class, Object.class, Collections.singletonList(new DecodePath(Object.class, Object.class, Object.class, Collections.emptyList(), new UnitTranscoder(), null)), null);
    private final ArrayMap<MultiClassKey, LoadPath<?, ?, ?>> cache = new ArrayMap();
    private final AtomicReference<MultiClassKey> keyRef = new AtomicReference();

    public boolean isEmptyLoadPath(@Nullable LoadPath<?, ?, ?> loadPath) {
        return NO_PATHS_SIGNAL.equals(loadPath);
    }

    @Nullable
    public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> get(Class<Data> cls, Class<TResource> cls2, Class<Transcode> cls3) {
        LoadPath loadPath;
        MultiClassKey key = getKey(cls, cls2, cls3);
        synchronized (this.cache) {
            loadPath = (LoadPath) this.cache.get(key);
        }
        this.keyRef.set(key);
        return loadPath;
    }

    public void put(Class<?> cls, Class<?> cls2, Class<?> cls3, @Nullable LoadPath<?, ?, ?> loadPath) {
        synchronized (this.cache) {
            Object loadPath2;
            ArrayMap arrayMap = this.cache;
            MultiClassKey multiClassKey = new MultiClassKey(cls, cls2, cls3);
            if (loadPath2 == null) {
                loadPath2 = NO_PATHS_SIGNAL;
            }
            arrayMap.put(multiClassKey, loadPath2);
        }
    }

    private MultiClassKey getKey(Class<?> cls, Class<?> cls2, Class<?> cls3) {
        MultiClassKey multiClassKey = (MultiClassKey) this.keyRef.getAndSet(null);
        if (multiClassKey == null) {
            multiClassKey = new MultiClassKey();
        }
        multiClassKey.set(cls, cls2, cls3);
        return multiClassKey;
    }
}
