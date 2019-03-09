package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import com.twitter.sdk.android.core.internal.CommonUtils;
import java.util.concurrent.ScheduledExecutorService;

public abstract class EventsHandler<T> implements EventsStorageListener {
    protected final Context context;
    protected final ScheduledExecutorService executor;
    protected EventsStrategy<T> strategy;

    public abstract EventsStrategy<T> getDisabledEventsStrategy();

    public EventsHandler(Context context, EventsStrategy<T> eventsStrategy, EventsFilesManager eventsFilesManager, ScheduledExecutorService scheduledExecutorService) {
        this.context = context.getApplicationContext();
        this.executor = scheduledExecutorService;
        this.strategy = eventsStrategy;
        eventsFilesManager.registerRollOverListener(this);
    }

    public void recordEventAsync(final T t, final boolean z) {
        executeAsync(new Runnable() {
            public void run() {
                try {
                    EventsHandler.this.strategy.recordEvent(t);
                    if (z) {
                        EventsHandler.this.strategy.rollFileOver();
                    }
                } catch (Exception e) {
                    CommonUtils.logControlledError(EventsHandler.this.context, "Failed to record event.", e);
                }
            }
        });
    }

    public void recordEventSync(final T t) {
        executeSync(new Runnable() {
            public void run() {
                try {
                    EventsHandler.this.strategy.recordEvent(t);
                } catch (Exception e) {
                    CommonUtils.logControlledError(EventsHandler.this.context, "Failed to record event", e);
                }
            }
        });
    }

    public void onRollOver(String str) {
        executeAsync(new Runnable() {
            public void run() {
                try {
                    EventsHandler.this.strategy.sendEvents();
                } catch (Exception e) {
                    CommonUtils.logControlledError(EventsHandler.this.context, "Failed to send events files.", e);
                }
            }
        });
    }

    public void disable() {
        executeAsync(new Runnable() {
            public void run() {
                try {
                    EventsStrategy eventsStrategy = EventsHandler.this.strategy;
                    EventsHandler.this.strategy = EventsHandler.this.getDisabledEventsStrategy();
                    eventsStrategy.deleteAllEvents();
                } catch (Exception e) {
                    CommonUtils.logControlledError(EventsHandler.this.context, "Failed to disable events.", e);
                }
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void executeSync(Runnable runnable) {
        try {
            this.executor.submit(runnable).get();
        } catch (Exception e) {
            CommonUtils.logControlledError(this.context, "Failed to run events task", e);
        }
    }

    /* Access modifiers changed, original: protected */
    public void executeAsync(Runnable runnable) {
        try {
            this.executor.submit(runnable);
        } catch (Exception e) {
            CommonUtils.logControlledError(this.context, "Failed to submit events task", e);
        }
    }
}
