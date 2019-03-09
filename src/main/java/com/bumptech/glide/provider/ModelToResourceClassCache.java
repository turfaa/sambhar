package com.bumptech.glide.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import com.bumptech.glide.util.MultiClassKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ModelToResourceClassCache {
    private final ArrayMap<MultiClassKey, List<Class<?>>> registeredResourceClassCache = new ArrayMap();
    private final AtomicReference<MultiClassKey> resourceClassKeyRef = new AtomicReference();

    @Nullable
    public List<Class<?>> get(@NonNull Class<?> cls, @NonNull Class<?> cls2) {
        List list;
        Object obj = (MultiClassKey) this.resourceClassKeyRef.getAndSet(null);
        if (obj == null) {
            obj = new MultiClassKey(cls, cls2);
        } else {
            obj.set(cls, cls2);
        }
        synchronized (this.registeredResourceClassCache) {
            list = (List) this.registeredResourceClassCache.get(obj);
        }
        this.resourceClassKeyRef.set(obj);
        return list;
    }

    public void put(@NonNull Class<?> cls, @NonNull Class<?> cls2, @NonNull List<Class<?>> list) {
        synchronized (this.registeredResourceClassCache) {
            this.registeredResourceClassCache.put(new MultiClassKey(cls, cls2), list);
        }
    }

    public void clear() {
        synchronized (this.registeredResourceClassCache) {
            this.registeredResourceClassCache.clear();
        }
    }
}
