package com.bumptech.glide.load.model;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import java.io.InputStream;

public class ResourceLoader<Data> implements ModelLoader<Integer, Data> {
    private static final String TAG = "ResourceLoader";
    private final Resources resources;
    private final ModelLoader<Uri, Data> uriLoader;

    public static final class AssetFileDescriptorFactory implements ModelLoaderFactory<Integer, AssetFileDescriptor> {
        private final Resources resources;

        public void teardown() {
        }

        public AssetFileDescriptorFactory(Resources resources) {
            this.resources = resources;
        }

        public ModelLoader<Integer, AssetFileDescriptor> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new ResourceLoader(this.resources, multiModelLoaderFactory.build(Uri.class, AssetFileDescriptor.class));
        }
    }

    public static class FileDescriptorFactory implements ModelLoaderFactory<Integer, ParcelFileDescriptor> {
        private final Resources resources;

        public void teardown() {
        }

        public FileDescriptorFactory(Resources resources) {
            this.resources = resources;
        }

        @NonNull
        public ModelLoader<Integer, ParcelFileDescriptor> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new ResourceLoader(this.resources, multiModelLoaderFactory.build(Uri.class, ParcelFileDescriptor.class));
        }
    }

    public static class StreamFactory implements ModelLoaderFactory<Integer, InputStream> {
        private final Resources resources;

        public void teardown() {
        }

        public StreamFactory(Resources resources) {
            this.resources = resources;
        }

        @NonNull
        public ModelLoader<Integer, InputStream> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new ResourceLoader(this.resources, multiModelLoaderFactory.build(Uri.class, InputStream.class));
        }
    }

    public static class UriFactory implements ModelLoaderFactory<Integer, Uri> {
        private final Resources resources;

        public void teardown() {
        }

        public UriFactory(Resources resources) {
            this.resources = resources;
        }

        @NonNull
        public ModelLoader<Integer, Uri> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new ResourceLoader(this.resources, UnitModelLoader.getInstance());
        }
    }

    public boolean handles(@NonNull Integer num) {
        return true;
    }

    public ResourceLoader(Resources resources, ModelLoader<Uri, Data> modelLoader) {
        this.resources = resources;
        this.uriLoader = modelLoader;
    }

    public LoadData<Data> buildLoadData(@NonNull Integer num, int i, int i2, @NonNull Options options) {
        Uri resourceUri = getResourceUri(num);
        if (resourceUri == null) {
            return null;
        }
        return this.uriLoader.buildLoadData(resourceUri, i, i2, options);
    }

    @Nullable
    private Uri getResourceUri(Integer num) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("android.resource://");
            stringBuilder.append(this.resources.getResourcePackageName(num.intValue()));
            stringBuilder.append('/');
            stringBuilder.append(this.resources.getResourceTypeName(num.intValue()));
            stringBuilder.append('/');
            stringBuilder.append(this.resources.getResourceEntryName(num.intValue()));
            return Uri.parse(stringBuilder.toString());
        } catch (NotFoundException e) {
            if (Log.isLoggable(TAG, 5)) {
                String str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Received invalid resource id: ");
                stringBuilder2.append(num);
                Log.w(str, stringBuilder2.toString(), e);
            }
            return null;
        }
    }
}
