package com.sambhar.sambharappreport.rest;

import com.google.gson.annotations.SerializedName;

public class ProcessingInfo {
    @SerializedName("state")
    private String state;

    public String getState() {
        return this.state;
    }

    public void setState(String str) {
        this.state = str;
    }
}
