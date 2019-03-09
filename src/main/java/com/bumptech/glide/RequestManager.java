package com.bumptech.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.view.View;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.manager.ConnectivityMonitor;
import com.bumptech.glide.manager.ConnectivityMonitor.ConnectivityListener;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.Lifecycle;
import com.bumptech.glide.manager.LifecycleListener;
import com.bumptech.glide.manager.RequestManagerTreeNode;
import com.bumptech.glide.manager.RequestTracker;
import com.bumptech.glide.manager.TargetTracker;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.net.URL;

public class RequestManager implements LifecycleListener, ModelTypes<RequestBuilder<Drawable>> {
    private static final RequestOptions DECODE_TYPE_BITMAP = RequestOptions.decodeTypeOf(Bitmap.class).lock();
    private static final RequestOptions DECODE_TYPE_GIF = RequestOptions.decodeTypeOf(GifDrawable.class).lock();
    private static final RequestOptions DOWNLOAD_ONLY_OPTIONS = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).priority(Priority.LOW).skipMemoryCache(true);
    private final Runnable addSelfToLifecycle;
    private final ConnectivityMonitor connectivityMonitor;
    protected final Context context;
    protected final Glide glide;
    final Lifecycle lifecycle;
    private final Handler mainHandler;
    private RequestOptions requestOptions;
    private final RequestTracker requestTracker;
    private final TargetTracker targetTracker;
    private final RequestManagerTreeNode treeNode;

    private static class RequestManagerConnectivityListener implements ConnectivityListener {
        private final RequestTracker requestTracker;

        RequestManagerConnectivityListener(@NonNull RequestTracker requestTracker) {
            this.requestTracker = requestTracker;
        }

        public void onConnectivityChanged(boolean z) {
            if (z) {
                this.requestTracker.restartRequests();
            }
        }
    }

    private static class ClearTarget extends ViewTarget<View, Object> {
        public void onResourceReady(@NonNull Object obj, @Nullable Transition<? super Object> transition) {
        }

        ClearTarget(@NonNull View view) {
            super(view);
        }
    }

    public RequestManager(@NonNull Glide glide, @NonNull Lifecycle lifecycle, @NonNull RequestManagerTreeNode requestManagerTreeNode, @NonNull Context context) {
        this(glide, lifecycle, requestManagerTreeNode, new RequestTracker(), glide.getConnectivityMonitorFactory(), context);
    }

    RequestManager(Glide glide, Lifecycle lifecycle, RequestManagerTreeNode requestManagerTreeNode, RequestTracker requestTracker, ConnectivityMonitorFactory connectivityMonitorFactory, Context context) {
        this.targetTracker = new TargetTracker();
        this.addSelfToLifecycle = new Runnable() {
            public void run() {
                RequestManager.this.lifecycle.addListener(RequestManager.this);
            }
        };
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.glide = glide;
        this.lifecycle = lifecycle;
        this.treeNode = requestManagerTreeNode;
        this.requestTracker = requestTracker;
        this.context = context;
        this.connectivityMonitor = connectivityMonitorFactory.build(context.getApplicationContext(), new RequestManagerConnectivityListener(requestTracker));
        if (Util.isOnBackgroundThread()) {
            this.mainHandler.post(this.addSelfToLifecycle);
        } else {
            lifecycle.addListener(this);
        }
        lifecycle.addListener(this.connectivityMonitor);
        setRequestOptions(glide.getGlideContext().getDefaultRequestOptions());
        glide.registerRequestManager(this);
    }

    /* Access modifiers changed, original: protected */
    public void setRequestOptions(@NonNull RequestOptions requestOptions) {
        this.requestOptions = requestOptions.clone().autoClone();
    }

    private void updateRequestOptions(@NonNull RequestOptions requestOptions) {
        this.requestOptions = this.requestOptions.apply(requestOptions);
    }

    @NonNull
    public RequestManager applyDefaultRequestOptions(@NonNull RequestOptions requestOptions) {
        updateRequestOptions(requestOptions);
        return this;
    }

    @NonNull
    public RequestManager setDefaultRequestOptions(@NonNull RequestOptions requestOptions) {
        setRequestOptions(requestOptions);
        return this;
    }

    public boolean isPaused() {
        Util.assertMainThread();
        return this.requestTracker.isPaused();
    }

    public void pauseRequests() {
        Util.assertMainThread();
        this.requestTracker.pauseRequests();
    }

    public void pauseAllRequests() {
        Util.assertMainThread();
        this.requestTracker.pauseAllRequests();
    }

    public void pauseRequestsRecursive() {
        Util.assertMainThread();
        pauseRequests();
        for (RequestManager pauseRequests : this.treeNode.getDescendants()) {
            pauseRequests.pauseRequests();
        }
    }

    public void resumeRequests() {
        Util.assertMainThread();
        this.requestTracker.resumeRequests();
    }

    public void resumeRequestsRecursive() {
        Util.assertMainThread();
        resumeRequests();
        for (RequestManager resumeRequests : this.treeNode.getDescendants()) {
            resumeRequests.resumeRequests();
        }
    }

    public void onStart() {
        resumeRequests();
        this.targetTracker.onStart();
    }

    public void onStop() {
        pauseRequests();
        this.targetTracker.onStop();
    }

    public void onDestroy() {
        this.targetTracker.onDestroy();
        for (Target clear : this.targetTracker.getAll()) {
            clear(clear);
        }
        this.targetTracker.clear();
        this.requestTracker.clearRequests();
        this.lifecycle.removeListener(this);
        this.lifecycle.removeListener(this.connectivityMonitor);
        this.mainHandler.removeCallbacks(this.addSelfToLifecycle);
        this.glide.unregisterRequestManager(this);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Bitmap> asBitmap() {
        return as(Bitmap.class).apply(DECODE_TYPE_BITMAP);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<GifDrawable> asGif() {
        return as(GifDrawable.class).apply(DECODE_TYPE_GIF);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> asDrawable() {
        return as(Drawable.class);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable Bitmap bitmap) {
        return asDrawable().load(bitmap);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable Drawable drawable) {
        return asDrawable().load(drawable);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable String str) {
        return asDrawable().load(str);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable Uri uri) {
        return asDrawable().load(uri);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable File file) {
        return asDrawable().load(file);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable @RawRes @DrawableRes Integer num) {
        return asDrawable().load(num);
    }

    @Deprecated
    @CheckResult
    public RequestBuilder<Drawable> load(@Nullable URL url) {
        return asDrawable().load(url);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable byte[] bArr) {
        return asDrawable().load(bArr);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<Drawable> load(@Nullable Object obj) {
        return asDrawable().load(obj);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<File> downloadOnly() {
        return as(File.class).apply(DOWNLOAD_ONLY_OPTIONS);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<File> download(@Nullable Object obj) {
        return downloadOnly().load(obj);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<File> asFile() {
        return as(File.class).apply(RequestOptions.skipMemoryCacheOf(true));
    }

    @CheckResult
    @NonNull
    public <ResourceType> RequestBuilder<ResourceType> as(@NonNull Class<ResourceType> cls) {
        return new RequestBuilder(this.glide, this, cls, this.context);
    }

    public void clear(@NonNull View view) {
        clear(new ClearTarget(view));
    }

    public void clear(@Nullable final Target<?> target) {
        if (target != null) {
            if (Util.isOnMainThread()) {
                untrackOrDelegate(target);
            } else {
                this.mainHandler.post(new Runnable() {
                    public void run() {
                        RequestManager.this.clear(target);
                    }
                });
            }
        }
    }

    private void untrackOrDelegate(@NonNull Target<?> target) {
        if (!untrack(target) && !this.glide.removeFromManagers(target) && target.getRequest() != null) {
            Request request = target.getRequest();
            target.setRequest(null);
            request.clear();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean untrack(@NonNull Target<?> target) {
        Request request = target.getRequest();
        if (request == null) {
            return true;
        }
        if (!this.requestTracker.clearRemoveAndRecycle(request)) {
            return false;
        }
        this.targetTracker.untrack(target);
        target.setRequest(null);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void track(@NonNull Target<?> target, @NonNull Request request) {
        this.targetTracker.track(target);
        this.requestTracker.runRequest(request);
    }

    /* Access modifiers changed, original: 0000 */
    public RequestOptions getDefaultRequestOptions() {
        return this.requestOptions;
    }

    /* Access modifiers changed, original: 0000 */
    @NonNull
    public <T> TransitionOptions<?, T> getDefaultTransitionOptions(Class<T> cls) {
        return this.glide.getGlideContext().getDefaultTransitionOptions(cls);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("{tracker=");
        stringBuilder.append(this.requestTracker);
        stringBuilder.append(", treeNode=");
        stringBuilder.append(this.treeNode);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
