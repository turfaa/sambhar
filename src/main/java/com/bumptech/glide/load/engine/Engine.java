package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pools.Pool;
import android.util.Log;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.cache.MemoryCache.ResourceRemovedListener;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import com.bumptech.glide.util.pool.FactoryPools;
import com.bumptech.glide.util.pool.FactoryPools.Factory;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Engine implements EngineJobListener, ResourceRemovedListener, ResourceListener {
    private static final int JOB_POOL_SIZE = 150;
    private static final String TAG = "Engine";
    private static final boolean VERBOSE_IS_LOGGABLE = Log.isLoggable(TAG, 2);
    private final ActiveResources activeResources;
    private final MemoryCache cache;
    private final DecodeJobFactory decodeJobFactory;
    private final LazyDiskCacheProvider diskCacheProvider;
    private final EngineJobFactory engineJobFactory;
    private final Jobs jobs;
    private final EngineKeyFactory keyFactory;
    private final ResourceRecycler resourceRecycler;

    @VisibleForTesting
    static class DecodeJobFactory {
        private int creationOrder;
        final DiskCacheProvider diskCacheProvider;
        final Pool<DecodeJob<?>> pool = FactoryPools.simple(Engine.JOB_POOL_SIZE, new Factory<DecodeJob<?>>() {
            public DecodeJob<?> create() {
                return new DecodeJob(DecodeJobFactory.this.diskCacheProvider, DecodeJobFactory.this.pool);
            }
        });

        DecodeJobFactory(DiskCacheProvider diskCacheProvider) {
            this.diskCacheProvider = diskCacheProvider;
        }

        /* Access modifiers changed, original: 0000 */
        public <R> DecodeJob<R> build(GlideContext glideContext, Object obj, EngineKey engineKey, Key key, int i, int i2, Class<?> cls, Class<R> cls2, Priority priority, DiskCacheStrategy diskCacheStrategy, Map<Class<?>, Transformation<?>> map, boolean z, boolean z2, boolean z3, Options options, Callback<R> callback) {
            GlideContext glideContext2 = glideContext;
            Object obj2 = obj;
            EngineKey engineKey2 = engineKey;
            Key key2 = key;
            int i3 = i;
            int i4 = i2;
            Class<?> cls3 = cls;
            Class<R> cls4 = cls2;
            Priority priority2 = priority;
            DiskCacheStrategy diskCacheStrategy2 = diskCacheStrategy;
            Map<Class<?>, Transformation<?>> map2 = map;
            boolean z4 = z;
            boolean z5 = z2;
            boolean z6 = z3;
            Options options2 = options;
            Callback<R> callback2 = callback;
            DecodeJob decodeJob = (DecodeJob) Preconditions.checkNotNull((DecodeJob) this.pool.acquire());
            int i5 = this.creationOrder;
            int i6 = i5;
            this.creationOrder = i5 + 1;
            return decodeJob.init(glideContext2, obj2, engineKey2, key2, i3, i4, cls3, cls4, priority2, diskCacheStrategy2, map2, z4, z5, z6, options2, callback2, i6);
        }
    }

    @VisibleForTesting
    static class EngineJobFactory {
        final GlideExecutor animationExecutor;
        final GlideExecutor diskCacheExecutor;
        final EngineJobListener listener;
        final Pool<EngineJob<?>> pool = FactoryPools.simple(Engine.JOB_POOL_SIZE, new Factory<EngineJob<?>>() {
            public EngineJob<?> create() {
                return new EngineJob(EngineJobFactory.this.diskCacheExecutor, EngineJobFactory.this.sourceExecutor, EngineJobFactory.this.sourceUnlimitedExecutor, EngineJobFactory.this.animationExecutor, EngineJobFactory.this.listener, EngineJobFactory.this.pool);
            }
        });
        final GlideExecutor sourceExecutor;
        final GlideExecutor sourceUnlimitedExecutor;

        EngineJobFactory(GlideExecutor glideExecutor, GlideExecutor glideExecutor2, GlideExecutor glideExecutor3, GlideExecutor glideExecutor4, EngineJobListener engineJobListener) {
            this.diskCacheExecutor = glideExecutor;
            this.sourceExecutor = glideExecutor2;
            this.sourceUnlimitedExecutor = glideExecutor3;
            this.animationExecutor = glideExecutor4;
            this.listener = engineJobListener;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public void shutdown() {
            shutdownAndAwaitTermination(this.diskCacheExecutor);
            shutdownAndAwaitTermination(this.sourceExecutor);
            shutdownAndAwaitTermination(this.sourceUnlimitedExecutor);
            shutdownAndAwaitTermination(this.animationExecutor);
        }

        /* Access modifiers changed, original: 0000 */
        public <R> EngineJob<R> build(Key key, boolean z, boolean z2, boolean z3, boolean z4) {
            return ((EngineJob) Preconditions.checkNotNull((EngineJob) this.pool.acquire())).init(key, z, z2, z3, z4);
        }

        private static void shutdownAndAwaitTermination(ExecutorService executorService) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        throw new RuntimeException("Failed to shutdown");
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class LoadStatus {
        private final ResourceCallback cb;
        private final EngineJob<?> engineJob;

        LoadStatus(ResourceCallback resourceCallback, EngineJob<?> engineJob) {
            this.cb = resourceCallback;
            this.engineJob = engineJob;
        }

        public void cancel() {
            this.engineJob.removeCallback(this.cb);
        }
    }

    private static class LazyDiskCacheProvider implements DiskCacheProvider {
        private volatile DiskCache diskCache;
        private final DiskCache.Factory factory;

        LazyDiskCacheProvider(DiskCache.Factory factory) {
            this.factory = factory;
        }

        /* Access modifiers changed, original: declared_synchronized */
        @VisibleForTesting
        public synchronized void clearDiskCacheIfCreated() {
            if (this.diskCache != null) {
                this.diskCache.clear();
            }
        }

        public DiskCache getDiskCache() {
            if (this.diskCache == null) {
                synchronized (this) {
                    if (this.diskCache == null) {
                        this.diskCache = this.factory.build();
                    }
                    if (this.diskCache == null) {
                        this.diskCache = new DiskCacheAdapter();
                    }
                }
            }
            return this.diskCache;
        }
    }

    public Engine(MemoryCache memoryCache, DiskCache.Factory factory, GlideExecutor glideExecutor, GlideExecutor glideExecutor2, GlideExecutor glideExecutor3, GlideExecutor glideExecutor4, boolean z) {
        this(memoryCache, factory, glideExecutor, glideExecutor2, glideExecutor3, glideExecutor4, null, null, null, null, null, null, z);
    }

    @VisibleForTesting
    Engine(MemoryCache memoryCache, DiskCache.Factory factory, GlideExecutor glideExecutor, GlideExecutor glideExecutor2, GlideExecutor glideExecutor3, GlideExecutor glideExecutor4, Jobs jobs, EngineKeyFactory engineKeyFactory, ActiveResources activeResources, EngineJobFactory engineJobFactory, DecodeJobFactory decodeJobFactory, ResourceRecycler resourceRecycler, boolean z) {
        EngineJobFactory engineJobFactory2;
        this.cache = memoryCache;
        DiskCache.Factory factory2 = factory;
        this.diskCacheProvider = new LazyDiskCacheProvider(factory);
        ActiveResources activeResources2 = activeResources == null ? new ActiveResources(z) : activeResources;
        this.activeResources = activeResources2;
        activeResources2.setListener(this);
        this.keyFactory = engineKeyFactory == null ? new EngineKeyFactory() : engineKeyFactory;
        this.jobs = jobs == null ? new Jobs() : jobs;
        if (engineJobFactory == null) {
            EngineJobFactory engineJobFactory3 = new EngineJobFactory(glideExecutor, glideExecutor2, glideExecutor3, glideExecutor4, this);
        } else {
            engineJobFactory2 = engineJobFactory;
        }
        this.engineJobFactory = engineJobFactory2;
        this.decodeJobFactory = decodeJobFactory == null ? new DecodeJobFactory(this.diskCacheProvider) : decodeJobFactory;
        this.resourceRecycler = resourceRecycler == null ? new ResourceRecycler() : resourceRecycler;
        memoryCache.setResourceRemovedListener(this);
    }

    public <R> LoadStatus load(GlideContext glideContext, Object obj, Key key, int i, int i2, Class<?> cls, Class<R> cls2, Priority priority, DiskCacheStrategy diskCacheStrategy, Map<Class<?>, Transformation<?>> map, boolean z, boolean z2, Options options, boolean z3, boolean z4, boolean z5, boolean z6, ResourceCallback resourceCallback) {
        boolean z7 = z3;
        ResourceCallback resourceCallback2 = resourceCallback;
        Util.assertMainThread();
        long logTime = VERBOSE_IS_LOGGABLE ? LogTime.getLogTime() : 0;
        EngineKey buildKey = this.keyFactory.buildKey(obj, key, i, i2, map, cls, cls2, options);
        EngineResource loadFromActiveResources = loadFromActiveResources(buildKey, z7);
        if (loadFromActiveResources != null) {
            resourceCallback2.onResourceReady(loadFromActiveResources, DataSource.MEMORY_CACHE);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from active resources", logTime, buildKey);
            }
            return null;
        }
        loadFromActiveResources = loadFromCache(buildKey, z7);
        if (loadFromActiveResources != null) {
            resourceCallback2.onResourceReady(loadFromActiveResources, DataSource.MEMORY_CACHE);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from cache", logTime, buildKey);
            }
            return null;
        }
        EngineJob engineJob = this.jobs.get(buildKey, z6);
        if (engineJob != null) {
            engineJob.addCallback(resourceCallback2);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Added to existing load", logTime, buildKey);
            }
            return new LoadStatus(resourceCallback2, engineJob);
        }
        engineJob = this.engineJobFactory.build(buildKey, z3, z4, z5, z6);
        DecodeJob build = this.decodeJobFactory.build(glideContext, obj, buildKey, key, i, i2, cls, cls2, priority, diskCacheStrategy, map, z, z2, z6, options, engineJob);
        this.jobs.put(buildKey, engineJob);
        engineJob.addCallback(resourceCallback2);
        engineJob.start(build);
        if (VERBOSE_IS_LOGGABLE) {
            logWithTimeAndKey("Started new load", logTime, buildKey);
        }
        return new LoadStatus(resourceCallback2, engineJob);
    }

    private static void logWithTimeAndKey(String str, long j, Key key) {
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(" in ");
        stringBuilder.append(LogTime.getElapsedMillis(j));
        stringBuilder.append("ms, key: ");
        stringBuilder.append(key);
        Log.v(str2, stringBuilder.toString());
    }

    @Nullable
    private EngineResource<?> loadFromActiveResources(Key key, boolean z) {
        if (!z) {
            return null;
        }
        EngineResource engineResource = this.activeResources.get(key);
        if (engineResource != null) {
            engineResource.acquire();
        }
        return engineResource;
    }

    private EngineResource<?> loadFromCache(Key key, boolean z) {
        if (!z) {
            return null;
        }
        EngineResource engineResourceFromCache = getEngineResourceFromCache(key);
        if (engineResourceFromCache != null) {
            engineResourceFromCache.acquire();
            this.activeResources.activate(key, engineResourceFromCache);
        }
        return engineResourceFromCache;
    }

    private EngineResource<?> getEngineResourceFromCache(Key key) {
        Resource remove = this.cache.remove(key);
        if (remove == null) {
            return null;
        }
        if (remove instanceof EngineResource) {
            return (EngineResource) remove;
        }
        return new EngineResource(remove, true, true);
    }

    public void release(Resource<?> resource) {
        Util.assertMainThread();
        if (resource instanceof EngineResource) {
            ((EngineResource) resource).release();
            return;
        }
        throw new IllegalArgumentException("Cannot release anything but an EngineResource");
    }

    public void onEngineJobComplete(EngineJob<?> engineJob, Key key, EngineResource<?> engineResource) {
        Util.assertMainThread();
        if (engineResource != null) {
            engineResource.setResourceListener(key, this);
            if (engineResource.isCacheable()) {
                this.activeResources.activate(key, engineResource);
            }
        }
        this.jobs.removeIfCurrent(key, engineJob);
    }

    public void onEngineJobCancelled(EngineJob<?> engineJob, Key key) {
        Util.assertMainThread();
        this.jobs.removeIfCurrent(key, engineJob);
    }

    public void onResourceRemoved(@NonNull Resource<?> resource) {
        Util.assertMainThread();
        this.resourceRecycler.recycle(resource);
    }

    public void onResourceReleased(Key key, EngineResource<?> engineResource) {
        Util.assertMainThread();
        this.activeResources.deactivate(key);
        if (engineResource.isCacheable()) {
            this.cache.put(key, engineResource);
        } else {
            this.resourceRecycler.recycle(engineResource);
        }
    }

    public void clearDiskCache() {
        this.diskCacheProvider.getDiskCache().clear();
    }

    @VisibleForTesting
    public void shutdown() {
        this.engineJobFactory.shutdown();
        this.diskCacheProvider.clearDiskCacheIfCreated();
        this.activeResources.shutdown();
    }
}
