package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.internal.CommonUtils;
import com.twitter.sdk.android.core.internal.IdManager;
import com.twitter.sdk.android.core.internal.SystemCurrentTimeProvider;
import com.twitter.sdk.android.core.internal.persistence.FileStoreImpl;
import com.twitter.sdk.android.core.internal.scribe.ScribeEvent.Transform;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class ScribeClient {
    private static final String STORAGE_DIR_BASE = "_se_to_send";
    private static final String WORKING_FILENAME_BASE = "_se.tap";
    private final TwitterAuthConfig authConfig;
    private final Context context;
    private final ScheduledExecutorService executor;
    private final GuestSessionProvider guestSessionProvider;
    private final IdManager idManager;
    private final ScribeConfig scribeConfig;
    final ConcurrentHashMap<Long, ScribeHandler> scribeHandlers = new ConcurrentHashMap(2);
    private final SessionManager<? extends Session<TwitterAuthToken>> sessionManager;
    private final Transform transform;

    public ScribeClient(Context context, ScheduledExecutorService scheduledExecutorService, ScribeConfig scribeConfig, Transform transform, TwitterAuthConfig twitterAuthConfig, SessionManager<? extends Session<TwitterAuthToken>> sessionManager, GuestSessionProvider guestSessionProvider, IdManager idManager) {
        this.context = context;
        this.executor = scheduledExecutorService;
        this.scribeConfig = scribeConfig;
        this.transform = transform;
        this.authConfig = twitterAuthConfig;
        this.sessionManager = sessionManager;
        this.guestSessionProvider = guestSessionProvider;
        this.idManager = idManager;
    }

    public boolean scribe(ScribeEvent scribeEvent, long j) {
        try {
            getScribeHandler(j).scribe(scribeEvent);
            return true;
        } catch (IOException e) {
            CommonUtils.logControlledError(this.context, "Failed to scribe event", e);
            return false;
        }
    }

    public boolean scribeAndFlush(ScribeEvent scribeEvent, long j) {
        try {
            getScribeHandler(j).scribeAndFlush(scribeEvent);
            return true;
        } catch (IOException e) {
            CommonUtils.logControlledError(this.context, "Failed to scribe event", e);
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ScribeHandler getScribeHandler(long j) throws IOException {
        if (!this.scribeHandlers.containsKey(Long.valueOf(j))) {
            this.scribeHandlers.putIfAbsent(Long.valueOf(j), newScribeHandler(j));
        }
        return (ScribeHandler) this.scribeHandlers.get(Long.valueOf(j));
    }

    private ScribeHandler newScribeHandler(long j) throws IOException {
        ScribeFilesManager scribeFilesManager = new ScribeFilesManager(this.context, this.transform, new SystemCurrentTimeProvider(), new QueueFileEventStorage(this.context, new FileStoreImpl(this.context).getFilesDir(), getWorkingFileNameForOwner(j), getStorageDirForOwner(j)), this.scribeConfig.maxFilesToKeep);
        return new ScribeHandler(this.context, getScribeStrategy(j, scribeFilesManager), scribeFilesManager, this.executor);
    }

    /* Access modifiers changed, original: 0000 */
    public EventsStrategy<ScribeEvent> getScribeStrategy(long j, ScribeFilesManager scribeFilesManager) {
        if (this.scribeConfig.isEnabled) {
            CommonUtils.logControlled(this.context, "Scribe enabled");
            return new EnabledScribeStrategy(this.context, this.executor, scribeFilesManager, this.scribeConfig, new ScribeFilesSender(this.context, this.scribeConfig, j, this.authConfig, this.sessionManager, this.guestSessionProvider, this.executor, this.idManager));
        }
        CommonUtils.logControlled(this.context, "Scribe disabled");
        return new DisabledEventsStrategy();
    }

    /* Access modifiers changed, original: 0000 */
    public String getWorkingFileNameForOwner(long j) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(j);
        stringBuilder.append(WORKING_FILENAME_BASE);
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public String getStorageDirForOwner(long j) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(j);
        stringBuilder.append(STORAGE_DIR_BASE);
        return stringBuilder.toString();
    }
}
