package com.twitter.sdk.android.core.internal.persistence;

import java.io.File;

public interface FileStore {
    File getCacheDir();

    File getExternalCacheDir();

    File getExternalFilesDir();

    File getFilesDir();
}
