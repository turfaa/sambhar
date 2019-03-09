package com.bumptech.glide.load.engine;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class ActiveResources {
    private static final int MSG_CLEAN_REF = 1;
    @VisibleForTesting
    final Map<Key, ResourceWeakReference> activeEngineResources = new HashMap();
    @Nullable
    private volatile DequeuedResourceCallback cb;
    @Nullable
    private Thread cleanReferenceQueueThread;
    private final boolean isActiveResourceRetentionAllowed;
    private volatile boolean isShutdown;
    private ResourceListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper(), new Callback() {
        public boolean handleMessage(Message message) {
            if (message.what != 1) {
                return false;
            }
            ActiveResources.this.cleanupActiveReference((ResourceWeakReference) message.obj);
            return true;
        }
    });
    @Nullable
    private ReferenceQueue<EngineResource<?>> resourceReferenceQueue;

    @VisibleForTesting
    interface DequeuedResourceCallback {
        void onResourceDequeued();
    }

    @VisibleForTesting
    static final class ResourceWeakReference extends WeakReference<EngineResource<?>> {
        final boolean isCacheable;
        final Key key;
        @Nullable
        Resource<?> resource;

        ResourceWeakReference(@NonNull Key key, @NonNull EngineResource<?> engineResource, @NonNull ReferenceQueue<? super EngineResource<?>> referenceQueue, boolean z) {
            super(engineResource, referenceQueue);
            this.key = (Key) Preconditions.checkNotNull(key);
            Resource resource = (engineResource.isCacheable() && z) ? (Resource) Preconditions.checkNotNull(engineResource.getResource()) : null;
            this.resource = resource;
            this.isCacheable = engineResource.isCacheable();
        }

        /* Access modifiers changed, original: 0000 */
        public void reset() {
            this.resource = null;
            clear();
        }
    }

    ActiveResources(boolean z) {
        this.isActiveResourceRetentionAllowed = z;
    }

    /* Access modifiers changed, original: 0000 */
    public void setListener(ResourceListener resourceListener) {
        this.listener = resourceListener;
    }

    /* Access modifiers changed, original: 0000 */
    public void activate(Key key, EngineResource<?> engineResource) {
        ResourceWeakReference resourceWeakReference = (ResourceWeakReference) this.activeEngineResources.put(key, new ResourceWeakReference(key, engineResource, getReferenceQueue(), this.isActiveResourceRetentionAllowed));
        if (resourceWeakReference != null) {
            resourceWeakReference.reset();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void deactivate(Key key) {
        ResourceWeakReference resourceWeakReference = (ResourceWeakReference) this.activeEngineResources.remove(key);
        if (resourceWeakReference != null) {
            resourceWeakReference.reset();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public EngineResource<?> get(Key key) {
        ResourceWeakReference resourceWeakReference = (ResourceWeakReference) this.activeEngineResources.get(key);
        if (resourceWeakReference == null) {
            return null;
        }
        EngineResource engineResource = (EngineResource) resourceWeakReference.get();
        if (engineResource == null) {
            cleanupActiveReference(resourceWeakReference);
        }
        return engineResource;
    }

    /* Access modifiers changed, original: 0000 */
    public void cleanupActiveReference(@NonNull ResourceWeakReference resourceWeakReference) {
        Util.assertMainThread();
        this.activeEngineResources.remove(resourceWeakReference.key);
        if (resourceWeakReference.isCacheable && resourceWeakReference.resource != null) {
            EngineResource engineResource = new EngineResource(resourceWeakReference.resource, true, false);
            engineResource.setResourceListener(resourceWeakReference.key, this.listener);
            this.listener.onResourceReleased(resourceWeakReference.key, engineResource);
        }
    }

    private ReferenceQueue<EngineResource<?>> getReferenceQueue() {
        if (this.resourceReferenceQueue == null) {
            this.resourceReferenceQueue = new ReferenceQueue();
            this.cleanReferenceQueueThread = new Thread(new Runnable() {
                public void run() {
                    Process.setThreadPriority(10);
                    ActiveResources.this.cleanReferenceQueue();
                }
            }, "glide-active-resources");
            this.cleanReferenceQueueThread.start();
        }
        return this.resourceReferenceQueue;
    }

    /* Access modifiers changed, original: 0000 */
    public void cleanReferenceQueue() {
        while (!this.isShutdown) {
            try {
                this.mainHandler.obtainMessage(1, (ResourceWeakReference) this.resourceReferenceQueue.remove()).sendToTarget();
                DequeuedResourceCallback dequeuedResourceCallback = this.cb;
                if (dequeuedResourceCallback != null) {
                    dequeuedResourceCallback.onResourceDequeued();
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setDequeuedResourceCallback(DequeuedResourceCallback dequeuedResourceCallback) {
        this.cb = dequeuedResourceCallback;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void shutdown() {
        this.isShutdown = true;
        if (this.cleanReferenceQueueThread != null) {
            this.cleanReferenceQueueThread.interrupt();
            try {
                this.cleanReferenceQueueThread.join(TimeUnit.SECONDS.toMillis(5));
                if (this.cleanReferenceQueueThread.isAlive()) {
                    throw new RuntimeException("Failed to join in time");
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
