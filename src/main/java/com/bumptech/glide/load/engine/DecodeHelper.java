package com.bumptech.glide.load.engine;

import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry.NoModelLoaderAvailableException;
import com.bumptech.glide.Registry.NoSourceEncoderAvailableException;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.bumptech.glide.load.resource.UnitTransformation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class DecodeHelper<Transcode> {
    private final List<Key> cacheKeys = new ArrayList();
    private DiskCacheProvider diskCacheProvider;
    private DiskCacheStrategy diskCacheStrategy;
    private GlideContext glideContext;
    private int height;
    private boolean isCacheKeysSet;
    private boolean isLoadDataSet;
    private boolean isScaleOnlyOrNoTransform;
    private boolean isTransformationRequired;
    private final List<LoadData<?>> loadData = new ArrayList();
    private Object model;
    private Options options;
    private Priority priority;
    private Class<?> resourceClass;
    private Key signature;
    private Class<Transcode> transcodeClass;
    private Map<Class<?>, Transformation<?>> transformations;
    private int width;

    DecodeHelper() {
    }

    /* Access modifiers changed, original: 0000 */
    public <R> void init(GlideContext glideContext, Object obj, Key key, int i, int i2, DiskCacheStrategy diskCacheStrategy, Class<?> cls, Class<R> cls2, Priority priority, Options options, Map<Class<?>, Transformation<?>> map, boolean z, boolean z2, DiskCacheProvider diskCacheProvider) {
        this.glideContext = glideContext;
        this.model = obj;
        this.signature = key;
        this.width = i;
        this.height = i2;
        this.diskCacheStrategy = diskCacheStrategy;
        this.resourceClass = cls;
        this.diskCacheProvider = diskCacheProvider;
        this.transcodeClass = cls2;
        this.priority = priority;
        this.options = options;
        this.transformations = map;
        this.isTransformationRequired = z;
        this.isScaleOnlyOrNoTransform = z2;
    }

    /* Access modifiers changed, original: 0000 */
    public void clear() {
        this.glideContext = null;
        this.model = null;
        this.signature = null;
        this.resourceClass = null;
        this.transcodeClass = null;
        this.options = null;
        this.priority = null;
        this.transformations = null;
        this.diskCacheStrategy = null;
        this.loadData.clear();
        this.isLoadDataSet = false;
        this.cacheKeys.clear();
        this.isCacheKeysSet = false;
    }

    /* Access modifiers changed, original: 0000 */
    public DiskCache getDiskCache() {
        return this.diskCacheProvider.getDiskCache();
    }

    /* Access modifiers changed, original: 0000 */
    public DiskCacheStrategy getDiskCacheStrategy() {
        return this.diskCacheStrategy;
    }

    /* Access modifiers changed, original: 0000 */
    public Priority getPriority() {
        return this.priority;
    }

    /* Access modifiers changed, original: 0000 */
    public Options getOptions() {
        return this.options;
    }

    /* Access modifiers changed, original: 0000 */
    public Key getSignature() {
        return this.signature;
    }

    /* Access modifiers changed, original: 0000 */
    public int getWidth() {
        return this.width;
    }

    /* Access modifiers changed, original: 0000 */
    public int getHeight() {
        return this.height;
    }

    /* Access modifiers changed, original: 0000 */
    public ArrayPool getArrayPool() {
        return this.glideContext.getArrayPool();
    }

    /* Access modifiers changed, original: 0000 */
    public Class<?> getTranscodeClass() {
        return this.transcodeClass;
    }

    /* Access modifiers changed, original: 0000 */
    public Class<?> getModelClass() {
        return this.model.getClass();
    }

    /* Access modifiers changed, original: 0000 */
    public List<Class<?>> getRegisteredResourceClasses() {
        return this.glideContext.getRegistry().getRegisteredResourceClasses(this.model.getClass(), this.resourceClass, this.transcodeClass);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasLoadPath(Class<?> cls) {
        return getLoadPath(cls) != null;
    }

    /* Access modifiers changed, original: 0000 */
    public <Data> LoadPath<Data, ?, Transcode> getLoadPath(Class<Data> cls) {
        return this.glideContext.getRegistry().getLoadPath(cls, this.resourceClass, this.transcodeClass);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isScaleOnlyOrNoTransform() {
        return this.isScaleOnlyOrNoTransform;
    }

    /* Access modifiers changed, original: 0000 */
    public <Z> Transformation<Z> getTransformation(Class<Z> cls) {
        Transformation<Z> transformation = (Transformation) this.transformations.get(cls);
        if (transformation == null) {
            for (Entry entry : this.transformations.entrySet()) {
                if (((Class) entry.getKey()).isAssignableFrom(cls)) {
                    transformation = (Transformation) entry.getValue();
                    break;
                }
            }
        }
        if (transformation != null) {
            return transformation;
        }
        if (!this.transformations.isEmpty() || !this.isTransformationRequired) {
            return UnitTransformation.get();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Missing transformation for ");
        stringBuilder.append(cls);
        stringBuilder.append(". If you wish to ignore unknown resource types, use the optional transformation methods.");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isResourceEncoderAvailable(Resource<?> resource) {
        return this.glideContext.getRegistry().isResourceEncoderAvailable(resource);
    }

    /* Access modifiers changed, original: 0000 */
    public <Z> ResourceEncoder<Z> getResultEncoder(Resource<Z> resource) {
        return this.glideContext.getRegistry().getResultEncoder(resource);
    }

    /* Access modifiers changed, original: 0000 */
    public List<ModelLoader<File, ?>> getModelLoaders(File file) throws NoModelLoaderAvailableException {
        return this.glideContext.getRegistry().getModelLoaders(file);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isSourceKey(Key key) {
        List loadData = getLoadData();
        int size = loadData.size();
        for (int i = 0; i < size; i++) {
            if (((LoadData) loadData.get(i)).sourceKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public List<LoadData<?>> getLoadData() {
        if (!this.isLoadDataSet) {
            this.isLoadDataSet = true;
            this.loadData.clear();
            List modelLoaders = this.glideContext.getRegistry().getModelLoaders(this.model);
            int size = modelLoaders.size();
            for (int i = 0; i < size; i++) {
                LoadData buildLoadData = ((ModelLoader) modelLoaders.get(i)).buildLoadData(this.model, this.width, this.height, this.options);
                if (buildLoadData != null) {
                    this.loadData.add(buildLoadData);
                }
            }
        }
        return this.loadData;
    }

    /* Access modifiers changed, original: 0000 */
    public List<Key> getCacheKeys() {
        if (!this.isCacheKeysSet) {
            this.isCacheKeysSet = true;
            this.cacheKeys.clear();
            List loadData = getLoadData();
            int size = loadData.size();
            for (int i = 0; i < size; i++) {
                LoadData loadData2 = (LoadData) loadData.get(i);
                if (!this.cacheKeys.contains(loadData2.sourceKey)) {
                    this.cacheKeys.add(loadData2.sourceKey);
                }
                for (int i2 = 0; i2 < loadData2.alternateKeys.size(); i2++) {
                    if (!this.cacheKeys.contains(loadData2.alternateKeys.get(i2))) {
                        this.cacheKeys.add(loadData2.alternateKeys.get(i2));
                    }
                }
            }
        }
        return this.cacheKeys;
    }

    /* Access modifiers changed, original: 0000 */
    public <X> Encoder<X> getSourceEncoder(X x) throws NoSourceEncoderAvailableException {
        return this.glideContext.getRegistry().getSourceEncoder(x);
    }
}
