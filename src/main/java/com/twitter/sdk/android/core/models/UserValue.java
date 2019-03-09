package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class UserValue {
    @SerializedName("id_str")
    public final String idStr;

    public UserValue(String str) {
        this.idStr = str;
    }
}
