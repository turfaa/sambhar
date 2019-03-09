package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class VideoInfo implements Serializable {
    @SerializedName("aspect_ratio")
    public final List<Integer> aspectRatio;
    @SerializedName("duration_millis")
    public final long durationMillis;
    @SerializedName("variants")
    public final List<Variant> variants;

    public static class Variant implements Serializable {
        @SerializedName("bitrate")
        public final long bitrate;
        @SerializedName("content_type")
        public final String contentType;
        @SerializedName("url")
        public final String url;

        public Variant(long j, String str, String str2) {
            this.bitrate = j;
            this.contentType = str;
            this.url = str2;
        }
    }

    private VideoInfo() {
        this(null, 0, null);
    }

    public VideoInfo(List<Integer> list, long j, List<Variant> list2) {
        this.aspectRatio = ModelUtils.getSafeList(list);
        this.durationMillis = j;
        this.variants = ModelUtils.getSafeList(list2);
    }
}
