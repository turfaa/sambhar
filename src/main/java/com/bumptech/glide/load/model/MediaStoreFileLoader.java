package com.bumptech.glide.load.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.DataFetcher.DataCallback;
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.bumptech.glide.signature.ObjectKey;
import java.io.File;
import java.io.FileNotFoundException;

public final class MediaStoreFileLoader implements ModelLoader<Uri, File> {
    private final Context context;

    public static final class Factory implements ModelLoaderFactory<Uri, File> {
        private final Context context;

        public void teardown() {
        }

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        public ModelLoader<Uri, File> build(MultiModelLoaderFactory multiModelLoaderFactory) {
            return new MediaStoreFileLoader(this.context);
        }
    }

    private static class FilePathFetcher implements DataFetcher<File> {
        private static final String[] PROJECTION = new String[]{"_data"};
        private final Context context;
        private final Uri uri;

        public void cancel() {
        }

        public void cleanup() {
        }

        FilePathFetcher(Context context, Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super File> dataCallback) {
            Cursor query = this.context.getContentResolver().query(this.uri, PROJECTION, null, null, null);
            CharSequence charSequence = null;
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        charSequence = query.getString(query.getColumnIndexOrThrow("_data"));
                    }
                    query.close();
                } catch (Throwable th) {
                    query.close();
                }
            }
            if (TextUtils.isEmpty(charSequence)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to find file path for: ");
                stringBuilder.append(this.uri);
                dataCallback.onLoadFailed(new FileNotFoundException(stringBuilder.toString()));
                return;
            }
            dataCallback.onDataReady(new File(charSequence));
        }

        @NonNull
        public Class<File> getDataClass() {
            return File.class;
        }

        @NonNull
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public MediaStoreFileLoader(Context context) {
        this.context = context;
    }

    public LoadData<File> buildLoadData(@NonNull Uri uri, int i, int i2, @NonNull Options options) {
        return new LoadData(new ObjectKey(uri), new FilePathFetcher(this.context, uri));
    }

    public boolean handles(@NonNull Uri uri) {
        return MediaStoreUtil.isMediaStoreUri(uri);
    }
}
