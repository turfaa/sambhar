package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso.Priority;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

abstract class Action<T> {
    boolean cancelled;
    final Drawable errorDrawable;
    final int errorResId;
    final String key;
    final int memoryPolicy;
    final int networkPolicy;
    final boolean noFade;
    final Picasso picasso;
    final Request request;
    final Object tag;
    final WeakReference<T> target;
    boolean willReplay;

    static class RequestWeakReference<M> extends WeakReference<M> {
        final Action action;

        public RequestWeakReference(Action action, M m, ReferenceQueue<? super M> referenceQueue) {
            super(m, referenceQueue);
            this.action = action;
        }
    }

    public abstract void complete(Bitmap bitmap, LoadedFrom loadedFrom);

    public abstract void error();

    Action(Picasso picasso, T t, Request request, int i, int i2, int i3, Drawable drawable, String str, Object obj, boolean z) {
        WeakReference weakReference;
        this.picasso = picasso;
        this.request = request;
        if (t == null) {
            weakReference = null;
        } else {
            weakReference = new RequestWeakReference(this, t, picasso.referenceQueue);
        }
        this.target = weakReference;
        this.memoryPolicy = i;
        this.networkPolicy = i2;
        this.noFade = z;
        this.errorResId = i3;
        this.errorDrawable = drawable;
        this.key = str;
        if (obj == null) {
            obj = this;
        }
        this.tag = obj;
    }

    /* Access modifiers changed, original: 0000 */
    public void cancel() {
        this.cancelled = true;
    }

    /* Access modifiers changed, original: 0000 */
    public Request getRequest() {
        return this.request;
    }

    /* Access modifiers changed, original: 0000 */
    public T getTarget() {
        return this.target == null ? null : this.target.get();
    }

    /* Access modifiers changed, original: 0000 */
    public String getKey() {
        return this.key;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean willReplay() {
        return this.willReplay;
    }

    /* Access modifiers changed, original: 0000 */
    public int getMemoryPolicy() {
        return this.memoryPolicy;
    }

    /* Access modifiers changed, original: 0000 */
    public int getNetworkPolicy() {
        return this.networkPolicy;
    }

    /* Access modifiers changed, original: 0000 */
    public Picasso getPicasso() {
        return this.picasso;
    }

    /* Access modifiers changed, original: 0000 */
    public Priority getPriority() {
        return this.request.priority;
    }

    /* Access modifiers changed, original: 0000 */
    public Object getTag() {
        return this.tag;
    }
}
