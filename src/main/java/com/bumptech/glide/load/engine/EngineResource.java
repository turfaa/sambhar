package com.bumptech.glide.load.engine;

import android.os.Looper;
import android.support.annotation.NonNull;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;

class EngineResource<Z> implements Resource<Z> {
    private int acquired;
    private final boolean isCacheable;
    private final boolean isRecyclable;
    private boolean isRecycled;
    private Key key;
    private ResourceListener listener;
    private final Resource<Z> resource;

    interface ResourceListener {
        void onResourceReleased(Key key, EngineResource<?> engineResource);
    }

    EngineResource(Resource<Z> resource, boolean z, boolean z2) {
        this.resource = (Resource) Preconditions.checkNotNull(resource);
        this.isCacheable = z;
        this.isRecyclable = z2;
    }

    /* Access modifiers changed, original: 0000 */
    public void setResourceListener(Key key, ResourceListener resourceListener) {
        this.key = key;
        this.listener = resourceListener;
    }

    /* Access modifiers changed, original: 0000 */
    public Resource<Z> getResource() {
        return this.resource;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isCacheable() {
        return this.isCacheable;
    }

    @NonNull
    public Class<Z> getResourceClass() {
        return this.resource.getResourceClass();
    }

    @NonNull
    public Z get() {
        return this.resource.get();
    }

    public int getSize() {
        return this.resource.getSize();
    }

    public void recycle() {
        if (this.acquired > 0) {
            throw new IllegalStateException("Cannot recycle a resource while it is still acquired");
        } else if (this.isRecycled) {
            throw new IllegalStateException("Cannot recycle a resource that has already been recycled");
        } else {
            this.isRecycled = true;
            if (this.isRecyclable) {
                this.resource.recycle();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void acquire() {
        if (this.isRecycled) {
            throw new IllegalStateException("Cannot acquire a recycled resource");
        } else if (Looper.getMainLooper().equals(Looper.myLooper())) {
            this.acquired++;
        } else {
            throw new IllegalThreadStateException("Must call acquire on the main thread");
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void release() {
        if (this.acquired <= 0) {
            throw new IllegalStateException("Cannot release a recycled or not yet acquired resource");
        } else if (Looper.getMainLooper().equals(Looper.myLooper())) {
            int i = this.acquired - 1;
            this.acquired = i;
            if (i == 0) {
                this.listener.onResourceReleased(this.key, this);
            }
        } else {
            throw new IllegalThreadStateException("Must call release on the main thread");
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EngineResource{isCacheable=");
        stringBuilder.append(this.isCacheable);
        stringBuilder.append(", listener=");
        stringBuilder.append(this.listener);
        stringBuilder.append(", key=");
        stringBuilder.append(this.key);
        stringBuilder.append(", acquired=");
        stringBuilder.append(this.acquired);
        stringBuilder.append(", isRecycled=");
        stringBuilder.append(this.isRecycled);
        stringBuilder.append(", resource=");
        stringBuilder.append(this.resource);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
