package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class LoginFormBody {
    @SerializedName("password")
    private String password;
    @SerializedName("username")
    private String username;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String str) {
        this.username = str;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String str) {
        this.password = str;
    }
}
