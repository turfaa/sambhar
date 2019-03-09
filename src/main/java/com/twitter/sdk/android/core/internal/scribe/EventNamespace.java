package com.twitter.sdk.android.core.internal.scribe;

import com.google.gson.annotations.SerializedName;

public class EventNamespace {
    @SerializedName("action")
    public final String action;
    @SerializedName("client")
    public final String client;
    @SerializedName("component")
    public final String component;
    @SerializedName("element")
    public final String element;
    @SerializedName("page")
    public final String page;
    @SerializedName("section")
    public final String section;

    public static class Builder {
        private String action;
        private String client;
        private String component;
        private String element;
        private String page;
        private String section;

        public Builder setClient(String str) {
            this.client = str;
            return this;
        }

        public Builder setPage(String str) {
            this.page = str;
            return this;
        }

        public Builder setSection(String str) {
            this.section = str;
            return this;
        }

        public Builder setComponent(String str) {
            this.component = str;
            return this;
        }

        public Builder setElement(String str) {
            this.element = str;
            return this;
        }

        public Builder setAction(String str) {
            this.action = str;
            return this;
        }

        public EventNamespace builder() {
            return new EventNamespace(this.client, this.page, this.section, this.component, this.element, this.action);
        }
    }

    public EventNamespace(String str, String str2, String str3, String str4, String str5, String str6) {
        this.client = str;
        this.page = str2;
        this.section = str3;
        this.component = str4;
        this.element = str5;
        this.action = str6;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("client=");
        stringBuilder.append(this.client);
        stringBuilder.append(", page=");
        stringBuilder.append(this.page);
        stringBuilder.append(", section=");
        stringBuilder.append(this.section);
        stringBuilder.append(", component=");
        stringBuilder.append(this.component);
        stringBuilder.append(", element=");
        stringBuilder.append(this.element);
        stringBuilder.append(", action=");
        stringBuilder.append(this.action);
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EventNamespace eventNamespace = (EventNamespace) obj;
        if (!this.action == null ? this.action.equals(eventNamespace.action) : eventNamespace.action == null) {
            return false;
        }
        if (!this.client == null ? this.client.equals(eventNamespace.client) : eventNamespace.client == null) {
            return false;
        }
        if (!this.component == null ? this.component.equals(eventNamespace.component) : eventNamespace.component == null) {
            return false;
        }
        if (!this.element == null ? this.element.equals(eventNamespace.element) : eventNamespace.element == null) {
            return false;
        }
        if (this.page == null ? eventNamespace.page == null : this.page.equals(eventNamespace.page)) {
            return this.section == null ? eventNamespace.section == null : this.section.equals(eventNamespace.section);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((((this.client != null ? this.client.hashCode() : 0) * 31) + (this.page != null ? this.page.hashCode() : 0)) * 31) + (this.section != null ? this.section.hashCode() : 0)) * 31) + (this.component != null ? this.component.hashCode() : 0)) * 31) + (this.element != null ? this.element.hashCode() : 0)) * 31;
        if (this.action != null) {
            i = this.action.hashCode();
        }
        return hashCode + i;
    }
}
