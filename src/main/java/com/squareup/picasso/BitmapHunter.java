package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.NetworkInfo;
import com.squareup.picasso.Downloader.ResponseException;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso.Priority;
import com.squareup.picasso.RequestHandler.Result;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

class BitmapHunter implements Runnable {
    private static final Object DECODE_LOCK = new Object();
    private static final RequestHandler ERRORING_HANDLER = new RequestHandler() {
        public boolean canHandleRequest(Request request) {
            return true;
        }

        public Result load(Request request, int i) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unrecognized type of request: ");
            stringBuilder.append(request);
            throw new IllegalStateException(stringBuilder.toString());
        }
    };
    private static final ThreadLocal<StringBuilder> NAME_BUILDER = new ThreadLocal<StringBuilder>() {
        /* Access modifiers changed, original: protected */
        public StringBuilder initialValue() {
            return new StringBuilder("Picasso-");
        }
    };
    private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();
    Action action;
    List<Action> actions;
    final Cache cache;
    final Request data;
    final Dispatcher dispatcher;
    Exception exception;
    int exifRotation;
    Future<?> future;
    final String key;
    LoadedFrom loadedFrom;
    final int memoryPolicy;
    int networkPolicy;
    final Picasso picasso;
    Priority priority;
    final RequestHandler requestHandler;
    Bitmap result;
    int retryCount;
    final int sequence = SEQUENCE_GENERATOR.incrementAndGet();
    final Stats stats;

    private static boolean shouldResize(boolean z, int i, int i2, int i3, int i4) {
        return !z || i > i3 || i2 > i4;
    }

    BitmapHunter(Picasso picasso, Dispatcher dispatcher, Cache cache, Stats stats, Action action, RequestHandler requestHandler) {
        this.picasso = picasso;
        this.dispatcher = dispatcher;
        this.cache = cache;
        this.stats = stats;
        this.action = action;
        this.key = action.getKey();
        this.data = action.getRequest();
        this.priority = action.getPriority();
        this.memoryPolicy = action.getMemoryPolicy();
        this.networkPolicy = action.getNetworkPolicy();
        this.requestHandler = requestHandler;
        this.retryCount = requestHandler.getRetryCount();
    }

    static Bitmap decodeStream(InputStream inputStream, Request request) throws IOException {
        MarkableInputStream markableInputStream = new MarkableInputStream(inputStream);
        long savePosition = markableInputStream.savePosition(65536);
        Options createBitmapOptions = RequestHandler.createBitmapOptions(request);
        boolean requiresInSampleSize = RequestHandler.requiresInSampleSize(createBitmapOptions);
        boolean isWebPFile = Utils.isWebPFile(markableInputStream);
        markableInputStream.reset(savePosition);
        if (isWebPFile) {
            byte[] toByteArray = Utils.toByteArray(markableInputStream);
            if (requiresInSampleSize) {
                BitmapFactory.decodeByteArray(toByteArray, 0, toByteArray.length, createBitmapOptions);
                RequestHandler.calculateInSampleSize(request.targetWidth, request.targetHeight, createBitmapOptions, request);
            }
            return BitmapFactory.decodeByteArray(toByteArray, 0, toByteArray.length, createBitmapOptions);
        }
        if (requiresInSampleSize) {
            BitmapFactory.decodeStream(markableInputStream, null, createBitmapOptions);
            RequestHandler.calculateInSampleSize(request.targetWidth, request.targetHeight, createBitmapOptions, request);
            markableInputStream.reset(savePosition);
        }
        Bitmap decodeStream = BitmapFactory.decodeStream(markableInputStream, null, createBitmapOptions);
        if (decodeStream != null) {
            return decodeStream;
        }
        throw new IOException("Failed to decode stream.");
    }

    public void run() {
        try {
            updateThreadName(this.data);
            if (this.picasso.loggingEnabled) {
                Utils.log("Hunter", "executing", Utils.getLogIdsForHunter(this));
            }
            this.result = hunt();
            if (this.result == null) {
                this.dispatcher.dispatchFailed(this);
            } else {
                this.dispatcher.dispatchComplete(this);
            }
        } catch (ResponseException e) {
            if (!(e.localCacheOnly && e.responseCode == 504)) {
                this.exception = e;
            }
            this.dispatcher.dispatchFailed(this);
        } catch (ContentLengthException e2) {
            this.exception = e2;
            this.dispatcher.dispatchRetry(this);
        } catch (IOException e3) {
            this.exception = e3;
            this.dispatcher.dispatchRetry(this);
        } catch (OutOfMemoryError e4) {
            StringWriter stringWriter = new StringWriter();
            this.stats.createSnapshot().dump(new PrintWriter(stringWriter));
            this.exception = new RuntimeException(stringWriter.toString(), e4);
            this.dispatcher.dispatchFailed(this);
        } catch (Exception e5) {
            this.exception = e5;
            this.dispatcher.dispatchFailed(this);
        } catch (Throwable th) {
            Thread.currentThread().setName("Picasso-Idle");
        }
        Thread.currentThread().setName("Picasso-Idle");
    }

    /* Access modifiers changed, original: 0000 */
    public Bitmap hunt() throws IOException {
        Bitmap bitmap;
        if (MemoryPolicy.shouldReadFromMemoryCache(this.memoryPolicy)) {
            bitmap = this.cache.get(this.key);
            if (bitmap != null) {
                this.stats.dispatchCacheHit();
                this.loadedFrom = LoadedFrom.MEMORY;
                if (this.picasso.loggingEnabled) {
                    Utils.log("Hunter", "decoded", this.data.logId(), "from cache");
                }
                return bitmap;
            }
        }
        bitmap = null;
        this.data.networkPolicy = this.retryCount == 0 ? NetworkPolicy.OFFLINE.index : this.networkPolicy;
        Result load = this.requestHandler.load(this.data, this.networkPolicy);
        if (load != null) {
            this.loadedFrom = load.getLoadedFrom();
            this.exifRotation = load.getExifOrientation();
            bitmap = load.getBitmap();
            if (bitmap == null) {
                InputStream stream = load.getStream();
                try {
                    Bitmap decodeStream = decodeStream(stream, this.data);
                    bitmap = decodeStream;
                } finally {
                    Utils.closeQuietly(stream);
                }
            }
        }
        if (bitmap != null) {
            if (this.picasso.loggingEnabled) {
                Utils.log("Hunter", "decoded", this.data.logId());
            }
            this.stats.dispatchBitmapDecoded(bitmap);
            if (this.data.needsTransformation() || this.exifRotation != 0) {
                synchronized (DECODE_LOCK) {
                    if (this.data.needsMatrixTransform() || this.exifRotation != 0) {
                        bitmap = transformResult(this.data, bitmap, this.exifRotation);
                        if (this.picasso.loggingEnabled) {
                            Utils.log("Hunter", "transformed", this.data.logId());
                        }
                    }
                    if (this.data.hasCustomTransformations()) {
                        bitmap = applyCustomTransformations(this.data.transformations, bitmap);
                        if (this.picasso.loggingEnabled) {
                            Utils.log("Hunter", "transformed", this.data.logId(), "from custom transformations");
                        }
                    }
                }
                if (bitmap != null) {
                    this.stats.dispatchBitmapTransformed(bitmap);
                }
            }
        }
        return bitmap;
    }

    /* Access modifiers changed, original: 0000 */
    public void attach(Action action) {
        boolean z = this.picasso.loggingEnabled;
        Request request = action.request;
        if (this.action == null) {
            this.action = action;
            if (z) {
                if (this.actions == null || this.actions.isEmpty()) {
                    Utils.log("Hunter", "joined", request.logId(), "to empty hunter");
                } else {
                    Utils.log("Hunter", "joined", request.logId(), Utils.getLogIdsForHunter(this, "to "));
                }
            }
            return;
        }
        if (this.actions == null) {
            this.actions = new ArrayList(3);
        }
        this.actions.add(action);
        if (z) {
            Utils.log("Hunter", "joined", request.logId(), Utils.getLogIdsForHunter(this, "to "));
        }
        Priority priority = action.getPriority();
        if (priority.ordinal() > this.priority.ordinal()) {
            this.priority = priority;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void detach(Action action) {
        boolean z;
        if (this.action == action) {
            this.action = null;
            z = true;
        } else {
            z = this.actions != null ? this.actions.remove(action) : false;
        }
        if (z && action.getPriority() == this.priority) {
            this.priority = computeNewPriority();
        }
        if (this.picasso.loggingEnabled) {
            Utils.log("Hunter", "removed", action.request.logId(), Utils.getLogIdsForHunter(this, "from "));
        }
    }

    private Priority computeNewPriority() {
        Priority priority = Priority.LOW;
        Object obj = 1;
        Object obj2 = (this.actions == null || this.actions.isEmpty()) ? null : 1;
        if (this.action == null && obj2 == null) {
            obj = null;
        }
        if (obj == null) {
            return priority;
        }
        if (this.action != null) {
            priority = this.action.getPriority();
        }
        if (obj2 != null) {
            int size = this.actions.size();
            for (int i = 0; i < size; i++) {
                Priority priority2 = ((Action) this.actions.get(i)).getPriority();
                if (priority2.ordinal() > priority.ordinal()) {
                    priority = priority2;
                }
            }
        }
        return priority;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean cancel() {
        if (this.action != null) {
            return false;
        }
        if ((this.actions == null || this.actions.isEmpty()) && this.future != null && this.future.cancel(false)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isCancelled() {
        return this.future != null && this.future.isCancelled();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean shouldRetry(boolean z, NetworkInfo networkInfo) {
        if ((this.retryCount > 0 ? 1 : null) == null) {
            return false;
        }
        this.retryCount--;
        return this.requestHandler.shouldRetry(z, networkInfo);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean supportsReplay() {
        return this.requestHandler.supportsReplay();
    }

    /* Access modifiers changed, original: 0000 */
    public Bitmap getResult() {
        return this.result;
    }

    /* Access modifiers changed, original: 0000 */
    public String getKey() {
        return this.key;
    }

    /* Access modifiers changed, original: 0000 */
    public int getMemoryPolicy() {
        return this.memoryPolicy;
    }

    /* Access modifiers changed, original: 0000 */
    public Request getData() {
        return this.data;
    }

    /* Access modifiers changed, original: 0000 */
    public Action getAction() {
        return this.action;
    }

    /* Access modifiers changed, original: 0000 */
    public Picasso getPicasso() {
        return this.picasso;
    }

    /* Access modifiers changed, original: 0000 */
    public List<Action> getActions() {
        return this.actions;
    }

    /* Access modifiers changed, original: 0000 */
    public Exception getException() {
        return this.exception;
    }

    /* Access modifiers changed, original: 0000 */
    public LoadedFrom getLoadedFrom() {
        return this.loadedFrom;
    }

    /* Access modifiers changed, original: 0000 */
    public Priority getPriority() {
        return this.priority;
    }

    static void updateThreadName(Request request) {
        String name = request.getName();
        StringBuilder stringBuilder = (StringBuilder) NAME_BUILDER.get();
        stringBuilder.ensureCapacity("Picasso-".length() + name.length());
        stringBuilder.replace("Picasso-".length(), stringBuilder.length(), name);
        Thread.currentThread().setName(stringBuilder.toString());
    }

    static BitmapHunter forRequest(Picasso picasso, Dispatcher dispatcher, Cache cache, Stats stats, Action action) {
        Request request = action.getRequest();
        List requestHandlers = picasso.getRequestHandlers();
        int size = requestHandlers.size();
        for (int i = 0; i < size; i++) {
            RequestHandler requestHandler = (RequestHandler) requestHandlers.get(i);
            if (requestHandler.canHandleRequest(request)) {
                return new BitmapHunter(picasso, dispatcher, cache, stats, action, requestHandler);
            }
        }
        return new BitmapHunter(picasso, dispatcher, cache, stats, action, ERRORING_HANDLER);
    }

    static Bitmap applyCustomTransformations(List<Transformation> list, Bitmap bitmap) {
        int size = list.size();
        int i = 0;
        while (i < size) {
            final Transformation transformation = (Transformation) list.get(i);
            try {
                Bitmap transform = transformation.transform(bitmap);
                if (transform == null) {
                    final StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Transformation ");
                    stringBuilder.append(transformation.key());
                    stringBuilder.append(" returned null after ");
                    stringBuilder.append(i);
                    stringBuilder.append(" previous transformation(s).\n\nTransformation list:\n");
                    for (Transformation key : list) {
                        stringBuilder.append(key.key());
                        stringBuilder.append(10);
                    }
                    Picasso.HANDLER.post(new Runnable() {
                        public void run() {
                            throw new NullPointerException(stringBuilder.toString());
                        }
                    });
                    return null;
                } else if (transform == bitmap && bitmap.isRecycled()) {
                    Picasso.HANDLER.post(new Runnable() {
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Transformation ");
                            stringBuilder.append(transformation.key());
                            stringBuilder.append(" returned input Bitmap but recycled it.");
                            throw new IllegalStateException(stringBuilder.toString());
                        }
                    });
                    return null;
                } else if (transform == bitmap || bitmap.isRecycled()) {
                    i++;
                    bitmap = transform;
                } else {
                    Picasso.HANDLER.post(new Runnable() {
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Transformation ");
                            stringBuilder.append(transformation.key());
                            stringBuilder.append(" mutated input Bitmap but failed to recycle the original.");
                            throw new IllegalStateException(stringBuilder.toString());
                        }
                    });
                    return null;
                }
            } catch (RuntimeException e) {
                Picasso.HANDLER.post(new Runnable() {
                    public void run() {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Transformation ");
                        stringBuilder.append(transformation.key());
                        stringBuilder.append(" crashed with exception.");
                        throw new RuntimeException(stringBuilder.toString(), e);
                    }
                });
                return null;
            }
        }
        return bitmap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c2  */
    static android.graphics.Bitmap transformResult(com.squareup.picasso.Request r13, android.graphics.Bitmap r14, int r15) {
        /*
        r0 = r14.getWidth();
        r1 = r14.getHeight();
        r2 = r13.onlyScaleDown;
        r8 = new android.graphics.Matrix;
        r8.<init>();
        r3 = r13.needsMatrixTransform();
        r4 = 0;
        if (r3 == 0) goto L_0x00b1;
    L_0x0016:
        r3 = r13.targetWidth;
        r5 = r13.targetHeight;
        r6 = r13.rotationDegrees;
        r7 = 0;
        r7 = (r6 > r7 ? 1 : (r6 == r7 ? 0 : -1));
        if (r7 == 0) goto L_0x0030;
    L_0x0021:
        r7 = r13.hasRotationPivot;
        if (r7 == 0) goto L_0x002d;
    L_0x0025:
        r7 = r13.rotationPivotX;
        r9 = r13.rotationPivotY;
        r8.setRotate(r6, r7, r9);
        goto L_0x0030;
    L_0x002d:
        r8.setRotate(r6);
    L_0x0030:
        r6 = r13.centerCrop;
        if (r6 == 0) goto L_0x0074;
    L_0x0034:
        r13 = (float) r3;
        r6 = (float) r0;
        r7 = r13 / r6;
        r9 = (float) r5;
        r10 = (float) r1;
        r11 = r9 / r10;
        r12 = (r7 > r11 ? 1 : (r7 == r11 ? 0 : -1));
        if (r12 <= 0) goto L_0x0054;
    L_0x0040:
        r11 = r11 / r7;
        r10 = r10 * r11;
        r10 = (double) r10;
        r10 = java.lang.Math.ceil(r10);
        r13 = (int) r10;
        r6 = r1 - r13;
        r6 = r6 / 2;
        r10 = (float) r13;
        r11 = r9 / r10;
        r9 = r13;
        r13 = r7;
        r7 = r0;
        goto L_0x0067;
    L_0x0054:
        r7 = r7 / r11;
        r6 = r6 * r7;
        r6 = (double) r6;
        r6 = java.lang.Math.ceil(r6);
        r6 = (int) r6;
        r7 = r0 - r6;
        r7 = r7 / 2;
        r9 = (float) r6;
        r13 = r13 / r9;
        r9 = r1;
        r4 = r7;
        r7 = r6;
        r6 = 0;
    L_0x0067:
        r0 = shouldResize(r2, r0, r1, r3, r5);
        if (r0 == 0) goto L_0x0070;
    L_0x006d:
        r8.preScale(r13, r11);
    L_0x0070:
        r5 = r6;
        r6 = r7;
        r7 = r9;
        goto L_0x00b4;
    L_0x0074:
        r13 = r13.centerInside;
        if (r13 == 0) goto L_0x008e;
    L_0x0078:
        r13 = (float) r3;
        r6 = (float) r0;
        r13 = r13 / r6;
        r6 = (float) r5;
        r7 = (float) r1;
        r6 = r6 / r7;
        r7 = (r13 > r6 ? 1 : (r13 == r6 ? 0 : -1));
        if (r7 >= 0) goto L_0x0083;
    L_0x0082:
        goto L_0x0084;
    L_0x0083:
        r13 = r6;
    L_0x0084:
        r2 = shouldResize(r2, r0, r1, r3, r5);
        if (r2 == 0) goto L_0x00b1;
    L_0x008a:
        r8.preScale(r13, r13);
        goto L_0x00b1;
    L_0x008e:
        if (r3 != 0) goto L_0x0092;
    L_0x0090:
        if (r5 == 0) goto L_0x00b1;
    L_0x0092:
        if (r3 != r0) goto L_0x0096;
    L_0x0094:
        if (r5 == r1) goto L_0x00b1;
    L_0x0096:
        if (r3 == 0) goto L_0x009c;
    L_0x0098:
        r13 = (float) r3;
        r6 = (float) r0;
    L_0x009a:
        r13 = r13 / r6;
        goto L_0x009f;
    L_0x009c:
        r13 = (float) r5;
        r6 = (float) r1;
        goto L_0x009a;
    L_0x009f:
        if (r5 == 0) goto L_0x00a5;
    L_0x00a1:
        r6 = (float) r5;
        r7 = (float) r1;
    L_0x00a3:
        r6 = r6 / r7;
        goto L_0x00a8;
    L_0x00a5:
        r6 = (float) r3;
        r7 = (float) r0;
        goto L_0x00a3;
    L_0x00a8:
        r2 = shouldResize(r2, r0, r1, r3, r5);
        if (r2 == 0) goto L_0x00b1;
    L_0x00ae:
        r8.preScale(r13, r6);
    L_0x00b1:
        r6 = r0;
        r7 = r1;
        r5 = 0;
    L_0x00b4:
        if (r15 == 0) goto L_0x00ba;
    L_0x00b6:
        r13 = (float) r15;
        r8.preRotate(r13);
    L_0x00ba:
        r9 = 1;
        r3 = r14;
        r13 = android.graphics.Bitmap.createBitmap(r3, r4, r5, r6, r7, r8, r9);
        if (r13 == r14) goto L_0x00c6;
    L_0x00c2:
        r14.recycle();
        goto L_0x00c7;
    L_0x00c6:
        r13 = r14;
    L_0x00c7:
        return r13;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.picasso.BitmapHunter.transformResult(com.squareup.picasso.Request, android.graphics.Bitmap, int):android.graphics.Bitmap");
    }
}
