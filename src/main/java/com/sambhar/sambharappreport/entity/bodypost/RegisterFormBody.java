package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class RegisterFormBody {
    @SerializedName("facebook")
    private String facebook;
    private String group;
    @SerializedName("group_id")
    private int groupId;
    @SerializedName("instagram")
    private String instagram;
    @SerializedName("password")
    private String password;
    private String province;
    @SerializedName("company_id")
    private int provinceId;
    @SerializedName("twitter")
    private String twitter;
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

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String str) {
        this.province = str;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String str) {
        this.group = str;
    }

    public String getFacebook() {
        return this.facebook;
    }

    public void setFacebook(String str) {
        this.facebook = str;
    }

    public String getTwitter() {
        return this.twitter;
    }

    public void setTwitter(String str) {
        this.twitter = str;
    }

    public String getInstagram() {
        return this.instagram;
    }

    public void setInstagram(String str) {
        this.instagram = str;
    }

    public int getProvinceId() {
        return this.provinceId;
    }

    public void setProvinceId(int i) {
        this.provinceId = i;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int i) {
        this.groupId = i;
    }
}
