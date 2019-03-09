package com.sambhar.sambharappreport.rest;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.Image;

public class MediaTwitterEntity {
    @SerializedName("image")
    public Image image;
    @SerializedName("processing_info")
    public ProcessingInfo info;
    @SerializedName("media_id")
    public long mediaId;
    @SerializedName("media_id_string")
    public String mediaIdString;
    @SerializedName("size")
    public long size;

    public long getMediaId() {
        return this.mediaId;
    }

    public void setMediaId(long j) {
        this.mediaId = j;
    }

    public String getMediaIdString() {
        return this.mediaIdString;
    }

    public void setMediaIdString(String str) {
        this.mediaIdString = str;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ProcessingInfo getInfo() {
        return this.info;
    }

    public void setInfo(ProcessingInfo processingInfo) {
        this.info = processingInfo;
    }
}
