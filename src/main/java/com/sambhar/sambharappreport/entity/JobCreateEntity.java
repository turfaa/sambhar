package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class JobCreateEntity {
    @SerializedName("counts")
    private SocialShareCount count;
    @SerializedName("job_id")
    private int jobId;

    public int getJobId() {
        return this.jobId;
    }

    public void setJobId(int i) {
        this.jobId = i;
    }

    public SocialShareCount getCount() {
        return this.count;
    }

    public void setCount(SocialShareCount socialShareCount) {
        this.count = socialShareCount;
    }
}
