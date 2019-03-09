package com.sambhar.sambharappreport.entity;

import com.google.gson.annotations.SerializedName;

public class ProfileEntity {
    @SerializedName("companies")
    private String companies;
    @SerializedName("fb_account")
    private String facebookAccount;
    @SerializedName("group")
    private String group;
    @SerializedName("insta_account")
    private String instagramAccount;
    @SerializedName("tw_account")
    private String twitterAccount;
    @SerializedName("username")
    private String username;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String str) {
        this.username = str;
    }

    public String getFacebookAccount() {
        return this.facebookAccount;
    }

    public void setFacebookAccount(String str) {
        this.facebookAccount = str;
    }

    public String getInstagramAccount() {
        return this.instagramAccount;
    }

    public void setInstagramAccount(String str) {
        this.instagramAccount = str;
    }

    public String getTwitterAccount() {
        return this.twitterAccount;
    }

    public void setTwitterAccount(String str) {
        this.twitterAccount = str;
    }

    public String getCompanies() {
        return this.companies;
    }

    public void setCompanies(String str) {
        this.companies = str;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String str) {
        this.group = str;
    }
}
