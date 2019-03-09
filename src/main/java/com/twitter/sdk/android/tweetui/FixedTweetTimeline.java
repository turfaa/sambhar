package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedTweetTimeline extends BaseTimeline implements Timeline<Tweet> {
    private static final String SCRIBE_SECTION = "fixed";
    final List<Tweet> tweets;

    public static class Builder {
        private List<Tweet> tweets;

        public Builder setTweets(List<Tweet> list) {
            this.tweets = list;
            return this;
        }

        public FixedTweetTimeline build() {
            return new FixedTweetTimeline(this.tweets);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String getTimelineType() {
        return SCRIBE_SECTION;
    }

    FixedTweetTimeline(List<Tweet> list) {
        List list2;
        if (list2 == null) {
            list2 = new ArrayList();
        }
        this.tweets = list2;
    }

    public void next(Long l, Callback<TimelineResult<Tweet>> callback) {
        callback.success(new Result(new TimelineResult(new TimelineCursor(this.tweets), this.tweets), null));
    }

    public void previous(Long l, Callback<TimelineResult<Tweet>> callback) {
        List emptyList = Collections.emptyList();
        callback.success(new Result(new TimelineResult(new TimelineCursor(emptyList), emptyList), null));
    }
}
