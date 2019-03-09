package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import com.twitter.sdk.android.core.internal.CommonUtils;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EnabledEventsStrategy<T> implements EventsStrategy<T> {
    static final int UNDEFINED_ROLLOVER_INTERVAL_SECONDS = -1;
    protected final Context context;
    final ScheduledExecutorService executorService;
    protected final EventsFilesManager<T> filesManager;
    volatile int rolloverIntervalSeconds = -1;
    final AtomicReference<ScheduledFuture<?>> scheduledRolloverFutureRef;

    public EnabledEventsStrategy(Context context, ScheduledExecutorService scheduledExecutorService, EventsFilesManager<T> eventsFilesManager) {
        this.context = context;
        this.executorService = scheduledExecutorService;
        this.filesManager = eventsFilesManager;
        this.scheduledRolloverFutureRef = new AtomicReference();
    }

    public void scheduleTimeBasedRollOverIfNeeded() {
        if ((this.rolloverIntervalSeconds != -1 ? 1 : null) != null) {
            scheduleTimeBasedFileRollOver((long) this.rolloverIntervalSeconds, (long) this.rolloverIntervalSeconds);
        }
    }

    public void sendEvents() {
        sendAndCleanUpIfSuccess();
    }

    public void cancelTimeBasedFileRollOver() {
        if (this.scheduledRolloverFutureRef.get() != null) {
            CommonUtils.logControlled(this.context, "Cancelling time-based rollover because no events are currently being generated.");
            ((ScheduledFuture) this.scheduledRolloverFutureRef.get()).cancel(false);
            this.scheduledRolloverFutureRef.set(null);
        }
    }

    public void deleteAllEvents() {
        this.filesManager.deleteAllEventsFiles();
    }

    public void recordEvent(T t) {
        CommonUtils.logControlled(this.context, t.toString());
        try {
            this.filesManager.writeEvent(t);
        } catch (IOException e) {
            CommonUtils.logControlledError(this.context, "Failed to write event.", e);
        }
        scheduleTimeBasedRollOverIfNeeded();
    }

    public boolean rollFileOver() {
        try {
            return this.filesManager.rollFileOver();
        } catch (IOException e) {
            CommonUtils.logControlledError(this.context, "Failed to roll file over.", e);
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public void configureRollover(int i) {
        this.rolloverIntervalSeconds = i;
        scheduleTimeBasedFileRollOver(0, (long) this.rolloverIntervalSeconds);
    }

    public int getRollover() {
        return this.rolloverIntervalSeconds;
    }

    /* Access modifiers changed, original: 0000 */
    public void scheduleTimeBasedFileRollOver(long j, long j2) {
        if ((this.scheduledRolloverFutureRef.get() == null ? 1 : null) != null) {
            TimeBasedFileRollOverRunnable timeBasedFileRollOverRunnable = new TimeBasedFileRollOverRunnable(this.context, this);
            Context context = this.context;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Scheduling time based file roll over every ");
            stringBuilder.append(j2);
            stringBuilder.append(" seconds");
            CommonUtils.logControlled(context, stringBuilder.toString());
            try {
                this.scheduledRolloverFutureRef.set(this.executorService.scheduleAtFixedRate(timeBasedFileRollOverRunnable, j, j2, TimeUnit.SECONDS));
            } catch (RejectedExecutionException e) {
                CommonUtils.logControlledError(this.context, "Failed to schedule time based file roll over", e);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void sendAndCleanUpIfSuccess() {
        FilesSender filesSender = getFilesSender();
        if (filesSender == null) {
            CommonUtils.logControlled(this.context, "skipping files send because we don't yet know the target endpoint");
            return;
        }
        CommonUtils.logControlled(this.context, "Sending all files");
        List batchOfFilesToSend = this.filesManager.getBatchOfFilesToSend();
        int i = 0;
        while (batchOfFilesToSend.size() > 0) {
            try {
                CommonUtils.logControlled(this.context, String.format(Locale.US, "attempt to send batch of %d files", new Object[]{Integer.valueOf(batchOfFilesToSend.size())}));
                boolean send = filesSender.send(batchOfFilesToSend);
                if (send) {
                    i += batchOfFilesToSend.size();
                    this.filesManager.deleteSentFiles(batchOfFilesToSend);
                }
                if (!send) {
                    break;
                }
                batchOfFilesToSend = this.filesManager.getBatchOfFilesToSend();
            } catch (Exception e) {
                Context context = this.context;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to send batch of analytics files to server: ");
                stringBuilder.append(e.getMessage());
                CommonUtils.logControlledError(context, stringBuilder.toString(), e);
            }
        }
        if (i == 0) {
            this.filesManager.deleteOldestInRollOverIfOverMax();
        }
    }
}
