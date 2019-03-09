package com.twitter.sdk.android.tweetui;

import java.util.List;

public class TimelineResult<T> {
    public final List<T> items;
    public final TimelineCursor timelineCursor;

    public TimelineResult(TimelineCursor timelineCursor, List<T> list) {
        this.timelineCursor = timelineCursor;
        this.items = list;
    }
}
