package com.bumptech.glide.request;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools.Pool;
import android.util.Log;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.Engine.LoadStatus;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.drawable.DrawableDecoderCompat;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.TransitionFactory;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.Util;
import com.bumptech.glide.util.pool.FactoryPools;
import com.bumptech.glide.util.pool.FactoryPools.Factory;
import com.bumptech.glide.util.pool.FactoryPools.Poolable;
import com.bumptech.glide.util.pool.StateVerifier;
import java.util.List;

public final class SingleRequest<R> implements Request, SizeReadyCallback, ResourceCallback, Poolable {
    private static final String GLIDE_TAG = "Glide";
    private static final boolean IS_VERBOSE_LOGGABLE = Log.isLoggable(TAG, 2);
    private static final Pool<SingleRequest<?>> POOL = FactoryPools.simple(150, new Factory<SingleRequest<?>>() {
        public SingleRequest<?> create() {
            return new SingleRequest();
        }
    });
    private static final String TAG = "Request";
    private TransitionFactory<? super R> animationFactory;
    private Context context;
    private Engine engine;
    private Drawable errorDrawable;
    private Drawable fallbackDrawable;
    private GlideContext glideContext;
    private int height;
    private boolean isCallingCallbacks;
    private LoadStatus loadStatus;
    @Nullable
    private Object model;
    private int overrideHeight;
    private int overrideWidth;
    private Drawable placeholderDrawable;
    private Priority priority;
    private RequestCoordinator requestCoordinator;
    @Nullable
    private List<RequestListener<R>> requestListeners;
    private RequestOptions requestOptions;
    private Resource<R> resource;
    private long startTime;
    private final StateVerifier stateVerifier;
    private Status status;
    @Nullable
    private final String tag;
    private Target<R> target;
    @Nullable
    private RequestListener<R> targetListener;
    private Class<R> transcodeClass;
    private int width;

    private enum Status {
        PENDING,
        RUNNING,
        WAITING_FOR_SIZE,
        COMPLETE,
        FAILED,
        CLEARED
    }

    public static <R> SingleRequest<R> obtain(Context context, GlideContext glideContext, Object obj, Class<R> cls, RequestOptions requestOptions, int i, int i2, Priority priority, Target<R> target, RequestListener<R> requestListener, @Nullable List<RequestListener<R>> list, RequestCoordinator requestCoordinator, Engine engine, TransitionFactory<? super R> transitionFactory) {
        SingleRequest<R> singleRequest = (SingleRequest) POOL.acquire();
        if (singleRequest == null) {
            singleRequest = new SingleRequest();
        }
        singleRequest.init(context, glideContext, obj, cls, requestOptions, i, i2, priority, target, requestListener, list, requestCoordinator, engine, transitionFactory);
        return singleRequest;
    }

    SingleRequest() {
        this.tag = IS_VERBOSE_LOGGABLE ? String.valueOf(super.hashCode()) : null;
        this.stateVerifier = StateVerifier.newInstance();
    }

    private void init(Context context, GlideContext glideContext, Object obj, Class<R> cls, RequestOptions requestOptions, int i, int i2, Priority priority, Target<R> target, RequestListener<R> requestListener, @Nullable List<RequestListener<R>> list, RequestCoordinator requestCoordinator, Engine engine, TransitionFactory<? super R> transitionFactory) {
        this.context = context;
        this.glideContext = glideContext;
        this.model = obj;
        this.transcodeClass = cls;
        this.requestOptions = requestOptions;
        this.overrideWidth = i;
        this.overrideHeight = i2;
        this.priority = priority;
        this.target = target;
        this.targetListener = requestListener;
        this.requestListeners = list;
        this.requestCoordinator = requestCoordinator;
        this.engine = engine;
        this.animationFactory = transitionFactory;
        this.status = Status.PENDING;
    }

    @NonNull
    public StateVerifier getVerifier() {
        return this.stateVerifier;
    }

