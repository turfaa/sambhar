package com.bumptech.glide.load.resource.drawable;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import java.util.List;

public class ResourceDrawableDecoder implements ResourceDecoder<Uri, Drawable> {
    private static final int ID_PATH_SEGMENTS = 1;
    private static final int NAME_PATH_SEGMENT_INDEX = 1;
    private static final int NAME_URI_PATH_SEGMENTS = 2;
    private static final int RESOURCE_ID_SEGMENT_INDEX = 0;
    private static final int TYPE_PATH_SEGMENT_INDEX = 0;
    private final Context context;

    public ResourceDrawableDecoder(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean handles(@NonNull Uri uri, @NonNull Options options) {
        return uri.getScheme().equals("android.resource");
    }

    @Nullable
    public Resource<Drawable> decode(@NonNull Uri uri, int i, int i2, @NonNull Options options) {
        i = loadResourceIdFromUri(uri);
        String authority = uri.getAuthority();
        return NonOwnedDrawableResource.newInstance(DrawableDecoderCompat.getDrawable(this.context, authority.equals(this.context.getPackageName()) ? this.context : getContextForPackage(uri, authority), i));
    }

    @NonNull
    private Context getContextForPackage(Uri uri, String str) {
        try {
            return this.context.createPackageContext(str, 0);
        } catch (NameNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to obtain context or unrecognized Uri format for: ");
            stringBuilder.append(uri);
            throw new IllegalArgumentException(stringBuilder.toString(), e);
        }
    }

    @DrawableRes
    private int loadResourceIdFromUri(Uri uri) {
        Integer valueOf;
        List pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            String str = (String) pathSegments.get(0);
            String str2 = (String) pathSegments.get(1);
            valueOf = Integer.valueOf(this.context.getResources().getIdentifier(str2, str, uri.getAuthority()));
        } else {
            if (pathSegments.size() == 1) {
                try {
                    valueOf = Integer.valueOf((String) pathSegments.get(0));
                } catch (NumberFormatException unused) {
                }
            }
            valueOf = null;
        }
        StringBuilder stringBuilder;
        if (valueOf == null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unrecognized Uri format: ");
            stringBuilder.append(uri);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (valueOf.intValue() != 0) {
            return valueOf.intValue();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to obtain resource id for: ");
            stringBuilder.append(uri);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }
}
