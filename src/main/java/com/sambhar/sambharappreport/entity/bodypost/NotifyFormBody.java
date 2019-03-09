package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class NotifyFormBody {
    @SerializedName("text")
    private String caption;
    @SerializedName("filename")
    private String fileName;
    @SerializedName("job_id")
    private int jobId;
    @SerializedName("source")
    private String source;
    @SerializedName("ts")
    private String ts;
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

    public String getSource() {
        return this.source;
    }

    public void setSource(String str) {
        this.source = str;
    }

    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String str) {
        this.caption = str;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String str) {
        this.fileName = str;
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

    public String getTs() {
        return this.ts;
    }

    public void setTs(String str) {
        this.ts = str;
    }
}
