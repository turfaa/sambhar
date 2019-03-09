package com.bumptech.glide.util.pool;

import android.support.annotation.NonNull;
import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SimplePool;
import android.support.v4.util.Pools.SynchronizedPool;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class FactoryPools {
    private static final int DEFAULT_POOL_SIZE = 20;
    private static final Resetter<Object> EMPTY_RESETTER = new Resetter<Object>() {
        public void reset(@NonNull Object obj) {
        }
    };
    private static final String TAG = "FactoryPools";

    public interface Factory<T> {
        T create();
    }

    public interface Poolable {
        @NonNull
        StateVerifier getVerifier();
    }

    public interface Resetter<T> {
        void reset(@NonNull T t);
    }

    private static final class FactoryPool<T> implements Pool<T> {
        private final Factory<T> factory;
        private final Pool<T> pool;
        private final Resetter<T> resetter;

        FactoryPool(@NonNull Pool<T> pool, @NonNull Factory<T> factory, @NonNull Resetter<T> resetter) {
            this.pool = pool;
            this.factory = factory;
            this.resetter = resetter;
        }

        public T acquire() {
            T acquire = this.pool.acquire();
            if (acquire == null) {
                acquire = this.factory.create();
                if (Log.isLoggable(FactoryPools.TAG, 2)) {
                    String str = FactoryPools.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Created new ");
                    stringBuilder.append(acquire.getClass());
                    Log.v(str, stringBuilder.toString());
                }
            }
            if (acquire instanceof Poolable) {
                ((Poolable) acquire).getVerifier().setRecycled(false);
            }
            return acquire;
        }

        public boolean release(@NonNull T t) {
            if (t instanceof Poolable) {
                ((Poolable) t).getVerifier().setRecycled(true);
            }
            this.resetter.reset(t);
            return this.pool.release(t);
        }
    }

    private FactoryPools() {
    }

    @NonNull
    public static <T extends Poolable> Pool<T> simple(int i, @NonNull Factory<T> factory) {
        return build(new SimplePool(i), factory);
    }

    @NonNull
    public static <T extends Poolable> Pool<T> threadSafe(int i, @NonNull Factory<T> factory) {
        return build(new SynchronizedPool(i), factory);
    }

    @NonNull
    public static <T> Pool<List<T>> threadSafeList() {
        return threadSafeList(20);
    }

    @NonNull
    public static <T> Pool<List<T>> threadSafeList(int i) {
        return build(new SynchronizedPool(i), new Factory<List<T>>() {
            @NonNull
            public List<T> create() {
                return new ArrayList();
            }
        }, new Resetter<List<T>>() {
            public void reset(@NonNull List<T> list) {
                list.clear();
            }
        });
    }

    @NonNull
    private static <T extends Poolable> Pool<T> build(@NonNull Pool<T> pool, @NonNull Factory<T> factory) {
        return build(pool, factory, emptyResetter());
    }

    @NonNull
    private static <T> Pool<T> build(@NonNull Pool<T> pool, @NonNull Factory<T> factory, @NonNull Resetter<T> resetter) {
        return new FactoryPool(pool, factory, resetter);
    }

    @NonNull
    private static <T> Resetter<T> emptyResetter() {
        return EMPTY_RESETTER;
    }
}
