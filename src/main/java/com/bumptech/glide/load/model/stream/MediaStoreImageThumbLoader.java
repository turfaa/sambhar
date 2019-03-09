package com.bumptech.glide.load.model.stream;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.load.data.mediastore.ThumbFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import java.io.InputStream;

public class MediaStoreImageThumbLoader implements ModelLoader<Uri, InputStream> {
    private final Context context;

    public static class Factory implements ModelLoaderFactory<Uri, InputStream> {
        private final Context context;

        public void teardown() {
        }

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        public ModelLoader<Uri, InputStream> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new MediaStoreImageThumbLoader(this.context);
        }
    }

    public MediaStoreImageThumbLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    public LoadData<InputStream> buildLoadData(@NonNull Uri uri, int i, int i2, @NonNull Options options) {
        return MediaStoreUtil.isThumbnailSize(i, i2) ? new LoadData(new ObjectKey(uri), ThumbFetcher.buildImageFetcher(this.context, uri)) : null;
    }

    public boolean handles(@NonNull Uri uri) {
        return MediaStoreUtil.isMediaStoreImageUri(uri);
    }
}
