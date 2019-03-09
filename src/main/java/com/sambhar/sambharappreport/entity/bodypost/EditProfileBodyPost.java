package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class EditProfileBodyPost {
    @SerializedName("company_id")
    private int companyId;
    @SerializedName("fb_account")
    private String fbAccount;
    @SerializedName("group")
    private String group;
    @SerializedName("group_id")
    private int groupId;
    @SerializedName("insta_account")
    private String instagramAccount;
    @SerializedName("province")
    private String province;
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

    public int getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(int i) {
        this.companyId = i;
    }

    public String getProvince() {
        return this.province;
    }

    public void setProvince(String str) {
        this.province = str;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int i) {
        this.groupId = i;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String str) {
        this.group = str;
    }

    public String getTwitterAccount() {
        return this.twitterAccount;
    }

    public void setTwitterAccount(String str) {
        this.twitterAccount = str;
    }

    public String getInstagramAccount() {
        return this.instagramAccount;
    }

    public void setInstagramAccount(String str) {
        this.instagramAccount = str;
    }

    public String getFbAccount() {
        return this.fbAccount;
    }

    public void setFbAccount(String str) {
        this.fbAccount = str;
    }
}
