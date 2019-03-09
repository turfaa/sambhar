package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class JobCheckBodyPost {
    @SerializedName("job_id")
    private int jobId;
    @SerializedName("ta")
    private String twitterAccessToken;
    @SerializedName("tc")
    private String twitterSecrets;

    public int getJobId() {
        return this.jobId;
    }

    public void setJobId(int i) {
        this.jobId = i;
    }

    public String getTwitterAccessToken() {
        return this.twitterAccessToken;
    }

    public void setTwitterAccessToken(String str) {
        this.twitterAccessToken = str;
    }

    public String getTwitterSecrets() {
        return this.twitterSecrets;
    }

    public void setTwitterSecrets(String str) {
        this.twitterSecrets = str;
    }
}
