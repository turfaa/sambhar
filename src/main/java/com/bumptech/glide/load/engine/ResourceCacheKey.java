package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

final class ResourceCacheKey implements Key {
    private static final LruCache<Class<?>, byte[]> RESOURCE_CLASS_BYTES = new LruCache(50);
    private final ArrayPool arrayPool;
    private final Class<?> decodedResourceClass;
    private final int height;
    private final Options options;
    private final Key signature;
    private final Key sourceKey;
    private final Transformation<?> transformation;
    private final int width;

    ResourceCacheKey(ArrayPool arrayPool, Key key, Key key2, int i, int i2, Transformation<?> transformation, Class<?> cls, Options options) {
        this.arrayPool = arrayPool;
        this.sourceKey = key;
        this.signature = key2;
        this.width = i;
        this.height = i2;
        this.transformation = transformation;
        this.decodedResourceClass = cls;
        this.options = options;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof ResourceCacheKey)) {
            return false;
        }
        ResourceCacheKey resourceCacheKey = (ResourceCacheKey) obj;
        if (this.height == resourceCacheKey.height && this.width == resourceCacheKey.width && Util.bothNullOrEqual(this.transformation, resourceCacheKey.transformation) && this.decodedResourceClass.equals(resourceCacheKey.decodedResourceClass) && this.sourceKey.equals(resourceCacheKey.sourceKey) && this.signature.equals(resourceCacheKey.signature) && this.options.equals(resourceCacheKey.options)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int hashCode = (((((this.sourceKey.hashCode() * 31) + this.signature.hashCode()) * 31) + this.width) * 31) + this.height;
        if (this.transformation != null) {
            hashCode = (hashCode * 31) + this.transformation.hashCode();
        }
        return (((hashCode * 31) + this.decodedResourceClass.hashCode()) * 31) + this.options.hashCode();
    }

    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        byte[] bArr = (byte[]) this.arrayPool.getExact(8, byte[].class);
        ByteBuffer.wrap(bArr).putInt(this.width).putInt(this.height).array();
        this.signature.updateDiskCacheKey(messageDigest);
        this.sourceKey.updateDiskCacheKey(messageDigest);
        messageDigest.update(bArr);
        if (this.transformation != null) {
            this.transformation.updateDiskCacheKey(messageDigest);
        }
        this.options.updateDiskCacheKey(messageDigest);
        messageDigest.update(getResourceClassBytes());
        this.arrayPool.put(bArr);
    }

    private byte[] getResourceClassBytes() {
        byte[] bArr = (byte[]) RESOURCE_CLASS_BYTES.get(this.decodedResourceClass);
        if (bArr != null) {
            return bArr;
        }
        bArr = this.decodedResourceClass.getName().getBytes(CHARSET);
        RESOURCE_CLASS_BYTES.put(this.decodedResourceClass, bArr);
        return bArr;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ResourceCacheKey{sourceKey=");
        stringBuilder.append(this.sourceKey);
        stringBuilder.append(", signature=");
        stringBuilder.append(this.signature);
        stringBuilder.append(", width=");
        stringBuilder.append(this.width);
        stringBuilder.append(", height=");
        stringBuilder.append(this.height);
        stringBuilder.append(", decodedResourceClass=");
        stringBuilder.append(this.decodedResourceClass);
        stringBuilder.append(", transformation='");
        stringBuilder.append(this.transformation);
        stringBuilder.append('\'');
        stringBuilder.append(", options=");
        stringBuilder.append(this.options);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
