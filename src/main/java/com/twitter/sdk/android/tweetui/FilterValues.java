package com.twitter.sdk.android.tweetui;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.ModelUtils;
import java.util.List;

public class FilterValues {
    @SerializedName("handles")
    public final List<String> handles;
    @SerializedName("hashtags")
    public final List<String> hashtags;
    @SerializedName("keywords")
    public final List<String> keywords;
    @SerializedName("urls")
    public final List<String> urls;

    private FilterValues() {
        this(null, null, null, null);
    }

    public FilterValues(List<String> list, List<String> list2, List<String> list3, List<String> list4) {
        this.keywords = ModelUtils.getSafeList(list);
        this.hashtags = ModelUtils.getSafeList(list2);
        this.handles = ModelUtils.getSafeList(list3);
        this.urls = ModelUtils.getSafeList(list4);
    }
}