    public void recycle() {
        assertNotCallingCallbacks();
        this.context = null;
        this.glideContext = null;
        this.model = null;
        this.transcodeClass = null;
        this.requestOptions = null;
        this.overrideWidth = -1;
        this.overrideHeight = -1;
        this.target = null;
        this.requestListeners = null;
        this.targetListener = null;
        this.requestCoordinator = null;
        this.animationFactory = null;
        this.loadStatus = null;
        this.errorDrawable = null;
        this.placeholderDrawable = null;
        this.fallbackDrawable = null;
        this.width = -1;
        this.height = -1;
        POOL.release(this);
    }

    public void begin() {
        assertNotCallingCallbacks();
        this.stateVerifier.throwIfRecycled();
        this.startTime = LogTime.getLogTime();
        if (this.model == null) {
            if (Util.isValidDimensions(this.overrideWidth, this.overrideHeight)) {
                this.width = this.overrideWidth;
                this.height = this.overrideHeight;
            }
            onLoadFailed(new GlideException("Received null model"), getFallbackDrawable() == null ? 5 : 3);
        } else if (this.status == Status.RUNNING) {
            throw new IllegalArgumentException("Cannot restart a running request");
        } else if (this.status == Status.COMPLETE) {
            onResourceReady(this.resource, DataSource.MEMORY_CACHE);
        } else {
            this.status = Status.WAITING_FOR_SIZE;
            if (Util.isValidDimensions(this.overrideWidth, this.overrideHeight)) {
                onSizeReady(this.overrideWidth, this.overrideHeight);
            } else {
                this.target.getSize(this);
            }
            if ((this.status == Status.RUNNING || this.status == Status.WAITING_FOR_SIZE) && canNotifyStatusChanged()) {
                this.target.onLoadStarted(getPlaceholderDrawable());
            }
            if (IS_VERBOSE_LOGGABLE) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("finished run method in ");
                stringBuilder.append(LogTime.getElapsedMillis(this.startTime));
                logV(stringBuilder.toString());
            }
        }
    }

    private void cancel() {
        assertNotCallingCallbacks();
        this.stateVerifier.throwIfRecycled();
        this.target.removeCallback(this);
        if (this.loadStatus != null) {
            this.loadStatus.cancel();
            this.loadStatus = null;
        }
    }

    private void assertNotCallingCallbacks() {
        if (this.isCallingCallbacks) {
            throw new IllegalStateException("You can't start or clear loads in RequestListener or Target callbacks. If you're trying to start a fallback request when a load fails, use RequestBuilder#error(RequestBuilder). Otherwise consider posting your into() or clear() calls to the main thread using a Handler instead.");
        }
    }

    public void clear() {
        Util.assertMainThread();
        assertNotCallingCallbacks();
        this.stateVerifier.throwIfRecycled();
        if (this.status != Status.CLEARED) {
            cancel();
            if (this.resource != null) {
                releaseResource(this.resource);
            }
            if (canNotifyCleared()) {
                this.target.onLoadCleared(getPlaceholderDrawable());
            }
            this.status = Status.CLEARED;
        }
    }

    private void releaseResource(Resource<?> resource) {
        this.engine.release(resource);
        this.resource = null;
    }

    public boolean isRunning() {
        return this.status == Status.RUNNING || this.status == Status.WAITING_FOR_SIZE;
    }

    public boolean isComplete() {
        return this.status == Status.COMPLETE;
    }

    public boolean isResourceSet() {
        return isComplete();
    }

    public boolean isCleared() {
        return this.status == Status.CLEARED;
    }

    public boolean isFailed() {
        return this.status == Status.FAILED;
    }

    private Drawable getErrorDrawable() {
        if (this.errorDrawable == null) {
            this.errorDrawable = this.requestOptions.getErrorPlaceholder();
            if (this.errorDrawable == null && this.requestOptions.getErrorId() > 0) {
                this.errorDrawable = loadDrawable(this.requestOptions.getErrorId());
            }
        }
        return this.errorDrawable;
    }

    private Drawable getPlaceholderDrawable() {
        if (this.placeholderDrawable == null) {
            this.placeholderDrawable = this.requestOptions.getPlaceholderDrawable();
            if (this.placeholderDrawable == null && this.requestOptions.getPlaceholderId() > 0) {
                this.placeholderDrawable = loadDrawable(this.requestOptions.getPlaceholderId());
            }
        }
        return this.placeholderDrawable;
    }

    private Drawable getFallbackDrawable() {
        if (this.fallbackDrawable == null) {
            this.fallbackDrawable = this.requestOptions.getFallbackDrawable();
            if (this.fallbackDrawable == null && this.requestOptions.getFallbackId() > 0) {
                this.fallbackDrawable = loadDrawable(this.requestOptions.getFallbackId());
            }
        }
        return this.fallbackDrawable;
    }

    private Drawable loadDrawable(@DrawableRes int i) {
        return DrawableDecoderCompat.getDrawable(this.glideContext, i, this.requestOptions.getTheme() != null ? this.requestOptions.getTheme() : this.context.getTheme());
    }

    private void setErrorPlaceholder() {
        if (canNotifyStatusChanged()) {
            Drawable drawable = null;
            if (this.model == null) {
                drawable = getFallbackDrawable();
            }
            if (drawable == null) {
                drawable = getErrorDrawable();
            }
            if (drawable == null) {
                drawable = getPlaceholderDrawable();
            }
            this.target.onLoadFailed(drawable);
        }
    }

    public void onSizeReady(int i, int i2) {
        StringBuilder stringBuilder;
        this.stateVerifier.throwIfRecycled();
        if (IS_VERBOSE_LOGGABLE) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Got onSizeReady in ");
            stringBuilder.append(LogTime.getElapsedMillis(this.startTime));
            logV(stringBuilder.toString());
        }
        if (this.status == Status.WAITING_FOR_SIZE) {
            this.status = Status.RUNNING;
            float sizeMultiplier = this.requestOptions.getSizeMultiplier();
            this.width = maybeApplySizeMultiplier(i, sizeMultiplier);
            this.height = maybeApplySizeMultiplier(i2, sizeMultiplier);
            if (IS_VERBOSE_LOGGABLE) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("finished setup for calling load in ");
                stringBuilder.append(LogTime.getElapsedMillis(this.startTime));
                logV(stringBuilder.toString());
            }
            Engine engine = this.engine;
            GlideContext glideContext = this.glideContext;
            LoadStatus load = engine.load(glideContext, this.model, this.requestOptions.getSignature(), this.width, this.height, this.requestOptions.getResourceClass(), this.transcodeClass, this.priority, this.requestOptions.getDiskCacheStrategy(), this.requestOptions.getTransformations(), this.requestOptions.isTransformationRequired(), this.requestOptions.isScaleOnlyOrNoTransform(), this.requestOptions.getOptions(), this.requestOptions.isMemoryCacheable(), this.requestOptions.getUseUnlimitedSourceGeneratorsPool(), this.requestOptions.getUseAnimationPool(), this.requestOptions.getOnlyRetrieveFromCache(), this);
            this.loadStatus = load;
            if (this.status != Status.RUNNING) {
                this.loadStatus = null;
            }
            if (IS_VERBOSE_LOGGABLE) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("finished onSizeReady in ");
                stringBuilder.append(LogTime.getElapsedMillis(this.startTime));
                logV(stringBuilder.toString());
            }
        }
    }

    private static int maybeApplySizeMultiplier(int i, float f) {
        return i == Integer.MIN_VALUE ? i : Math.round(f * ((float) i));
    }

    private boolean canSetResource() {
        return this.requestCoordinator == null || this.requestCoordinator.canSetImage(this);
    }

    private boolean canNotifyCleared() {
        return this.requestCoordinator == null || this.requestCoordinator.canNotifyCleared(this);
    }

    private boolean canNotifyStatusChanged() {
        return this.requestCoordinator == null || this.requestCoordinator.canNotifyStatusChanged(this);
    }

    private boolean isFirstReadyResource() {
        return this.requestCoordinator == null || !this.requestCoordinator.isAnyResourceSet();
    }

    private void notifyLoadSuccess() {
        if (this.requestCoordinator != null) {
            this.requestCoordinator.onRequestSuccess(this);
        }
    }

    private void notifyLoadFailed() {
        if (this.requestCoordinator != null) {
            this.requestCoordinator.onRequestFailed(this);
        }
    }

    public void onResourceReady(Resource<?> resource, DataSource dataSource) {
        this.stateVerifier.throwIfRecycled();
        this.loadStatus = null;
        if (resource == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Expected to receive a Resource<R> with an object of ");
            stringBuilder.append(this.transcodeClass);
            stringBuilder.append(" inside, but instead got null.");
            onLoadFailed(new GlideException(stringBuilder.toString()));
            return;
        }
        Object obj = resource.get();
        if (obj == null || !this.transcodeClass.isAssignableFrom(obj.getClass())) {
            releaseResource(resource);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Expected to receive an object of ");
            stringBuilder2.append(this.transcodeClass);
            stringBuilder2.append(" but instead got ");
            stringBuilder2.append(obj != null ? obj.getClass() : "");
            stringBuilder2.append("{");
            stringBuilder2.append(obj);
            stringBuilder2.append("} inside Resource{");
            stringBuilder2.append(resource);
            stringBuilder2.append("}.");
            stringBuilder2.append(obj != null ? "" : " To indicate failure return a null Resource object, rather than a Resource object containing null data.");
            onLoadFailed(new GlideException(stringBuilder2.toString()));
        } else if (canSetResource()) {
            onResourceReady(resource, obj, dataSource);
        } else {
            releaseResource(resource);
            this.status = Status.COMPLETE;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00ab A:{Catch:{ all -> 0x00bc }} */
    private void onResourceReady(com.bumptech.glide.load.engine.Resource<R> r11, R r12, com.bumptech.glide.load.DataSource r13) {
        /*
        r10 = this;
        r6 = r10.isFirstReadyResource();
        r0 = com.bumptech.glide.request.SingleRequest.Status.COMPLETE;
        r10.status = r0;
        r10.resource = r11;
        r11 = r10.glideContext;
        r11 = r11.getLogLevel();
        r0 = 3;
        if (r11 > r0) goto L_0x006a;
    L_0x0013:
        r11 = "Glide";
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "Finished loading ";
        r0.append(r1);
        r1 = r12.getClass();
        r1 = r1.getSimpleName();
        r0.append(r1);
        r1 = " from ";
        r0.append(r1);
        r0.append(r13);
        r1 = " for ";
        r0.append(r1);
        r1 = r10.model;
        r0.append(r1);
        r1 = " with size [";
        r0.append(r1);
        r1 = r10.width;
        r0.append(r1);
        r1 = "x";
        r0.append(r1);
        r1 = r10.height;
        r0.append(r1);
        r1 = "] in ";
        r0.append(r1);
        r1 = r10.startTime;
        r1 = com.bumptech.glide.util.LogTime.getElapsedMillis(r1);
        r0.append(r1);
        r1 = " ms";
        r0.append(r1);
        r0 = r0.toString();
        android.util.Log.d(r11, r0);
    L_0x006a:
        r11 = 1;
        r10.isCallingCallbacks = r11;
        r7 = 0;
        r0 = r10.requestListeners;	 Catch:{ all -> 0x00bc }
        if (r0 == 0) goto L_0x0092;
    L_0x0072:
        r0 = r10.requestListeners;	 Catch:{ all -> 0x00bc }
        r8 = r0.iterator();	 Catch:{ all -> 0x00bc }
        r9 = 0;
    L_0x0079:
        r0 = r8.hasNext();	 Catch:{ all -> 0x00bc }
        if (r0 == 0) goto L_0x0093;
    L_0x007f:
        r0 = r8.next();	 Catch:{ all -> 0x00bc }
        r0 = (com.bumptech.glide.request.RequestListener) r0;	 Catch:{ all -> 0x00bc }
        r2 = r10.model;	 Catch:{ all -> 0x00bc }
        r3 = r10.target;	 Catch:{ all -> 0x00bc }
        r1 = r12;
        r4 = r13;
        r5 = r6;
        r0 = r0.onResourceReady(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00bc }
        r9 = r9 | r0;
        goto L_0x0079;
    L_0x0092:
        r9 = 0;
    L_0x0093:
        r0 = r10.targetListener;	 Catch:{ all -> 0x00bc }
        if (r0 == 0) goto L_0x00a7;
    L_0x0097:
        r0 = r10.targetListener;	 Catch:{ all -> 0x00bc }
        r2 = r10.model;	 Catch:{ all -> 0x00bc }
        r3 = r10.target;	 Catch:{ all -> 0x00bc }
        r1 = r12;
        r4 = r13;
        r5 = r6;
        r0 = r0.onResourceReady(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00bc }
        if (r0 == 0) goto L_0x00a7;
    L_0x00a6:
        goto L_0x00a8;
    L_0x00a7:
        r11 = 0;
    L_0x00a8:
        r11 = r11 | r9;
        if (r11 != 0) goto L_0x00b6;
    L_0x00ab:
        r11 = r10.animationFactory;	 Catch:{ all -> 0x00bc }
        r11 = r11.build(r13, r6);	 Catch:{ all -> 0x00bc }
        r13 = r10.target;	 Catch:{ all -> 0x00bc }
        r13.onResourceReady(r12, r11);	 Catch:{ all -> 0x00bc }
    L_0x00b6:
        r10.isCallingCallbacks = r7;
        r10.notifyLoadSuccess();
        return;
    L_0x00bc:
        r11 = move-exception;
        r10.isCallingCallbacks = r7;
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.request.SingleRequest.onResourceReady(com.bumptech.glide.load.engine.Resource, java.lang.Object, com.bumptech.glide.load.DataSource):void");
    }

    public void onLoadFailed(GlideException glideException) {
        onLoadFailed(glideException, 5);
    }

    private void onLoadFailed(GlideException glideException, int i) {
        this.stateVerifier.throwIfRecycled();
        int logLevel = this.glideContext.getLogLevel();
        if (logLevel <= i) {
            String str = GLIDE_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Load failed for ");
            stringBuilder.append(this.model);
            stringBuilder.append(" with size [");
            stringBuilder.append(this.width);
            stringBuilder.append("x");
            stringBuilder.append(this.height);
            stringBuilder.append("]");
            Log.w(str, stringBuilder.toString(), glideException);
            if (logLevel <= 4) {
                glideException.logRootCauses(GLIDE_TAG);
            }
        }
        this.loadStatus = null;
        this.status = Status.FAILED;
        i = 1;
        this.isCallingCallbacks = true;
        try {
            int i2;
            if (this.requestListeners != null) {
                i2 = 0;
                for (RequestListener onLoadFailed : this.requestListeners) {
                    i2 |= onLoadFailed.onLoadFailed(glideException, this.model, this.target, isFirstReadyResource());
                }
            } else {
                i2 = 0;
            }
            if (this.targetListener == null || !this.targetListener.onLoadFailed(glideException, this.model, this.target, isFirstReadyResource())) {
                i = 0;
            }
            if ((i2 | i) == 0) {
                setErrorPlaceholder();
            }
            this.isCallingCallbacks = false;
            notifyLoadFailed();
        } catch (Throwable th) {
            this.isCallingCallbacks = false;
        }
    }

    public boolean isEquivalentTo(Request request) {
        boolean z = false;
        if (!(request instanceof SingleRequest)) {
            return false;
        }
        SingleRequest singleRequest = (SingleRequest) request;
        if (this.overrideWidth == singleRequest.overrideWidth && this.overrideHeight == singleRequest.overrideHeight && Util.bothModelsNullEquivalentOrEquals(this.model, singleRequest.model) && this.transcodeClass.equals(singleRequest.transcodeClass) && this.requestOptions.equals(singleRequest.requestOptions) && this.priority == singleRequest.priority && listenerCountEquals(this, singleRequest)) {
            z = true;
        }
        return z;
    }

    private static boolean listenerCountEquals(SingleRequest<?> singleRequest, SingleRequest<?> singleRequest2) {
        if ((singleRequest.requestListeners == null ? 0 : singleRequest.requestListeners.size()) == (singleRequest2.requestListeners == null ? 0 : singleRequest2.requestListeners.size())) {
            return true;
        }
        return false;
    }

    private void logV(String str) {
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(" this: ");
        stringBuilder.append(this.tag);
        Log.v(str2, stringBuilder.toString());
    }
}
