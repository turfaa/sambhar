package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class HashtagEntity extends Entity {
    @SerializedName("text")
    public final String text;

    public /* bridge */ /* synthetic */ int getEnd() {
        return super.getEnd();
    }

    public /* bridge */ /* synthetic */ int getStart() {
        return super.getStart();
    }

    public HashtagEntity(String str, int i, int i2) {
        super(i, i2);
        this.text = str;
    }
}
