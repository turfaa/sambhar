package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class Media {
    @SerializedName("image")
    public final Image image;
    @SerializedName("media_id")
    public final long mediaId;
    @SerializedName("media_id_string")
    public final String mediaIdString;
    @SerializedName("size")
    public final long size;

    public Media(long j, String str, long j2, Image image) {
        this.mediaId = j;
        this.mediaIdString = str;
        this.size = j2;
        this.image = image;
    }
}
