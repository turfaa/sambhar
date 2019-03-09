package com.bumptech.glide.load.model.stream;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class BaseGlideUrlLoader<Model> implements ModelLoader<Model, InputStream> {
    private final ModelLoader<GlideUrl, InputStream> concreteLoader;
    @Nullable
    private final ModelCache<Model, GlideUrl> modelCache;

    public abstract String getUrl(Model model, int i, int i2, Options options);

    protected BaseGlideUrlLoader(ModelLoader<GlideUrl, InputStream> modelLoader) {
        this(modelLoader, null);
    }

    protected BaseGlideUrlLoader(ModelLoader<GlideUrl, InputStream> modelLoader, @Nullable ModelCache<Model, GlideUrl> modelCache) {
        this.concreteLoader = modelLoader;
        this.modelCache = modelCache;
    }

    @Nullable
    public LoadData<InputStream> buildLoadData(@NonNull Model model, int i, int i2, @NonNull Options options) {
        Object obj = this.modelCache != null ? (GlideUrl) this.modelCache.get(model, i, i2) : null;
        if (obj == null) {
            String url = getUrl(model, i, i2, options);
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            GlideUrl glideUrl = new GlideUrl(url, getHeaders(model, i, i2, options));
            if (this.modelCache != null) {
                this.modelCache.put(model, i, i2, glideUrl);
            }
            obj = glideUrl;
        }
        List alternateUrls = getAlternateUrls(model, i, i2, options);
        LoadData buildLoadData = this.concreteLoader.buildLoadData(obj, i, i2, options);
        return (buildLoadData == null || alternateUrls.isEmpty()) ? buildLoadData : new LoadData(buildLoadData.sourceKey, getAlternateKeys(alternateUrls), buildLoadData.fetcher);
    }

    private static List<Key> getAlternateKeys(Collection<String> collection) {
        ArrayList arrayList = new ArrayList(collection.size());
        for (String glideUrl : collection) {
            arrayList.add(new GlideUrl(glideUrl));
        }
        return arrayList;
    }

    /* Access modifiers changed, original: protected */
    public List<String> getAlternateUrls(Model model, int i, int i2, Options options) {
        return Collections.emptyList();
    }

    /* Access modifiers changed, original: protected */
    @Nullable
    public Headers getHeaders(Model model, int i, int i2, Options options) {
        return Headers.DEFAULT;
    }
}
