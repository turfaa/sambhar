package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.LoginEntity;

public class LoginResponse {
    @SerializedName("result")
    private LoginEntity loginEntity;

    public LoginEntity getLoginEntity() {
        return this.loginEntity;
    }

    public void setLoginEntity(LoginEntity loginEntity) {
        this.loginEntity = loginEntity;
    }
}
