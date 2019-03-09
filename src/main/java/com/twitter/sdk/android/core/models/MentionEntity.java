package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class MentionEntity extends Entity {
    @SerializedName("id")
    public final long id;
    @SerializedName("id_str")
    public final String idStr;
    @SerializedName("name")
    public final String name;
    @SerializedName("screen_name")
    public final String screenName;

    public /* bridge */ /* synthetic */ int getEnd() {
        return super.getEnd();
    }

    public /* bridge */ /* synthetic */ int getStart() {
        return super.getStart();
    }

    public MentionEntity(long j, String str, String str2, String str3, int i, int i2) {
        super(i, i2);
        this.id = j;
        this.idStr = str;
        this.name = str2;
        this.screenName = str3;
    }
}
