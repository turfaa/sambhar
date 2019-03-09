package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class Image {
    @SerializedName("h")
    public final int h;
    @SerializedName("image_type")
    public final String imageType;
    @SerializedName("w")
    public final int w;

    public Image(int i, int i2, String str) {
        this.w = i;
        this.h = i2;
        this.imageType = str;
    }
}
