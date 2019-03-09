package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class SocialShareCount {
    @SerializedName("fb")
    private int fbCount;
    @SerializedName("instagram")
    private int instagramCount;
    @SerializedName("twitter")
    private int twitterCount;

    public int getFbCount() {
        return this.fbCount;
    }

    public void setFbCount(int i) {
        this.fbCount = i;
    }

    public int getTwitterCount() {
        return this.twitterCount;
    }

    public void setTwitterCount(int i) {
        this.twitterCount = i;
    }

    public int getInstagramCount() {
        return this.instagramCount;
    }

    public void setInstagramCount(int i) {
        this.instagramCount = i;
    }
}
