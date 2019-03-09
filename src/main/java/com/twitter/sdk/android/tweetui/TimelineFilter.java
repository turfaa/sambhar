package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.Tweet;
import java.util.List;

public interface TimelineFilter {
    List<Tweet> filter(List<Tweet> list);

    int totalFilters();
}
