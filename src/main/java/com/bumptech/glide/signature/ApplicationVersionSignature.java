package com.bumptech.glide.signature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.bumptech.glide.load.Key;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ApplicationVersionSignature {
    private static final ConcurrentMap<String, Key> PACKAGE_NAME_TO_KEY = new ConcurrentHashMap();
    private static final String TAG = "AppVersionSignature";

    @NonNull
    public static Key obtain(@NonNull Context context) {
        String packageName = context.getPackageName();
        Key key = (Key) PACKAGE_NAME_TO_KEY.get(packageName);
        if (key != null) {
            return key;
        }
        key = obtainVersionSignature(context);
        Key key2 = (Key) PACKAGE_NAME_TO_KEY.putIfAbsent(packageName, key);
        return key2 == null ? key : key2;
    }

    @VisibleForTesting
    static void reset() {
        PACKAGE_NAME_TO_KEY.clear();
    }

    @NonNull
    private static Key obtainVersionSignature(@NonNull Context context) {
        return new ObjectKey(getVersionCode(getPackageInfo(context)));
    }

    @NonNull
    private static String getVersionCode(@Nullable PackageInfo packageInfo) {
        if (packageInfo != null) {
            return String.valueOf(packageInfo.versionCode);
        }
        return UUID.randomUUID().toString();
    }

    @Nullable
    private static PackageInfo getPackageInfo(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot resolve info for");
            stringBuilder.append(context.getPackageName());
            Log.e(str, stringBuilder.toString(), e);
            return null;
        }
    }

    private ApplicationVersionSignature() {
    }
}
