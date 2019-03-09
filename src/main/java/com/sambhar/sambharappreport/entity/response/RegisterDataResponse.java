package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.RegisterDataEntity;

public class RegisterDataResponse {
    @SerializedName("result")
    private RegisterDataEntity entity;

    public RegisterDataEntity getEntity() {
        return this.entity;
    }

    public void setEntity(RegisterDataEntity registerDataEntity) {
        this.entity = registerDataEntity;
    }
}
