package com.twitter.sdk.android.core.internal.scribe;

public class ScribeConfig {
    public static final String BASE_URL = "https://api.twitter.com";
    public static final int DEFAULT_MAX_FILES_TO_KEEP = 100;
    public static final int DEFAULT_SEND_INTERVAL_SECONDS = 600;
    public final String baseUrl;
    public final boolean isEnabled;
    public final int maxFilesToKeep;
    public final String pathType;
    public final String pathVersion;
    public final int sendIntervalSeconds;
    public final String sequence;
    public final String userAgent;

    public ScribeConfig(boolean z, String str, String str2, String str3, String str4, String str5, int i, int i2) {
        this.isEnabled = z;
        this.baseUrl = str;
        this.pathVersion = str2;
        this.pathType = str3;
        this.sequence = str4;
        this.userAgent = str5;
        this.maxFilesToKeep = i;
        this.sendIntervalSeconds = i2;
    }
}
