package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.ProfileEntity;

public class ProfileResponse {
    @SerializedName("result")
    private ProfileEntity profileEntity;

    public ProfileEntity getProfileEntity() {
        return this.profileEntity;
    }

    public void setProfileEntity(ProfileEntity profileEntity) {
        this.profileEntity = profileEntity;
    }
}
