package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserEntities {
    @SerializedName("description")
    public final UrlEntities description;
    @SerializedName("url")
    public final UrlEntities url;

    public static class UrlEntities {
        @SerializedName("urls")
        public final List<UrlEntity> urls;

        private UrlEntities() {
            this(null);
        }

        public UrlEntities(List<UrlEntity> list) {
            this.urls = ModelUtils.getSafeList(list);
        }
    }

    public UserEntities(UrlEntities urlEntities, UrlEntities urlEntities2) {
        this.url = urlEntities;
        this.description = urlEntities2;
    }
}
