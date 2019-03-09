package com.bumptech.glide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache.Factory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator.Builder;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.DefaultConnectivityMonitorFactory;
import com.bumptech.glide.manager.RequestManagerRetriever;
import com.bumptech.glide.manager.RequestManagerRetriever.RequestManagerFactory;
import com.bumptech.glide.request.RequestOptions;
import java.util.Map;

public final class GlideBuilder {
    private GlideExecutor animationExecutor;
    private ArrayPool arrayPool;
    private BitmapPool bitmapPool;
    private ConnectivityMonitorFactory connectivityMonitorFactory;
    private RequestOptions defaultRequestOptions = new RequestOptions();
    private final Map<Class<?>, TransitionOptions<?, ?>> defaultTransitionOptions = new ArrayMap();
    private GlideExecutor diskCacheExecutor;
    private Factory diskCacheFactory;
    private Engine engine;
    private boolean isActiveResourceRetentionAllowed;
    private int logLevel = 4;
    private MemoryCache memoryCache;
    private MemorySizeCalculator memorySizeCalculator;
    @Nullable
    private RequestManagerFactory requestManagerFactory;
    private GlideExecutor sourceExecutor;

    @NonNull
    public GlideBuilder setBitmapPool(@Nullable BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
        return this;
    }

    @NonNull
    public GlideBuilder setArrayPool(@Nullable ArrayPool arrayPool) {
        this.arrayPool = arrayPool;
        return this;
    }

    @NonNull
    public GlideBuilder setMemoryCache(@Nullable MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    @NonNull
    public GlideBuilder setDiskCache(@Nullable Factory factory) {
        this.diskCacheFactory = factory;
        return this;
    }

    @Deprecated
    public GlideBuilder setResizeExecutor(@Nullable GlideExecutor glideExecutor) {
        return setSourceExecutor(glideExecutor);
    }

    @NonNull
    public GlideBuilder setSourceExecutor(@Nullable GlideExecutor glideExecutor) {
        this.sourceExecutor = glideExecutor;
        return this;
    }

    @NonNull
    public GlideBuilder setDiskCacheExecutor(@Nullable GlideExecutor glideExecutor) {
        this.diskCacheExecutor = glideExecutor;
        return this;
    }

    @NonNull
    public GlideBuilder setAnimationExecutor(@Nullable GlideExecutor glideExecutor) {
        this.animationExecutor = glideExecutor;
        return this;
    }

    @NonNull
    public GlideBuilder setDefaultRequestOptions(@Nullable RequestOptions requestOptions) {
        this.defaultRequestOptions = requestOptions;
        return this;
    }

    @NonNull
    public <T> GlideBuilder setDefaultTransitionOptions(@NonNull Class<T> cls, @Nullable TransitionOptions<?, T> transitionOptions) {
        this.defaultTransitionOptions.put(cls, transitionOptions);
        return this;
    }

    @NonNull
    public GlideBuilder setMemorySizeCalculator(@NonNull Builder builder) {
        return setMemorySizeCalculator(builder.build());
    }

    @NonNull
    public GlideBuilder setMemorySizeCalculator(@Nullable MemorySizeCalculator memorySizeCalculator) {
        this.memorySizeCalculator = memorySizeCalculator;
        return this;
    }

    @NonNull
    public GlideBuilder setConnectivityMonitorFactory(@Nullable ConnectivityMonitorFactory connectivityMonitorFactory) {
        this.connectivityMonitorFactory = connectivityMonitorFactory;
        return this;
    }

    @NonNull
    public GlideBuilder setLogLevel(int i) {
        if (i < 2 || i > 6) {
            throw new IllegalArgumentException("Log level must be one of Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, or Log.ERROR");
        }
        this.logLevel = i;
        return this;
    }

    @NonNull
    public GlideBuilder setIsActiveResourceRetentionAllowed(boolean z) {
        this.isActiveResourceRetentionAllowed = z;
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    public void setRequestManagerFactory(@Nullable RequestManagerFactory requestManagerFactory) {
        this.requestManagerFactory = requestManagerFactory;
    }

    /* Access modifiers changed, original: 0000 */
    public GlideBuilder setEngine(Engine engine) {
        this.engine = engine;
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    @NonNull
    public Glide build(@NonNull Context context) {
        if (this.sourceExecutor == null) {
            this.sourceExecutor = GlideExecutor.newSourceExecutor();
        }
        if (this.diskCacheExecutor == null) {
            this.diskCacheExecutor = GlideExecutor.newDiskCacheExecutor();
        }
        if (this.animationExecutor == null) {
            this.animationExecutor = GlideExecutor.newAnimationExecutor();
        }
        if (this.memorySizeCalculator == null) {
            this.memorySizeCalculator = new Builder(context).build();
        }
        if (this.connectivityMonitorFactory == null) {
            this.connectivityMonitorFactory = new DefaultConnectivityMonitorFactory();
        }
        if (this.bitmapPool == null) {
            int bitmapPoolSize = this.memorySizeCalculator.getBitmapPoolSize();
            if (bitmapPoolSize > 0) {
                this.bitmapPool = new LruBitmapPool((long) bitmapPoolSize);
            } else {
                this.bitmapPool = new BitmapPoolAdapter();
            }
        }
        if (this.arrayPool == null) {
            this.arrayPool = new LruArrayPool(this.memorySizeCalculator.getArrayPoolSizeInBytes());
        }
        if (this.memoryCache == null) {
            this.memoryCache = new LruResourceCache((long) this.memorySizeCalculator.getMemoryCacheSize());
        }
        if (this.diskCacheFactory == null) {
            this.diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }
        if (this.engine == null) {
            this.engine = new Engine(this.memoryCache, this.diskCacheFactory, this.diskCacheExecutor, this.sourceExecutor, GlideExecutor.newUnlimitedSourceExecutor(), GlideExecutor.newAnimationExecutor(), this.isActiveResourceRetentionAllowed);
        }
        return new Glide(context, this.engine, this.memoryCache, this.bitmapPool, this.arrayPool, new RequestManagerRetriever(this.requestManagerFactory), this.connectivityMonitorFactory, this.logLevel, this.defaultRequestOptions.lock(), this.defaultTransitionOptions);
    }
}
