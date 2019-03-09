package com.sambhar.sambharappreport.entity.bodypost;

import com.google.gson.annotations.SerializedName;

public class UpdatePasswordFormBody {
    @SerializedName("new_password")
    private String newPassword;
    @SerializedName("old_password")
    private String oldPassword;

    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String str) {
        this.oldPassword = str;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String str) {
        this.newPassword = str;
    }
}
