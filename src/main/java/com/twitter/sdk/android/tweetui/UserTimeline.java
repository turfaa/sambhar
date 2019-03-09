package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.List;
import retrofit2.Call;

public class UserTimeline extends BaseTimeline implements Timeline<Tweet> {
    private static final String SCRIBE_SECTION = "user";
    final Boolean includeReplies;
    final Boolean includeRetweets;
    final Integer maxItemsPerRequest;
    final String screenName;
    final TwitterCore twitterCore;
    final Long userId;

    public static class Builder {
        private Boolean includeReplies;
        private Boolean includeRetweets;
        private Integer maxItemsPerRequest;
        private String screenName;
        private final TwitterCore twitterCore;
        private Long userId;

        public Builder() {
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = TwitterCore.getInstance();
        }

        Builder(TwitterCore twitterCore) {
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = twitterCore;
        }

        public Builder userId(Long l) {
            this.userId = l;
            return this;
        }

        public Builder screenName(String str) {
            this.screenName = str;
            return this;
        }

        public Builder maxItemsPerRequest(Integer num) {
            this.maxItemsPerRequest = num;
            return this;
        }

        public Builder includeReplies(Boolean bool) {
            this.includeReplies = bool;
            return this;
        }

        public Builder includeRetweets(Boolean bool) {
            this.includeRetweets = bool;
            return this;
        }

        public UserTimeline build() {
            return new UserTimeline(this.twitterCore, this.userId, this.screenName, this.maxItemsPerRequest, this.includeReplies, this.includeRetweets);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String getTimelineType() {
        return SCRIBE_SECTION;
    }

    UserTimeline(TwitterCore twitterCore, Long l, String str, Integer num, Boolean bool, Boolean bool2) {
        boolean z;
        this.twitterCore = twitterCore;
        this.userId = l;
        this.screenName = str;
        this.maxItemsPerRequest = num;
        if (bool == null) {
            z = false;
        } else {
            z = bool.booleanValue();
        }
        this.includeReplies = Boolean.valueOf(z);
        this.includeRetweets = bool2;
    }

    public void next(Long l, Callback<TimelineResult<Tweet>> callback) {
        createUserTimelineRequest(l, null).enqueue(new TweetsCallback(callback));
    }

    public void previous(Long l, Callback<TimelineResult<Tweet>> callback) {
        createUserTimelineRequest(null, BaseTimeline.decrementMaxId(l)).enqueue(new TweetsCallback(callback));
    }

    /* Access modifiers changed, original: 0000 */
    public Call<List<Tweet>> createUserTimelineRequest(Long l, Long l2) {
        return this.twitterCore.getApiClient().getStatusesService().userTimeline(this.userId, this.screenName, this.maxItemsPerRequest, l, l2, Boolean.valueOf(false), Boolean.valueOf(this.includeReplies.booleanValue() ^ 1), null, this.includeRetweets);
    }
}
