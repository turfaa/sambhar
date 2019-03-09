package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.TwitterCollection;
import com.twitter.sdk.android.core.models.TwitterCollection.TimelineItem;
import com.twitter.sdk.android.core.models.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import retrofit2.Call;

public class CollectionTimeline extends BaseTimeline implements Timeline<Tweet> {
    static final String COLLECTION_PREFIX = "custom-";
    private static final String SCRIBE_SECTION = "collection";
    final String collectionIdentifier;
    final Integer maxItemsPerRequest;
    final TwitterCore twitterCore;

    public static class Builder {
        private Long collectionId;
        private Integer maxItemsPerRequest;
        private final TwitterCore twitterCore;

        public Builder() {
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = TwitterCore.getInstance();
        }

        Builder(TwitterCore twitterCore) {
            this.maxItemsPerRequest = Integer.valueOf(30);
            this.twitterCore = twitterCore;
        }

        public Builder id(Long l) {
            this.collectionId = l;
            return this;
        }

        public Builder maxItemsPerRequest(Integer num) {
            this.maxItemsPerRequest = num;
            return this;
        }

        public CollectionTimeline build() {
            if (this.collectionId != null) {
                return new CollectionTimeline(this.twitterCore, this.collectionId, this.maxItemsPerRequest);
            }
            throw new IllegalStateException("collection id must not be null");
        }
    }

    class CollectionCallback extends Callback<TwitterCollection> {
        final Callback<TimelineResult<Tweet>> cb;

        CollectionCallback(Callback<TimelineResult<Tweet>> callback) {
            this.cb = callback;
        }

        public void success(Result<TwitterCollection> result) {
            Object timelineResult;
            TimelineCursor timelineCursor = CollectionTimeline.getTimelineCursor((TwitterCollection) result.data);
            List orderedTweets = CollectionTimeline.getOrderedTweets((TwitterCollection) result.data);
            if (timelineCursor != null) {
                timelineResult = new TimelineResult(timelineCursor, orderedTweets);
            } else {
                timelineResult = new TimelineResult(null, Collections.emptyList());
            }
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

    CollectionTimeline(TwitterCore twitterCore, Long l, Integer num) {
        if (l == null) {
            this.collectionIdentifier = null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(COLLECTION_PREFIX);
            stringBuilder.append(Long.toString(l.longValue()));
            this.collectionIdentifier = stringBuilder.toString();
        }
        this.twitterCore = twitterCore;
        this.maxItemsPerRequest = num;
    }

    public void next(Long l, Callback<TimelineResult<Tweet>> callback) {
        createCollectionRequest(l, null).enqueue(new CollectionCallback(callback));
    }

    public void previous(Long l, Callback<TimelineResult<Tweet>> callback) {
        createCollectionRequest(null, l).enqueue(new CollectionCallback(callback));
    }

    /* Access modifiers changed, original: 0000 */
    public Call<TwitterCollection> createCollectionRequest(Long l, Long l2) {
        return this.twitterCore.getApiClient().getCollectionService().collection(this.collectionIdentifier, this.maxItemsPerRequest, l2, l);
    }

    static List<Tweet> getOrderedTweets(TwitterCollection twitterCollection) {
        if (twitterCollection == null || twitterCollection.contents == null || twitterCollection.contents.tweetMap == null || twitterCollection.contents.userMap == null || twitterCollection.contents.tweetMap.isEmpty() || twitterCollection.contents.userMap.isEmpty() || twitterCollection.metadata == null || twitterCollection.metadata.timelineItems == null || twitterCollection.metadata.position == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (TimelineItem timelineItem : twitterCollection.metadata.timelineItems) {
            arrayList.add(mapTweetToUsers((Tweet) twitterCollection.contents.tweetMap.get(timelineItem.tweetItem.id), twitterCollection.contents.userMap));
        }
        return arrayList;
    }

    static Tweet mapTweetToUsers(Tweet tweet, Map<Long, User> map) {
        TweetBuilder user = new TweetBuilder().copy(tweet).setUser((User) map.get(Long.valueOf(tweet.user.id)));
        if (tweet.quotedStatus != null) {
            user.setQuotedStatus(mapTweetToUsers(tweet.quotedStatus, map));
        }
        return user.build();
    }

    static TimelineCursor getTimelineCursor(TwitterCollection twitterCollection) {
        return (twitterCollection == null || twitterCollection.metadata == null || twitterCollection.metadata.position == null) ? null : new TimelineCursor(twitterCollection.metadata.position.minPosition, twitterCollection.metadata.position.maxPosition);
    }
}
