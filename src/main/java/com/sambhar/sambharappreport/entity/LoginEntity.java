package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class LoginEntity {
    @SerializedName("counts")
    private SocialShareCount count;
    @SerializedName("fb")
    private int fbStatus;
    @SerializedName("instagram")
    private int instagramStatus;
    @SerializedName("name")
    private String name;
    @SerializedName("token")
    private String token;
    @SerializedName("twitter")
    private int twitterStatus;

    public String getToken() {
        return this.token;
    }

    public void setToken(String str) {
        this.token = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public int getFbStatus() {
        return this.fbStatus;
    }

    public void setFbStatus(int i) {
        this.fbStatus = i;
    }

    public int getTwitterStatus() {
        return this.twitterStatus;
    }

    public void setTwitterStatus(int i) {
        this.twitterStatus = i;
    }

    public int getInstagramStatus() {
        return this.instagramStatus;
    }

    public void setInstagramStatus(int i) {
        this.instagramStatus = i;
    }

    public SocialShareCount getCount() {
        return this.count;
    }

    public void setCount(SocialShareCount socialShareCount) {
        this.count = socialShareCount;
    }
}
