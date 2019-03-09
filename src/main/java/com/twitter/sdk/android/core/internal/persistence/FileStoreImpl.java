package com.twitter.sdk.android.core.internal.persistence;

import android.content.Context;
import android.os.Environment;
import com.twitter.sdk.android.core.Twitter;
import java.io.File;

public class FileStoreImpl implements FileStore {
    private final Context context;

    public FileStoreImpl(Context context) {
        if (context != null) {
            this.context = context;
            return;
        }
        throw new IllegalArgumentException("Context must not be null");
    }

    public File getCacheDir() {
        return prepare(this.context.getCacheDir());
    }

    public File getExternalCacheDir() {
        if (isExternalStorageAvailable()) {
            return prepare(this.context.getExternalCacheDir());
        }
        return prepare(null);
    }

    public File getFilesDir() {
        return prepare(this.context.getFilesDir());
    }

    public File getExternalFilesDir() {
        if (isExternalStorageAvailable()) {
            return prepare(this.context.getExternalFilesDir(null));
        }
        return prepare(null);
    }

    /* Access modifiers changed, original: 0000 */
    public File prepare(File file) {
        if (file == null) {
            Twitter.getLogger().d("Twitter", "Null File");
        } else if (file.exists() || file.mkdirs()) {
            return file;
        } else {
            Twitter.getLogger().w("Twitter", "Couldn't create file");
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isExternalStorageAvailable() {
        if ("mounted".equals(Environment.getExternalStorageState())) {
            return true;
        }
        Twitter.getLogger().w("Twitter", "External Storage is not mounted and/or writable\nHave you declared android.permission.WRITE_EXTERNAL_STORAGE in the manifest?");
        return false;
    }
}
