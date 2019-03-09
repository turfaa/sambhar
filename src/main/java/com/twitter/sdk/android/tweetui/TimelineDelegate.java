package com.twitter.sdk.android.tweetui;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Identifiable;
import java.util.ArrayList;
import java.util.List;

class TimelineDelegate<T extends Identifiable> {
    static final long CAPACITY = 200;
    List<T> itemList;
    final DataSetObservable listAdapterObservable;
    final Timeline<T> timeline;
    final TimelineStateHolder timelineStateHolder;

    class DefaultCallback extends Callback<TimelineResult<T>> {
        final Callback<TimelineResult<T>> developerCallback;
        final TimelineStateHolder timelineStateHolder;

        DefaultCallback(Callback<TimelineResult<T>> callback, TimelineStateHolder timelineStateHolder) {
            this.developerCallback = callback;
            this.timelineStateHolder = timelineStateHolder;
        }

        public void success(Result<TimelineResult<T>> result) {
            this.timelineStateHolder.finishTimelineRequest();
            if (this.developerCallback != null) {
                this.developerCallback.success(result);
            }
        }

        public void failure(TwitterException twitterException) {
            this.timelineStateHolder.finishTimelineRequest();
            if (this.developerCallback != null) {
                this.developerCallback.failure(twitterException);
            }
        }
    }

    class NextCallback extends DefaultCallback {
        NextCallback(Callback<TimelineResult<T>> callback, TimelineStateHolder timelineStateHolder) {
            super(callback, timelineStateHolder);
        }

        public void success(Result<TimelineResult<T>> result) {
            if (((TimelineResult) result.data).items.size() > 0) {
                ArrayList arrayList = new ArrayList(((TimelineResult) result.data).items);
                arrayList.addAll(TimelineDelegate.this.itemList);
                TimelineDelegate.this.itemList = arrayList;
                TimelineDelegate.this.notifyDataSetChanged();
                this.timelineStateHolder.setNextCursor(((TimelineResult) result.data).timelineCursor);
            }
            super.success(result);
        }
    }

    class PreviousCallback extends DefaultCallback {
        PreviousCallback(TimelineStateHolder timelineStateHolder) {
            super(null, timelineStateHolder);
        }

        public void success(Result<TimelineResult<T>> result) {
            if (((TimelineResult) result.data).items.size() > 0) {
                TimelineDelegate.this.itemList.addAll(((TimelineResult) result.data).items);
                TimelineDelegate.this.notifyDataSetChanged();
                this.timelineStateHolder.setPreviousCursor(((TimelineResult) result.data).timelineCursor);
            }
            super.success(result);
        }
    }

    class RefreshCallback extends NextCallback {
        RefreshCallback(Callback<TimelineResult<T>> callback, TimelineStateHolder timelineStateHolder) {
            super(callback, timelineStateHolder);
        }

        public void success(Result<TimelineResult<T>> result) {
            if (((TimelineResult) result.data).items.size() > 0) {
                TimelineDelegate.this.itemList.clear();
            }
            super.success(result);
        }
    }

    public TimelineDelegate(Timeline<T> timeline) {
        this(timeline, null, null);
    }

    TimelineDelegate(Timeline<T> timeline, DataSetObservable dataSetObservable, List<T> list) {
        if (timeline != null) {
            this.timeline = timeline;
            this.timelineStateHolder = new TimelineStateHolder();
            if (dataSetObservable == null) {
                this.listAdapterObservable = new DataSetObservable();
            } else {
                this.listAdapterObservable = dataSetObservable;
            }
            if (list == null) {
                this.itemList = new ArrayList();
                return;
            } else {
                this.itemList = list;
                return;
            }
        }
        throw new IllegalArgumentException("Timeline must not be null");
    }

    public void refresh(Callback<TimelineResult<T>> callback) {
        this.timelineStateHolder.resetCursors();
        loadNext(this.timelineStateHolder.positionForNext(), new RefreshCallback(callback, this.timelineStateHolder));
    }

    public void next(Callback<TimelineResult<T>> callback) {
        loadNext(this.timelineStateHolder.positionForNext(), new NextCallback(callback, this.timelineStateHolder));
    }

    public void previous() {
        loadPrevious(this.timelineStateHolder.positionForPrevious(), new PreviousCallback(this.timelineStateHolder));
    }

    public int getCount() {
        return this.itemList.size();
    }

    public Timeline getTimeline() {
        return this.timeline;
    }

    public T getItem(int i) {
        if (isLastPosition(i)) {
            previous();
        }
        return (Identifiable) this.itemList.get(i);
    }

    public long getItemId(int i) {
        return ((Identifiable) this.itemList.get(i)).getId();
    }

    public void setItemById(T t) {
        for (int i = 0; i < this.itemList.size(); i++) {
            if (t.getId() == ((Identifiable) this.itemList.get(i)).getId()) {
                this.itemList.set(i, t);
            }
        }
        notifyDataSetChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean withinMaxCapacity() {
        return ((long) this.itemList.size()) < CAPACITY;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isLastPosition(int i) {
        return i == this.itemList.size() - 1;
    }

    /* Access modifiers changed, original: 0000 */
    public void loadNext(Long l, Callback<TimelineResult<T>> callback) {
        if (!withinMaxCapacity()) {
            callback.failure(new TwitterException("Max capacity reached"));
        } else if (this.timelineStateHolder.startTimelineRequest()) {
            this.timeline.next(l, callback);
        } else {
            callback.failure(new TwitterException("Request already in flight"));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void loadPrevious(Long l, Callback<TimelineResult<T>> callback) {
        if (!withinMaxCapacity()) {
            callback.failure(new TwitterException("Max capacity reached"));
        } else if (this.timelineStateHolder.startTimelineRequest()) {
            this.timeline.previous(l, callback);
        } else {
            callback.failure(new TwitterException("Request already in flight"));
        }
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.listAdapterObservable.registerObserver(dataSetObserver);
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.listAdapterObservable.unregisterObserver(dataSetObserver);
    }

    public void notifyDataSetChanged() {
        this.listAdapterObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        this.listAdapterObservable.notifyInvalidated();
    }
}
