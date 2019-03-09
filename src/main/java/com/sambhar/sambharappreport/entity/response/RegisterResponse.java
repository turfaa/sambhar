package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private int status;

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }
}
