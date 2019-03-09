package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Key;
import java.security.MessageDigest;

final class DataCacheKey implements Key {
    private final Key signature;
    private final Key sourceKey;

    DataCacheKey(Key key, Key key2) {
        this.sourceKey = key;
        this.signature = key2;
    }

    /* Access modifiers changed, original: 0000 */
    public Key getSourceKey() {
        return this.sourceKey;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DataCacheKey)) {
            return false;
        }
        DataCacheKey dataCacheKey = (DataCacheKey) obj;
        if (this.sourceKey.equals(dataCacheKey.sourceKey) && this.signature.equals(dataCacheKey.signature)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.sourceKey.hashCode() * 31) + this.signature.hashCode();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DataCacheKey{sourceKey=");
        stringBuilder.append(this.sourceKey);
        stringBuilder.append(", signature=");
        stringBuilder.append(this.signature);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        this.sourceKey.updateDiskCacheKey(messageDigest);
        this.signature.updateDiskCacheKey(messageDigest);
    }
}
