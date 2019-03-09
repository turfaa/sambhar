package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;

public class SearchTimeline extends BaseTimeline implements Timeline<Tweet> {
    static final String FILTER_RETWEETS = " -filter:retweets";
    private static final SimpleDateFormat QUERY_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final String SCRIBE_SECTION = "search";
    final Geocode geocode;
    final String languageCode;
    final Integer maxItemsPerRequest;
    final String query;
    final String resultType;
    final TwitterCore twitterCore;
    final String untilDate;

    public static class Builder {
        private Geocode geocode;
        private String lang;
        private Integer maxItemsPerRequest;
        private String query;
        private String resultType;
        private final TwitterCore twitterCore;
        private String untilDate;

        public Builder() {
            this.resultType = ResultType.FILTERED.type;
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = TwitterCore.getInstance();
        }

        Builder(TwitterCore twitterCore) {
            this.resultType = ResultType.FILTERED.type;
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = twitterCore;
        }

        public Builder query(String str) {
            this.query = str;
            return this;
        }

        public Builder geocode(Geocode geocode) {
            this.geocode = geocode;
            return this;
        }

        public Builder resultType(ResultType resultType) {
            this.resultType = resultType.type;
            return this;
        }

        public Builder languageCode(String str) {
            this.lang = str;
            return this;
        }

        public Builder maxItemsPerRequest(Integer num) {
            this.maxItemsPerRequest = num;
            return this;
        }

        public Builder untilDate(Date date) {
            this.untilDate = SearchTimeline.QUERY_DATE.format(date);
            return this;
        }

        public SearchTimeline build() {
            if (this.query != null) {
                return new SearchTimeline(this.twitterCore, this.query, this.geocode, this.resultType, this.lang, this.maxItemsPerRequest, this.untilDate);
            }
            throw new IllegalStateException("query must not be null");
        }
    }

    public enum ResultType {
        RECENT("recent"),
        POPULAR("popular"),
        MIXED("mixed"),
        FILTERED("filtered");
        
        final String type;

        private ResultType(String str) {
            this.type = str;
        }
    }

    class SearchCallback extends Callback<Search> {
        final Callback<TimelineResult<Tweet>> cb;

        SearchCallback(Callback<TimelineResult<Tweet>> callback) {
            this.cb = callback;
        }

        public void success(Result<Search> result) {
            List list = ((Search) result.data).tweets;
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

    /* Access modifiers changed, original: 0000 */
    public String getTimelineType() {
        return SCRIBE_SECTION;
    }

    SearchTimeline(TwitterCore twitterCore, String str, Geocode geocode, String str2, String str3, Integer num, String str4) {
        String str5;
        this.twitterCore = twitterCore;
        this.languageCode = str3;
        this.maxItemsPerRequest = num;
        this.untilDate = str4;
        this.resultType = str2;
        if (str == null) {
            str5 = null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(FILTER_RETWEETS);
            str5 = stringBuilder.toString();
        }
        this.query = str5;
        this.geocode = geocode;
    }

    public void next(Long l, Callback<TimelineResult<Tweet>> callback) {
        createSearchRequest(l, null).enqueue(new SearchCallback(callback));
    }

    public void previous(Long l, Callback<TimelineResult<Tweet>> callback) {
        createSearchRequest(null, BaseTimeline.decrementMaxId(l)).enqueue(new SearchCallback(callback));
    }

    /* Access modifiers changed, original: 0000 */
    public Call<Search> createSearchRequest(Long l, Long l2) {
        return this.twitterCore.getApiClient().getSearchService().tweets(this.query, this.geocode, this.languageCode, null, this.resultType, this.maxItemsPerRequest, this.untilDate, l, l2, Boolean.valueOf(true));
    }
}
