package com.twitter.sdk.android.tweetui;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

class FilterTimelineDelegate extends TimelineDelegate<Tweet> {
    static final String TOTAL_APPLIED_FILTERS_JSON_PROP = "total_filters";
    static final String TWEETS_COUNT_JSON_PROP = "tweet_count";
    static final String TWEETS_FILTERED_JSON_PROP = "tweets_filtered";
    final Gson gson = new Gson();
    final TimelineFilter timelineFilter;
    final TweetUi tweetUi;

    class TimelineFilterCallback extends Callback<TimelineResult<Tweet>> {
        final DefaultCallback callback;
        final ExecutorService executorService = Twitter.getInstance().getExecutorService();
        final Handler handler = new Handler(Looper.getMainLooper());
        final TimelineFilter timelineFilter;

        TimelineFilterCallback(DefaultCallback defaultCallback, TimelineFilter timelineFilter) {
            this.callback = defaultCallback;
            this.timelineFilter = timelineFilter;
        }

        public void success(final Result<TimelineResult<Tweet>> result) {
            this.executorService.execute(new Runnable() {
                public void run() {
                    List filter = TimelineFilterCallback.this.timelineFilter.filter(((TimelineResult) result.data).items);
                    final TimelineResult buildTimelineResult = TimelineFilterCallback.this.buildTimelineResult(((TimelineResult) result.data).timelineCursor, filter);
                    TimelineFilterCallback.this.handler.post(new Runnable() {
                        public void run() {
                            TimelineFilterCallback.this.callback.success(new Result(buildTimelineResult, result.response));
                        }
                    });
                    FilterTimelineDelegate.this.scribeFilteredTimeline(((TimelineResult) result.data).items, filter);
                }
            });
        }

        public void failure(TwitterException twitterException) {
            if (this.callback != null) {
                this.callback.failure(twitterException);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public TimelineResult<Tweet> buildTimelineResult(TimelineCursor timelineCursor, List<Tweet> list) {
            return new TimelineResult(timelineCursor, list);
        }
    }

    public FilterTimelineDelegate(Timeline<Tweet> timeline, TimelineFilter timelineFilter) {
        super(timeline);
        this.timelineFilter = timelineFilter;
        this.tweetUi = TweetUi.getInstance();
    }

    public void refresh(Callback<TimelineResult<Tweet>> callback) {
        this.timelineStateHolder.resetCursors();
        loadNext(this.timelineStateHolder.positionForNext(), new TimelineFilterCallback(new RefreshCallback(callback, this.timelineStateHolder), this.timelineFilter));
    }

    public void next(Callback<TimelineResult<Tweet>> callback) {
        loadNext(this.timelineStateHolder.positionForNext(), new TimelineFilterCallback(new NextCallback(callback, this.timelineStateHolder), this.timelineFilter));
    }

    public void previous() {
        loadPrevious(this.timelineStateHolder.positionForPrevious(), new TimelineFilterCallback(new PreviousCallback(this.timelineStateHolder), this.timelineFilter));
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeFilteredTimeline(List<Tweet> list, List<Tweet> list2) {
        int size = list.size();
        ScribeItem fromMessage = ScribeItem.fromMessage(getJsonMessage(size, size - list2.size(), this.timelineFilter.totalFilters()));
        ArrayList arrayList = new ArrayList();
        arrayList.add(fromMessage);
        this.tweetUi.scribe(ScribeConstants.getTfwClientFilterTimelineNamespace(TweetTimelineListAdapter.getTimelineType(this.timeline)), arrayList);
    }

    private String getJsonMessage(int i, int i2, int i3) {
        JsonElement jsonObject = new JsonObject();
        jsonObject.addProperty(TWEETS_COUNT_JSON_PROP, Integer.valueOf(i));
        jsonObject.addProperty(TWEETS_FILTERED_JSON_PROP, Integer.valueOf(i - i2));
        jsonObject.addProperty(TOTAL_APPLIED_FILTERS_JSON_PROP, Integer.valueOf(i3));
        return this.gson.toJson(jsonObject);
    }
}
