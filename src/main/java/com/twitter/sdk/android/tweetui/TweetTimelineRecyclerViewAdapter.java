package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;
import com.facebook.internal.FacebookRequestErrorClassification;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.Identifiable;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import java.util.ArrayList;

public class TweetTimelineRecyclerViewAdapter extends Adapter<TweetViewHolder> {
    static final String DEFAULT_FILTERS_JSON_MSG = "{\"total_filters\":0}";
    static final String TOTAL_FILTERS_JSON_PROP = "total_filters";
    protected Callback<Tweet> actionCallback;
    protected final Context context;
    final Gson gson;
    private int previousCount;
    protected final int styleResId;
    protected final TimelineDelegate<Tweet> timelineDelegate;
    protected TweetUi tweetUi;

    public static class Builder {
        private Callback<Tweet> actionCallback;
        private Context context;
        private int styleResId = R.style.tw__TweetLightStyle;
        private Timeline<Tweet> timeline;
        private TimelineFilter timelineFilter;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTimeline(Timeline<Tweet> timeline) {
            this.timeline = timeline;
            return this;
        }

        public Builder setViewStyle(int i) {
            this.styleResId = i;
            return this;
        }

        public Builder setOnActionCallback(Callback<Tweet> callback) {
            this.actionCallback = callback;
            return this;
        }

        public Builder setTimelineFilter(TimelineFilter timelineFilter) {
            this.timelineFilter = timelineFilter;
            return this;
        }

        public TweetTimelineRecyclerViewAdapter build() {
            if (this.timelineFilter == null) {
                return new TweetTimelineRecyclerViewAdapter(this.context, this.timeline, this.styleResId, this.actionCallback);
            }
            return new TweetTimelineRecyclerViewAdapter(this.context, new FilterTimelineDelegate(this.timeline, this.timelineFilter), this.styleResId, this.actionCallback, TweetUi.getInstance());
        }
    }

    protected static final class TweetViewHolder extends ViewHolder {
        public TweetViewHolder(CompactTweetView compactTweetView) {
            super(compactTweetView);
        }
    }

    static class ReplaceTweetCallback extends Callback<Tweet> {
        Callback<Tweet> cb;
        TimelineDelegate<Tweet> delegate;

        ReplaceTweetCallback(TimelineDelegate<Tweet> timelineDelegate, Callback<Tweet> callback) {
            this.delegate = timelineDelegate;
            this.cb = callback;
        }

        public void success(Result<Tweet> result) {
            this.delegate.setItemById((Identifiable) result.data);
            if (this.cb != null) {
                this.cb.success(result);
            }
        }

        public void failure(TwitterException twitterException) {
            if (this.cb != null) {
                this.cb.failure(twitterException);
            }
        }
    }

    public TweetTimelineRecyclerViewAdapter(Context context, Timeline<Tweet> timeline) {
        this(context, timeline, R.style.tw__TweetLightStyle, null);
    }

    protected TweetTimelineRecyclerViewAdapter(Context context, Timeline<Tweet> timeline, int i, Callback<Tweet> callback) {
        this(context, new TimelineDelegate(timeline), i, callback, TweetUi.getInstance());
    }

    TweetTimelineRecyclerViewAdapter(Context context, TimelineDelegate<Tweet> timelineDelegate, int i, Callback<Tweet> callback, TweetUi tweetUi) {
        this(context, timelineDelegate, i);
        this.actionCallback = new ReplaceTweetCallback(timelineDelegate, callback);
        this.tweetUi = tweetUi;
        scribeTimelineImpression();
    }

    TweetTimelineRecyclerViewAdapter(Context context, TimelineDelegate<Tweet> timelineDelegate, int i) {
        this.gson = new Gson();
        if (context != null) {
            this.context = context;
            this.timelineDelegate = timelineDelegate;
            this.styleResId = i;
            this.timelineDelegate.refresh(new Callback<TimelineResult<Tweet>>() {
                public void failure(TwitterException twitterException) {
                }

                public void success(Result<TimelineResult<Tweet>> result) {
                    TweetTimelineRecyclerViewAdapter.this.notifyDataSetChanged();
                    TweetTimelineRecyclerViewAdapter.this.previousCount = TweetTimelineRecyclerViewAdapter.this.timelineDelegate.getCount();
                }
            });
            this.timelineDelegate.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    super.onChanged();
                    if (TweetTimelineRecyclerViewAdapter.this.previousCount == 0) {
                        TweetTimelineRecyclerViewAdapter.this.notifyDataSetChanged();
                    } else {
                        TweetTimelineRecyclerViewAdapter.this.notifyItemRangeInserted(TweetTimelineRecyclerViewAdapter.this.previousCount, TweetTimelineRecyclerViewAdapter.this.timelineDelegate.getCount() - TweetTimelineRecyclerViewAdapter.this.previousCount);
                    }
                    TweetTimelineRecyclerViewAdapter.this.previousCount = TweetTimelineRecyclerViewAdapter.this.timelineDelegate.getCount();
                }

                public void onInvalidated() {
                    TweetTimelineRecyclerViewAdapter.this.notifyDataSetChanged();
                    super.onInvalidated();
                }
            });
            return;
        }
        throw new IllegalArgumentException("Context must not be null");
    }

    public void refresh(Callback<TimelineResult<Tweet>> callback) {
        this.timelineDelegate.refresh(callback);
        this.previousCount = 0;
    }

    public TweetViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CompactTweetView compactTweetView = new CompactTweetView(this.context, new TweetBuilder().build(), this.styleResId);
        compactTweetView.setOnActionCallback(this.actionCallback);
        return new TweetViewHolder(compactTweetView);
    }

    public void onBindViewHolder(TweetViewHolder tweetViewHolder, int i) {
        ((CompactTweetView) tweetViewHolder.itemView).setTweet((Tweet) this.timelineDelegate.getItem(i));
    }

    public int getItemCount() {
        return this.timelineDelegate.getCount();
    }

    private void scribeTimelineImpression() {
        ScribeItem fromMessage = ScribeItem.fromMessage(this.timelineDelegate instanceof FilterTimelineDelegate ? getJsonMessage(((FilterTimelineDelegate) this.timelineDelegate).timelineFilter.totalFilters()) : DEFAULT_FILTERS_JSON_MSG);
        ArrayList arrayList = new ArrayList();
        arrayList.add(fromMessage);
        this.tweetUi.scribe(ScribeConstants.getSyndicatedSdkTimelineNamespace(getTimelineType(this.timelineDelegate.getTimeline())));
        this.tweetUi.scribe(ScribeConstants.getTfwClientTimelineNamespace(r0), arrayList);
    }

    private String getJsonMessage(int i) {
        JsonElement jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_FILTERS_JSON_PROP, Integer.valueOf(i));
        return this.gson.toJson(jsonObject);
    }

    static String getTimelineType(Timeline timeline) {
        return timeline instanceof BaseTimeline ? ((BaseTimeline) timeline).getTimelineType() : FacebookRequestErrorClassification.KEY_OTHER;
    }
}
