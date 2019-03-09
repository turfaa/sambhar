package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.List;

abstract class BaseTimeline {

    static class TweetsCallback extends Callback<List<Tweet>> {
        final Callback<TimelineResult<Tweet>> cb;

        TweetsCallback(Callback<TimelineResult<Tweet>> callback) {
            this.cb = callback;
        }

        public void success(Result<List<Tweet>> result) {
            List list = (List) result.data;
            TimelineResult timelineResult = new TimelineResult(new TimelineCursor(list), list);
            if (this.cb != null) {
                this.cb.success(new Result(timelineResult, result.response));
            }
        }

        public void failure(TwitterException twitterException) {
            if (this.cb != null) {
                this.cb.failure(twitterException);
            }
        }
    }

    public abstract String getTimelineType();

    BaseTimeline() {
    }

    static Long decrementMaxId(Long l) {
        return l == null ? null : Long.valueOf(l.longValue() - 1);
    }
}
