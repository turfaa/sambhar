package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.Identifiable;
import java.util.List;

public class TimelineCursor {
    public final Long maxPosition;
    public final Long minPosition;

    public TimelineCursor(Long l, Long l2) {
        this.minPosition = l;
        this.maxPosition = l2;
    }

    TimelineCursor(List<? extends Identifiable> list) {
        Long l = null;
        this.minPosition = list.size() > 0 ? Long.valueOf(((Identifiable) list.get(list.size() - 1)).getId()) : null;
        if (list.size() > 0) {
            l = Long.valueOf(((Identifiable) list.get(0)).getId());
        }
        this.maxPosition = l;
    }
}
