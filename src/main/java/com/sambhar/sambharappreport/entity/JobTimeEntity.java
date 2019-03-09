package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class JobTimeEntity {
    @SerializedName("t")
    private String apiConsumerKey;
    @SerializedName("c")
    private String apiConsumerSecret;
    @SerializedName("ts")
    private String ts;

    public String getTs() {
        return this.ts;
    }

    public void setTs(String str) {
        this.ts = str;
    }

    public String getApiConsumerKey() {
        return this.apiConsumerKey;
    }

    public void setApiConsumerKey(String str) {
        this.apiConsumerKey = str;
    }

    public String getApiConsumerSecret() {
        return this.apiConsumerSecret;
    }

    public void setApiConsumerSecret(String str) {
        this.apiConsumerSecret = str;
    }
}
