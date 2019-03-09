package com.sambhar.sambharappreport.entity.response;

import com.google.gson.annotations.SerializedName;
import com.sambhar.sambharappreport.entity.NotifyEntity;

public class NotifyResponse {
    @SerializedName("result")
    private NotifyEntity entity;
    @SerializedName("message")
    private String messageFromServer;

    public NotifyEntity getEntity() {
        return this.entity;
    }

    public void setEntity(NotifyEntity notifyEntity) {
        this.entity = notifyEntity;
    }

    public String getMessageFromServer() {
        return this.messageFromServer;
    }

    public void setMessageFromServer(String str) {
        this.messageFromServer = str;
    }
}
