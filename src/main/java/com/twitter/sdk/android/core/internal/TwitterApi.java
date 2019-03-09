package com.twitter.sdk.android.core.internal;

import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Build.VERSION;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class TwitterApi {
    public static final String BASE_HOST = "api.twitter.com";
    public static final String BASE_HOST_URL = "https://api.twitter.com";
    private final String baseHostUrl;

    public TwitterApi() {
        this("https://api.twitter.com");
    }

    public TwitterApi(String str) {
        this.baseHostUrl = str;
    }

    public String getBaseHostUrl() {
        return this.baseHostUrl;
    }

    public Builder buildUponBaseHostUrl(String... strArr) {
        Builder buildUpon = Uri.parse(getBaseHostUrl()).buildUpon();
        if (strArr != null) {
            for (String appendPath : strArr) {
                buildUpon.appendPath(appendPath);
            }
        }
        return buildUpon;
    }

    public static String buildUserAgent(String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.append('/');
        stringBuilder.append(str2);
        stringBuilder.append(' ');
        stringBuilder.append(Build.MODEL);
        stringBuilder.append('/');
        stringBuilder.append(VERSION.RELEASE);
        stringBuilder.append(" (");
        stringBuilder.append(Build.MANUFACTURER);
        stringBuilder.append(';');
        stringBuilder.append(Build.MODEL);
        stringBuilder.append(';');
        stringBuilder.append(Build.BRAND);
        stringBuilder.append(';');
        stringBuilder.append(Build.PRODUCT);
        stringBuilder.append(')');
        return normalizeString(stringBuilder.toString());
    }

    static String normalizeString(String str) {
        return stripNonAscii(Normalizer.normalize(str, Form.NFD));
    }

    static String stripNonAscii(String str) {
        StringBuilder stringBuilder = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt > 31 && charAt < 127) {
                stringBuilder.append(charAt);
            }
        }
        return stringBuilder.toString();
    }
}
