package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.JobCreateEntity;

public class JobCreateResponse {
    @SerializedName("result")
    private JobCreateEntity entity;

    public JobCreateEntity getEntity() {
        return this.entity;
    }

    public void setEntity(JobCreateEntity jobCreateEntity) {
        this.entity = jobCreateEntity;
    }
}
