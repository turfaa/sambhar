package com.bumptech.glide.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LruCache<T, Y> {
    private final Map<T, Y> cache = new LinkedHashMap(100, 0.75f, true);
    private long currentSize;
    private final long initialMaxSize;
    private long maxSize;

    /* Access modifiers changed, original: protected */
    public int getSize(@Nullable Y y) {
        return 1;
    }

    /* Access modifiers changed, original: protected */
    public void onItemEvicted(@NonNull T t, @Nullable Y y) {
    }

    public LruCache(long j) {
        this.initialMaxSize = j;
        this.maxSize = j;
    }

    public synchronized void setSizeMultiplier(float f) {
        if (f >= 0.0f) {
            this.maxSize = (long) Math.round(((float) this.initialMaxSize) * f);
            evict();
        } else {
            throw new IllegalArgumentException("Multiplier must be >= 0");
        }
    }

    /* Access modifiers changed, original: protected|declared_synchronized */
    public synchronized int getCount() {
        return this.cache.size();
    }

    public synchronized long getMaxSize() {
        return this.maxSize;
    }

    public synchronized long getCurrentSize() {
        return this.currentSize;
    }

    public synchronized boolean contains(@NonNull T t) {
        return this.cache.containsKey(t);
    }

    @Nullable
    public synchronized Y get(@NonNull T t) {
        return this.cache.get(t);
    }

    @Nullable
    public synchronized Y put(@NonNull T t, @Nullable Y y) {
        long size = (long) getSize(y);
        if (size >= this.maxSize) {
            onItemEvicted(t, y);
            return null;
        }
        if (y != null) {
            this.currentSize += size;
        }
        Object put = this.cache.put(t, y);
        if (put != null) {
            this.currentSize -= (long) getSize(put);
            if (!put.equals(y)) {
                onItemEvicted(t, put);
            }
        }
        evict();
        return put;
    }

    @Nullable
    public synchronized Y remove(@NonNull T t) {
        Object remove;
        remove = this.cache.remove(t);
        if (remove != null) {
            this.currentSize -= (long) getSize(remove);
        }
        return remove;
    }

    public void clearMemory() {
        trimToSize(0);
    }

    /* Access modifiers changed, original: protected|declared_synchronized */
    public synchronized void trimToSize(long j) {
        while (this.currentSize > j) {
            Iterator it = this.cache.entrySet().iterator();
            Entry entry = (Entry) it.next();
            Object value = entry.getValue();
            this.currentSize -= (long) getSize(value);
            Object key = entry.getKey();
            it.remove();
            onItemEvicted(key, value);
        }
    }

    private void evict() {
        trimToSize(this.maxSize);
    }
}
