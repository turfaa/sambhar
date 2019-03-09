package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class NotifyEntity {
    @SerializedName("counts")
    private SocialShareCount shareCount;

    public SocialShareCount getShareCount() {
        return this.shareCount;
    }

    public void setShareCount(SocialShareCount socialShareCount) {
        this.shareCount = socialShareCount;
    }
}
