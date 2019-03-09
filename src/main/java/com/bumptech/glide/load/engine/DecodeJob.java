package com.bumptech.glide.load.engine;

import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools.Pool;
import android.util.Log;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry.NoResultEncoderAvailableException;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.load.engine.DataFetcherGenerator.FetcherReadyCallback;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.pool.FactoryPools.Poolable;
import com.bumptech.glide.util.pool.GlideTrace;
import com.bumptech.glide.util.pool.StateVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DecodeJob<R> implements FetcherReadyCallback, Runnable, Comparable<DecodeJob<?>>, Poolable {
    private static final String TAG = "DecodeJob";
    private Callback<R> callback;
    private Key currentAttemptingKey;
    private Object currentData;
    private DataSource currentDataSource;
    private DataFetcher<?> currentFetcher;
    private volatile DataFetcherGenerator currentGenerator;
    private Key currentSourceKey;
    private Thread currentThread;
    private final DecodeHelper<R> decodeHelper = new DecodeHelper();
    private final DeferredEncodeManager<?> deferredEncodeManager = new DeferredEncodeManager();
    private final DiskCacheProvider diskCacheProvider;
    private DiskCacheStrategy diskCacheStrategy;
    private GlideContext glideContext;
    private int height;
    private volatile boolean isCallbackNotified;
    private volatile boolean isCancelled;
    private EngineKey loadKey;
    private Object model;
    private boolean onlyRetrieveFromCache;
    private Options options;
    private int order;
    private final Pool<DecodeJob<?>> pool;
    private Priority priority;
    private final ReleaseManager releaseManager = new ReleaseManager();
    private RunReason runReason;
    private Key signature;
    private Stage stage;
    private long startFetchTime;
    private final StateVerifier stateVerifier = StateVerifier.newInstance();
    private final List<Throwable> throwables = new ArrayList();
    private int width;

    interface Callback<R> {
        void onLoadFailed(GlideException glideException);

        void onResourceReady(Resource<R> resource, DataSource dataSource);

        void reschedule(DecodeJob<?> decodeJob);
    }

    private static class DeferredEncodeManager<Z> {
        private ResourceEncoder<Z> encoder;
        private Key key;
        private LockedResource<Z> toEncode;

        DeferredEncodeManager() {
        }

        /* Access modifiers changed, original: 0000 */
        public <X> void init(Key key, ResourceEncoder<X> resourceEncoder, LockedResource<X> lockedResource) {
            this.key = key;
            this.encoder = resourceEncoder;
            this.toEncode = lockedResource;
        }

        /* Access modifiers changed, original: 0000 */
        public void encode(DiskCacheProvider diskCacheProvider, Options options) {
            GlideTrace.beginSection("DecodeJob.encode");
            try {
                diskCacheProvider.getDiskCache().put(this.key, new DataCacheWriter(this.encoder, this.toEncode, options));
            } finally {
                this.toEncode.unlock();
                GlideTrace.endSection();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean hasResourceToEncode() {
            return this.toEncode != null;
        }

        /* Access modifiers changed, original: 0000 */
        public void clear() {
            this.key = null;
            this.encoder = null;
            this.toEncode = null;
        }
    }

    interface DiskCacheProvider {
        DiskCache getDiskCache();
    }

    private static class ReleaseManager {
        private boolean isEncodeComplete;
        private boolean isFailed;
        private boolean isReleased;

        ReleaseManager() {
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized boolean release(boolean z) {
            this.isReleased = true;
            return isComplete(z);
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized boolean onEncodeComplete() {
            this.isEncodeComplete = true;
            return isComplete(false);
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized boolean onFailed() {
            this.isFailed = true;
            return isComplete(false);
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized void reset() {
            this.isEncodeComplete = false;
            this.isReleased = false;
            this.isFailed = false;
        }

        private boolean isComplete(boolean z) {
            return (this.isFailed || z || this.isEncodeComplete) && this.isReleased;
        }
    }

    private enum RunReason {
        INITIALIZE,
        SWITCH_TO_SOURCE_SERVICE,
        DECODE_DATA
    }

    private enum Stage {
        INITIALIZE,
        RESOURCE_CACHE,
        DATA_CACHE,
        SOURCE,
        ENCODE,
        FINISHED
    }

    private final class DecodeCallback<Z> implements DecodeCallback<Z> {
        private final DataSource dataSource;

        DecodeCallback(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @NonNull
        public Resource<Z> onResourceDecoded(@NonNull Resource<Z> resource) {
            return DecodeJob.this.onResourceDecoded(this.dataSource, resource);
        }
    }

    DecodeJob(DiskCacheProvider diskCacheProvider, Pool<DecodeJob<?>> pool) {
        this.diskCacheProvider = diskCacheProvider;
        this.pool = pool;
    }

    /* Access modifiers changed, original: 0000 */
    public DecodeJob<R> init(GlideContext glideContext, Object obj, EngineKey engineKey, Key key, int i, int i2, Class<?> cls, Class<R> cls2, Priority priority, DiskCacheStrategy diskCacheStrategy, Map<Class<?>, Transformation<?>> map, boolean z, boolean z2, boolean z3, Options options, Callback<R> callback, int i3) {
        this.decodeHelper.init(glideContext, obj, key, i, i2, diskCacheStrategy, cls, cls2, priority, options, map, z, z2, this.diskCacheProvider);
        this.glideContext = glideContext;
        this.signature = key;
        this.priority = priority;
        this.loadKey = engineKey;
        this.width = i;
        this.height = i2;
        this.diskCacheStrategy = diskCacheStrategy;
        this.onlyRetrieveFromCache = z3;
        this.options = options;
        this.callback = callback;
        this.order = i3;
        this.runReason = RunReason.INITIALIZE;
        this.model = obj;
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean willDecodeFromCache() {
        Stage nextStage = getNextStage(Stage.INITIALIZE);
        return nextStage == Stage.RESOURCE_CACHE || nextStage == Stage.DATA_CACHE;
    }

    /* Access modifiers changed, original: 0000 */
    public void release(boolean z) {
        if (this.releaseManager.release(z)) {
            releaseInternal();
        }
    }

    private void onEncodeComplete() {
        if (this.releaseManager.onEncodeComplete()) {
            releaseInternal();
        }
    }

    private void onLoadFailed() {
        if (this.releaseManager.onFailed()) {
            releaseInternal();
        }
    }

    private void releaseInternal() {
        this.releaseManager.reset();
        this.deferredEncodeManager.clear();
        this.decodeHelper.clear();
        this.isCallbackNotified = false;
        this.glideContext = null;
        this.signature = null;
        this.options = null;
        this.priority = null;
        this.loadKey = null;
        this.callback = null;
        this.stage = null;
        this.currentGenerator = null;
        this.currentThread = null;
        this.currentSourceKey = null;
        this.currentData = null;
        this.currentDataSource = null;
        this.currentFetcher = null;
        this.startFetchTime = 0;
        this.isCancelled = false;
        this.model = null;
        this.throwables.clear();
        this.pool.release(this);
    }

    public int compareTo(@NonNull DecodeJob<?> decodeJob) {
        int priority = getPriority() - decodeJob.getPriority();
        return priority == 0 ? this.order - decodeJob.order : priority;
    }

    private int getPriority() {
        return this.priority.ordinal();
    }

    public void cancel() {
        this.isCancelled = true;
        DataFetcherGenerator dataFetcherGenerator = this.currentGenerator;
        if (dataFetcherGenerator != null) {
            dataFetcherGenerator.cancel();
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001c, code skipped:
            if (r0 != null) goto L_0x001e;
     */
    public void run() {
        /*
        r5 = this;
        r0 = "DecodeJob#run(model=%s)";
        r1 = r5.model;
        com.bumptech.glide.util.pool.GlideTrace.beginSectionFormat(r0, r1);
        r0 = r5.currentFetcher;
        r1 = r5.isCancelled;	 Catch:{ Throwable -> 0x0027 }
        if (r1 == 0) goto L_0x0019;
    L_0x000d:
        r5.notifyFailed();	 Catch:{ Throwable -> 0x0027 }
        if (r0 == 0) goto L_0x0015;
    L_0x0012:
        r0.cleanup();
    L_0x0015:
        com.bumptech.glide.util.pool.GlideTrace.endSection();
        return;
    L_0x0019:
        r5.runWrapped();	 Catch:{ Throwable -> 0x0027 }
        if (r0 == 0) goto L_0x0021;
    L_0x001e:
        r0.cleanup();
    L_0x0021:
        com.bumptech.glide.util.pool.GlideTrace.endSection();
        goto L_0x0068;
    L_0x0025:
        r1 = move-exception;
        goto L_0x006a;
    L_0x0027:
        r1 = move-exception;
        r2 = "DecodeJob";
        r3 = 3;
        r2 = android.util.Log.isLoggable(r2, r3);	 Catch:{ all -> 0x0025 }
        if (r2 == 0) goto L_0x0053;
    L_0x0031:
        r2 = "DecodeJob";
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0025 }
        r3.<init>();	 Catch:{ all -> 0x0025 }
        r4 = "DecodeJob threw unexpectedly, isCancelled: ";
        r3.append(r4);	 Catch:{ all -> 0x0025 }
        r4 = r5.isCancelled;	 Catch:{ all -> 0x0025 }
        r3.append(r4);	 Catch:{ all -> 0x0025 }
        r4 = ", stage: ";
        r3.append(r4);	 Catch:{ all -> 0x0025 }
        r4 = r5.stage;	 Catch:{ all -> 0x0025 }
        r3.append(r4);	 Catch:{ all -> 0x0025 }
        r3 = r3.toString();	 Catch:{ all -> 0x0025 }
        android.util.Log.d(r2, r3, r1);	 Catch:{ all -> 0x0025 }
    L_0x0053:
        r2 = r5.stage;	 Catch:{ all -> 0x0025 }
        r3 = com.bumptech.glide.load.engine.DecodeJob.Stage.ENCODE;	 Catch:{ all -> 0x0025 }
        if (r2 == r3) goto L_0x0061;
    L_0x0059:
        r2 = r5.throwables;	 Catch:{ all -> 0x0025 }
        r2.add(r1);	 Catch:{ all -> 0x0025 }
        r5.notifyFailed();	 Catch:{ all -> 0x0025 }
    L_0x0061:
        r2 = r5.isCancelled;	 Catch:{ all -> 0x0025 }
        if (r2 == 0) goto L_0x0069;
    L_0x0065:
        if (r0 == 0) goto L_0x0021;
    L_0x0067:
        goto L_0x001e;
    L_0x0068:
        return;
    L_0x0069:
        throw r1;	 Catch:{ all -> 0x0025 }
    L_0x006a:
        if (r0 == 0) goto L_0x006f;
    L_0x006c:
        r0.cleanup();
    L_0x006f:
        com.bumptech.glide.util.pool.GlideTrace.endSection();
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.load.engine.DecodeJob.run():void");
    }

    private void runWrapped() {
        switch (this.runReason) {
            case INITIALIZE:
                this.stage = getNextStage(Stage.INITIALIZE);
                this.currentGenerator = getNextGenerator();
                runGenerators();
                return;
            case SWITCH_TO_SOURCE_SERVICE:
                runGenerators();
                return;
            case DECODE_DATA:
                decodeFromRetrievedData();
                return;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unrecognized run reason: ");
                stringBuilder.append(this.runReason);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private DataFetcherGenerator getNextGenerator() {
        switch (this.stage) {
            case RESOURCE_CACHE:
                return new ResourceCacheGenerator(this.decodeHelper, this);
            case DATA_CACHE:
                return new DataCacheGenerator(this.decodeHelper, this);
            case SOURCE:
                return new SourceGenerator(this.decodeHelper, this);
            case FINISHED:
                return null;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unrecognized stage: ");
                stringBuilder.append(this.stage);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private void runGenerators() {
        this.currentThread = Thread.currentThread();
        this.startFetchTime = LogTime.getLogTime();
        boolean z = false;
        while (!this.isCancelled && this.currentGenerator != null) {
            z = this.currentGenerator.startNext();
            if (z) {
                break;
            }
            this.stage = getNextStage(this.stage);
            this.currentGenerator = getNextGenerator();
            if (this.stage == Stage.SOURCE) {
                reschedule();
                return;
            }
        }
        if ((this.stage == Stage.FINISHED || this.isCancelled) && !r0) {
            notifyFailed();
        }
    }

    private void notifyFailed() {
        setNotifiedOrThrow();
        this.callback.onLoadFailed(new GlideException("Failed to load resource", new ArrayList(this.throwables)));
        onLoadFailed();
    }

    private void notifyComplete(Resource<R> resource, DataSource dataSource) {
        setNotifiedOrThrow();
        this.callback.onResourceReady(resource, dataSource);
    }

    private void setNotifiedOrThrow() {
        this.stateVerifier.throwIfRecycled();
        if (this.isCallbackNotified) {
            throw new IllegalStateException("Already notified");
        }
        this.isCallbackNotified = true;
    }

    private Stage getNextStage(Stage stage) {
        switch (stage) {
            case RESOURCE_CACHE:
                return this.diskCacheStrategy.decodeCachedData() ? Stage.DATA_CACHE : getNextStage(Stage.DATA_CACHE);
            case DATA_CACHE:
                return this.onlyRetrieveFromCache ? Stage.FINISHED : Stage.SOURCE;
            case SOURCE:
            case FINISHED:
                return Stage.FINISHED;
            case INITIALIZE:
                return this.diskCacheStrategy.decodeCachedResource() ? Stage.RESOURCE_CACHE : getNextStage(Stage.RESOURCE_CACHE);
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unrecognized stage: ");
                stringBuilder.append(stage);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public void reschedule() {
        this.runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
        this.callback.reschedule(this);
    }

    public void onDataFetcherReady(Key key, Object obj, DataFetcher<?> dataFetcher, DataSource dataSource, Key key2) {
        this.currentSourceKey = key;
        this.currentData = obj;
        this.currentFetcher = dataFetcher;
        this.currentDataSource = dataSource;
        this.currentAttemptingKey = key2;
        if (Thread.currentThread() != this.currentThread) {
            this.runReason = RunReason.DECODE_DATA;
            this.callback.reschedule(this);
            return;
        }
        GlideTrace.beginSection("DecodeJob.decodeFromRetrievedData");
        try {
            decodeFromRetrievedData();
        } finally {
            GlideTrace.endSection();
        }
    }

    public void onDataFetcherFailed(Key key, Exception exception, DataFetcher<?> dataFetcher, DataSource dataSource) {
        dataFetcher.cleanup();
        GlideException glideException = new GlideException("Fetching data failed", (Throwable) exception);
        glideException.setLoggingDetails(key, dataSource, dataFetcher.getDataClass());
        this.throwables.add(glideException);
        if (Thread.currentThread() != this.currentThread) {
            this.runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
            this.callback.reschedule(this);
            return;
        }
        runGenerators();
    }

    private void decodeFromRetrievedData() {
        if (Log.isLoggable(TAG, 2)) {
            long j = this.startFetchTime;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("data: ");
            stringBuilder.append(this.currentData);
            stringBuilder.append(", cache key: ");
            stringBuilder.append(this.currentSourceKey);
            stringBuilder.append(", fetcher: ");
            stringBuilder.append(this.currentFetcher);
            logWithTimeAndKey("Retrieved data", j, stringBuilder.toString());
        }
        Resource resource = null;
        try {
            resource = decodeFromData(this.currentFetcher, this.currentData, this.currentDataSource);
        } catch (GlideException e) {
            e.setLoggingDetails(this.currentAttemptingKey, this.currentDataSource);
            this.throwables.add(e);
        }
        if (resource != null) {
            notifyEncodeAndRelease(resource, this.currentDataSource);
        } else {
            runGenerators();
        }
    }

    private void notifyEncodeAndRelease(Resource<R> resource, DataSource dataSource) {
        Resource resource2;
        if (resource2 instanceof Initializable) {
            ((Initializable) resource2).initialize();
        }
        LockedResource lockedResource = null;
        if (this.deferredEncodeManager.hasResourceToEncode()) {
            resource2 = LockedResource.obtain(resource2);
            lockedResource = resource2;
        }
        notifyComplete(resource2, dataSource);
        this.stage = Stage.ENCODE;
        try {
            if (this.deferredEncodeManager.hasResourceToEncode()) {
                this.deferredEncodeManager.encode(this.diskCacheProvider, this.options);
            }
            if (lockedResource != null) {
                lockedResource.unlock();
            }
            onEncodeComplete();
        } catch (Throwable th) {
            if (lockedResource != null) {
                lockedResource.unlock();
            }
        }
    }

    private <Data> Resource<R> decodeFromData(DataFetcher<?> dataFetcher, Data data, DataSource dataSource) throws GlideException {
        if (data == null) {
            dataFetcher.cleanup();
            return null;
        }
        try {
            long logTime = LogTime.getLogTime();
            Resource decodeFromFetcher = decodeFromFetcher(data, dataSource);
            if (Log.isLoggable(TAG, 2)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Decoded result ");
                stringBuilder.append(decodeFromFetcher);
                logWithTimeAndKey(stringBuilder.toString(), logTime);
            }
            dataFetcher.cleanup();
            return decodeFromFetcher;
        } catch (Throwable th) {
            dataFetcher.cleanup();
        }
    }

    private <Data> Resource<R> decodeFromFetcher(Data data, DataSource dataSource) throws GlideException {
        return runLoadPath(data, dataSource, this.decodeHelper.getLoadPath(data.getClass()));
    }

    @NonNull
    private Options getOptionsWithHardwareConfig(DataSource dataSource) {
        Options options = this.options;
        if (VERSION.SDK_INT < 26) {
            return options;
        }
        boolean z = dataSource == DataSource.RESOURCE_DISK_CACHE || this.decodeHelper.isScaleOnlyOrNoTransform();
        Boolean bool = (Boolean) options.get(Downsampler.ALLOW_HARDWARE_CONFIG);
        if (bool != null && (!bool.booleanValue() || z)) {
            return options;
        }
        options = new Options();
        options.putAll(this.options);
        options.set(Downsampler.ALLOW_HARDWARE_CONFIG, Boolean.valueOf(z));
        return options;
    }

    private <Data, ResourceType> Resource<R> runLoadPath(Data data, DataSource dataSource, LoadPath<Data, ResourceType, R> loadPath) throws GlideException {
        Options optionsWithHardwareConfig = getOptionsWithHardwareConfig(dataSource);
        DataRewinder rewinder = this.glideContext.getRegistry().getRewinder(data);
        try {
            Resource<R> load = loadPath.load(rewinder, optionsWithHardwareConfig, this.width, this.height, new DecodeCallback(dataSource));
            return load;
        } finally {
            rewinder.cleanup();
        }
    }

    private void logWithTimeAndKey(String str, long j) {
        logWithTimeAndKey(str, j, null);
    }

    private void logWithTimeAndKey(String str, long j, String str2) {
        String str3 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(" in ");
        stringBuilder.append(LogTime.getElapsedMillis(j));
        stringBuilder.append(", load key: ");
        stringBuilder.append(this.loadKey);
        if (str2 != null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(", ");
            stringBuilder2.append(str2);
            str = stringBuilder2.toString();
        } else {
            str = "";
        }
        stringBuilder.append(str);
        stringBuilder.append(", thread: ");
        stringBuilder.append(Thread.currentThread().getName());
        Log.v(str3, stringBuilder.toString());
    }

    @NonNull
    public StateVerifier getVerifier() {
        return this.stateVerifier;
    }

    /* Access modifiers changed, original: 0000 */
    @NonNull
    public <Z> Resource<Z> onResourceDecoded(DataSource dataSource, @NonNull Resource<Z> resource) {
        Transformation transformation;
        Object transform;
        EncodeStrategy encodeStrategy;
        Class cls = resource.get().getClass();
        ResourceEncoder resourceEncoder = null;
        if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
            Transformation transformation2 = this.decodeHelper.getTransformation(cls);
            transformation = transformation2;
            transform = transformation2.transform(this.glideContext, resource, this.width, this.height);
        } else {
            transform = resource;
            transformation = null;
        }
        if (!resource.equals(transform)) {
            resource.recycle();
        }
        if (this.decodeHelper.isResourceEncoderAvailable(transform)) {
            resourceEncoder = this.decodeHelper.getResultEncoder(transform);
            encodeStrategy = resourceEncoder.getEncodeStrategy(this.options);
        } else {
            encodeStrategy = EncodeStrategy.NONE;
        }
        ResourceEncoder resourceEncoder2 = resourceEncoder;
        if (!this.diskCacheStrategy.isResourceCacheable(this.decodeHelper.isSourceKey(this.currentSourceKey) ^ 1, dataSource, encodeStrategy)) {
            return transform;
        }
        if (resourceEncoder2 != null) {
            Key dataCacheKey;
            switch (encodeStrategy) {
                case SOURCE:
                    dataCacheKey = new DataCacheKey(this.currentSourceKey, this.signature);
                    break;
                case TRANSFORMED:
                    Key resourceCacheKey = new ResourceCacheKey(this.decodeHelper.getArrayPool(), this.currentSourceKey, this.signature, this.width, this.height, transformation, cls, this.options);
                    break;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown strategy: ");
                    stringBuilder.append(encodeStrategy);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
            Resource<Z> obtain = LockedResource.obtain(transform);
            this.deferredEncodeManager.init(dataCacheKey, resourceEncoder2, obtain);
            return obtain;
        }
        throw new NoResultEncoderAvailableException(transform.get().getClass());
    }
}
