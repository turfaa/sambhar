package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
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
import java.util.ArrayList;

public class TweetTimelineListAdapter extends TimelineListAdapter<Tweet> {
    static final String DEFAULT_FILTERS_JSON_MSG = "{\"total_filters\":0}";
    static final String TOTAL_FILTERS_JSON_PROP = "total_filters";
    protected Callback<Tweet> actionCallback;
    final Gson gson;
    protected final int styleResId;
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

        public TweetTimelineListAdapter build() {
            if (this.timelineFilter == null) {
                return new TweetTimelineListAdapter(this.context, this.timeline, this.styleResId, this.actionCallback);
            }
            return new TweetTimelineListAdapter(this.context, new FilterTimelineDelegate(this.timeline, this.timelineFilter), this.styleResId, this.actionCallback, TweetUi.getInstance());
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

    public /* bridge */ /* synthetic */ int getCount() {
        return super.getCount();
    }

    public /* bridge */ /* synthetic */ long getItemId(int i) {
        return super.getItemId(i);
    }

    public /* bridge */ /* synthetic */ void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public /* bridge */ /* synthetic */ void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    public /* bridge */ /* synthetic */ void refresh(Callback callback) {
        super.refresh(callback);
    }

    public /* bridge */ /* synthetic */ void registerDataSetObserver(DataSetObserver dataSetObserver) {
        super.registerDataSetObserver(dataSetObserver);
    }

    public /* bridge */ /* synthetic */ void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        super.unregisterDataSetObserver(dataSetObserver);
    }

    public TweetTimelineListAdapter(Context context, Timeline<Tweet> timeline) {
        this(context, timeline, R.style.tw__TweetLightStyle, null);
    }

    TweetTimelineListAdapter(Context context, Timeline<Tweet> timeline, int i, Callback<Tweet> callback) {
        this(context, new TimelineDelegate(timeline), i, callback, TweetUi.getInstance());
    }

    TweetTimelineListAdapter(Context context, TimelineDelegate<Tweet> timelineDelegate, int i, Callback<Tweet> callback, TweetUi tweetUi) {
        super(context, (TimelineDelegate) timelineDelegate);
        this.gson = new Gson();
        this.styleResId = i;
        this.actionCallback = new ReplaceTweetCallback(timelineDelegate, callback);
        this.tweetUi = tweetUi;
        scribeTimelineImpression();
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        Tweet tweet = (Tweet) getItem(i);
        if (view == null) {
            view = new CompactTweetView(this.context, tweet, this.styleResId);
            view.setOnActionCallback(this.actionCallback);
            return view;
        }
        ((BaseTweetView) view).setTweet(tweet);
        return view;
    }

    private void scribeTimelineImpression() {
        ScribeItem fromMessage = ScribeItem.fromMessage(this.delegate instanceof FilterTimelineDelegate ? getJsonMessage(((FilterTimelineDelegate) this.delegate).timelineFilter.totalFilters()) : DEFAULT_FILTERS_JSON_MSG);
        ArrayList arrayList = new ArrayList();
        arrayList.add(fromMessage);
        this.tweetUi.scribe(ScribeConstants.getSyndicatedSdkTimelineNamespace(getTimelineType(this.delegate.getTimeline())));
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
