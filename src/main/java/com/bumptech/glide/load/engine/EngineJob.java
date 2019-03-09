package com.bumptech.glide.load.engine;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pools.Pool;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.util.Util;
import com.bumptech.glide.util.pool.FactoryPools.Poolable;
import com.bumptech.glide.util.pool.StateVerifier;
import java.util.ArrayList;
import java.util.List;

class EngineJob<R> implements Callback<R>, Poolable {
    private static final EngineResourceFactory DEFAULT_FACTORY = new EngineResourceFactory();
    private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper(), new MainThreadCallback());
    private static final int MSG_CANCELLED = 3;
    private static final int MSG_COMPLETE = 1;
    private static final int MSG_EXCEPTION = 2;
    private final GlideExecutor animationExecutor;
    private final List<ResourceCallback> cbs;
    private DataSource dataSource;
    private DecodeJob<R> decodeJob;
    private final GlideExecutor diskCacheExecutor;
    private EngineResource<?> engineResource;
    private final EngineResourceFactory engineResourceFactory;
    private GlideException exception;
    private boolean hasLoadFailed;
    private boolean hasResource;
    private List<ResourceCallback> ignoredCallbacks;
    private boolean isCacheable;
    private volatile boolean isCancelled;
    private Key key;
    private final EngineJobListener listener;
    private boolean onlyRetrieveFromCache;
    private final Pool<EngineJob<?>> pool;
    private Resource<?> resource;
    private final GlideExecutor sourceExecutor;
    private final GlideExecutor sourceUnlimitedExecutor;
    private final StateVerifier stateVerifier;
    private boolean useAnimationPool;
    private boolean useUnlimitedSourceGeneratorPool;

    @VisibleForTesting
    static class EngineResourceFactory {
        EngineResourceFactory() {
        }

        public <R> EngineResource<R> build(Resource<R> resource, boolean z) {
            return new EngineResource(resource, z, true);
        }
    }

    private static class MainThreadCallback implements Callback {
        MainThreadCallback() {
        }

        public boolean handleMessage(Message message) {
            EngineJob engineJob = (EngineJob) message.obj;
            switch (message.what) {
                case 1:
                    engineJob.handleResultOnMainThread();
                    break;
                case 2:
                    engineJob.handleExceptionOnMainThread();
                    break;
                case 3:
                    engineJob.handleCancelledOnMainThread();
                    break;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unrecognized message: ");
                    stringBuilder.append(message.what);
                    throw new IllegalStateException(stringBuilder.toString());
            }
            return true;
        }
    }

    EngineJob(GlideExecutor glideExecutor, GlideExecutor glideExecutor2, GlideExecutor glideExecutor3, GlideExecutor glideExecutor4, EngineJobListener engineJobListener, Pool<EngineJob<?>> pool) {
        this(glideExecutor, glideExecutor2, glideExecutor3, glideExecutor4, engineJobListener, pool, DEFAULT_FACTORY);
    }

    @VisibleForTesting
    EngineJob(GlideExecutor glideExecutor, GlideExecutor glideExecutor2, GlideExecutor glideExecutor3, GlideExecutor glideExecutor4, EngineJobListener engineJobListener, Pool<EngineJob<?>> pool, EngineResourceFactory engineResourceFactory) {
        this.cbs = new ArrayList(2);
        this.stateVerifier = StateVerifier.newInstance();
        this.diskCacheExecutor = glideExecutor;
        this.sourceExecutor = glideExecutor2;
        this.sourceUnlimitedExecutor = glideExecutor3;
        this.animationExecutor = glideExecutor4;
        this.listener = engineJobListener;
        this.pool = pool;
        this.engineResourceFactory = engineResourceFactory;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public EngineJob<R> init(Key key, boolean z, boolean z2, boolean z3, boolean z4) {
        this.key = key;
        this.isCacheable = z;
        this.useUnlimitedSourceGeneratorPool = z2;
        this.useAnimationPool = z3;
        this.onlyRetrieveFromCache = z4;
        return this;
    }

    public void start(DecodeJob<R> decodeJob) {
        GlideExecutor glideExecutor;
        this.decodeJob = decodeJob;
        if (decodeJob.willDecodeFromCache()) {
            glideExecutor = this.diskCacheExecutor;
        } else {
            glideExecutor = getActiveSourceExecutor();
        }
        glideExecutor.execute(decodeJob);
    }

    /* Access modifiers changed, original: 0000 */
    public void addCallback(ResourceCallback resourceCallback) {
        Util.assertMainThread();
        this.stateVerifier.throwIfRecycled();
        if (this.hasResource) {
            resourceCallback.onResourceReady(this.engineResource, this.dataSource);
        } else if (this.hasLoadFailed) {
            resourceCallback.onLoadFailed(this.exception);
        } else {
            this.cbs.add(resourceCallback);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeCallback(ResourceCallback resourceCallback) {
        Util.assertMainThread();
        this.stateVerifier.throwIfRecycled();
        if (this.hasResource || this.hasLoadFailed) {
            addIgnoredCallback(resourceCallback);
            return;
        }
        this.cbs.remove(resourceCallback);
        if (this.cbs.isEmpty()) {
            cancel();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean onlyRetrieveFromCache() {
        return this.onlyRetrieveFromCache;
    }

    private GlideExecutor getActiveSourceExecutor() {
        if (this.useUnlimitedSourceGeneratorPool) {
            return this.sourceUnlimitedExecutor;
        }
        return this.useAnimationPool ? this.animationExecutor : this.sourceExecutor;
    }

    private void addIgnoredCallback(ResourceCallback resourceCallback) {
        if (this.ignoredCallbacks == null) {
            this.ignoredCallbacks = new ArrayList(2);
        }
        if (!this.ignoredCallbacks.contains(resourceCallback)) {
            this.ignoredCallbacks.add(resourceCallback);
        }
    }

    private boolean isInIgnoredCallbacks(ResourceCallback resourceCallback) {
        return this.ignoredCallbacks != null && this.ignoredCallbacks.contains(resourceCallback);
    }

    /* Access modifiers changed, original: 0000 */
    public void cancel() {
        if (!this.hasLoadFailed && !this.hasResource && !this.isCancelled) {
            this.isCancelled = true;
            this.decodeJob.cancel();
            this.listener.onEngineJobCancelled(this, this.key);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /* Access modifiers changed, original: 0000 */
    public void handleResultOnMainThread() {
        this.stateVerifier.throwIfRecycled();
        if (this.isCancelled) {
            this.resource.recycle();
            release(false);
        } else if (this.cbs.isEmpty()) {
            throw new IllegalStateException("Received a resource without any callbacks to notify");
        } else if (this.hasResource) {
            throw new IllegalStateException("Already have resource");
        } else {
            this.engineResource = this.engineResourceFactory.build(this.resource, this.isCacheable);
            this.hasResource = true;
            this.engineResource.acquire();
            this.listener.onEngineJobComplete(this, this.key, this.engineResource);
            int size = this.cbs.size();
            for (int i = 0; i < size; i++) {
                ResourceCallback resourceCallback = (ResourceCallback) this.cbs.get(i);
                if (!isInIgnoredCallbacks(resourceCallback)) {
                    this.engineResource.acquire();
                    resourceCallback.onResourceReady(this.engineResource, this.dataSource);
                }
            }
            this.engineResource.release();
            release(false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void handleCancelledOnMainThread() {
        this.stateVerifier.throwIfRecycled();
        if (this.isCancelled) {
            this.listener.onEngineJobCancelled(this, this.key);
            release(false);
            return;
        }
        throw new IllegalStateException("Not cancelled");
    }

    private void release(boolean z) {
        Util.assertMainThread();
        this.cbs.clear();
        this.key = null;
        this.engineResource = null;
        this.resource = null;
        if (this.ignoredCallbacks != null) {
            this.ignoredCallbacks.clear();
        }
        this.hasLoadFailed = false;
        this.isCancelled = false;
        this.hasResource = false;
        this.decodeJob.release(z);
        this.decodeJob = null;
        this.exception = null;
        this.dataSource = null;
        this.pool.release(this);
    }

    public void onResourceReady(Resource<R> resource, DataSource dataSource) {
        this.resource = resource;
        this.dataSource = dataSource;
        MAIN_THREAD_HANDLER.obtainMessage(1, this).sendToTarget();
    }

    public void onLoadFailed(GlideException glideException) {
        this.exception = glideException;
        MAIN_THREAD_HANDLER.obtainMessage(2, this).sendToTarget();
    }

    public void reschedule(DecodeJob<?> decodeJob) {
        getActiveSourceExecutor().execute(decodeJob);
    }

    /* Access modifiers changed, original: 0000 */
    public void handleExceptionOnMainThread() {
        this.stateVerifier.throwIfRecycled();
        if (this.isCancelled) {
            release(false);
        } else if (this.cbs.isEmpty()) {
            throw new IllegalStateException("Received an exception without any callbacks to notify");
        } else if (this.hasLoadFailed) {
            throw new IllegalStateException("Already failed once");
        } else {
            this.hasLoadFailed = true;
            this.listener.onEngineJobComplete(this, this.key, null);
            for (ResourceCallback resourceCallback : this.cbs) {
                if (!isInIgnoredCallbacks(resourceCallback)) {
                    resourceCallback.onLoadFailed(this.exception);
                }
            }
            release(false);
        }
    }

    @NonNull
    public StateVerifier getVerifier() {
        return this.stateVerifier;
    }
}
