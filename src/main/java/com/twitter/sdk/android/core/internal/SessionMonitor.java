package com.twitter.sdk.android.core.internal;

import android.app.Activity;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.internal.ActivityLifecycleManager.Callbacks;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

public class SessionMonitor<T extends Session> {
    private final ExecutorService executorService;
    protected final MonitorState monitorState;
    private final SessionManager<T> sessionManager;
    private final SessionVerifier sessionVerifier;
    private final SystemCurrentTimeProvider time;

    protected static class MonitorState {
        private static final long TIME_THRESHOLD_IN_MILLIS = 21600000;
        public long lastVerification;
        private final Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        public boolean verifying;

        /* JADX WARNING: Missing block: B:14:0x0026, code skipped:
            return false;
     */
        public synchronized boolean beginVerification(long r8) {
            /*
            r7 = this;
            monitor-enter(r7);
            r0 = r7.lastVerification;	 Catch:{ all -> 0x0027 }
            r2 = 0;
            r0 = r8 - r0;
            r2 = 21600000; // 0x1499700 float:3.7026207E-38 double:1.0671818E-316;
            r4 = 0;
            r5 = 1;
            r6 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
            if (r6 <= 0) goto L_0x0011;
        L_0x000f:
            r0 = 1;
            goto L_0x0012;
        L_0x0011:
            r0 = 0;
        L_0x0012:
            r1 = r7.lastVerification;	 Catch:{ all -> 0x0027 }
            r8 = r7.isOnSameDate(r8, r1);	 Catch:{ all -> 0x0027 }
            r8 = r8 ^ r5;
            r9 = r7.verifying;	 Catch:{ all -> 0x0027 }
            if (r9 != 0) goto L_0x0025;
        L_0x001d:
            if (r0 != 0) goto L_0x0021;
        L_0x001f:
            if (r8 == 0) goto L_0x0025;
        L_0x0021:
            r7.verifying = r5;	 Catch:{ all -> 0x0027 }
            monitor-exit(r7);
            return r5;
        L_0x0025:
            monitor-exit(r7);
            return r4;
        L_0x0027:
            r8 = move-exception;
            monitor-exit(r7);
            throw r8;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.internal.SessionMonitor$MonitorState.beginVerification(long):boolean");
        }

        public synchronized void endVerification(long j) {
            this.verifying = false;
            this.lastVerification = j;
        }

        private boolean isOnSameDate(long j, long j2) {
            this.utcCalendar.setTimeInMillis(j);
            int i = this.utcCalendar.get(6);
            int i2 = this.utcCalendar.get(1);
            this.utcCalendar.setTimeInMillis(j2);
            int i3 = this.utcCalendar.get(6);
            int i4 = this.utcCalendar.get(1);
            if (i == i3 && i2 == i4) {
                return true;
            }
            return false;
        }
    }

    public SessionMonitor(SessionManager<T> sessionManager, ExecutorService executorService, SessionVerifier<T> sessionVerifier) {
        this(sessionManager, new SystemCurrentTimeProvider(), executorService, new MonitorState(), sessionVerifier);
    }

    SessionMonitor(SessionManager<T> sessionManager, SystemCurrentTimeProvider systemCurrentTimeProvider, ExecutorService executorService, MonitorState monitorState, SessionVerifier sessionVerifier) {
        this.time = systemCurrentTimeProvider;
        this.sessionManager = sessionManager;
        this.executorService = executorService;
        this.monitorState = monitorState;
        this.sessionVerifier = sessionVerifier;
    }

    public void monitorActivityLifecycle(ActivityLifecycleManager activityLifecycleManager) {
        activityLifecycleManager.registerCallbacks(new Callbacks() {
            public void onActivityStarted(Activity activity) {
                SessionMonitor.this.triggerVerificationIfNecessary();
            }
        });
    }

    public void triggerVerificationIfNecessary() {
        Object obj = (this.sessionManager.getActiveSession() == null || !this.monitorState.beginVerification(this.time.getCurrentTimeMillis())) ? null : 1;
        if (obj != null) {
            this.executorService.submit(new Runnable() {
                public void run() {
                    SessionMonitor.this.verifyAll();
                }
            });
        }
    }

    /* Access modifiers changed, original: protected */
    public void verifyAll() {
        for (Session verifySession : this.sessionManager.getSessionMap().values()) {
            this.sessionVerifier.verifySession(verifySession);
        }
        this.monitorState.endVerification(this.time.getCurrentTimeMillis());
    }
}
