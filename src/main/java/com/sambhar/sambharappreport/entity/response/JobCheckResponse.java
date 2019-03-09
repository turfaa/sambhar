package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.JobTimeEntity;

public class JobCheckResponse {
    @SerializedName("result")
    private JobTimeEntity jobTimeEntity;
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

    public JobTimeEntity getJobTimeEntity() {
        return this.jobTimeEntity;
    }

    public void setJobTimeEntity(JobTimeEntity jobTimeEntity) {
        this.jobTimeEntity = jobTimeEntity;
    }
}
