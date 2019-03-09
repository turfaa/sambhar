package com.bumptech.glide.load.data;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;

public class StreamAssetPathFetcher extends AssetPathFetcher<InputStream> {
    public StreamAssetPathFetcher(AssetManager assetManager, String str) {
        super(assetManager, str);
    }

    /* Access modifiers changed, original: protected */
    public InputStream loadResource(AssetManager assetManager, String str) throws IOException {
        return assetManager.open(str);
    }

    /* Access modifiers changed, original: protected */
    public void close(InputStream inputStream) throws IOException {
        inputStream.close();
    }

    @NonNull
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
