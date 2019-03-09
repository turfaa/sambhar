package io.sentry.connection;

import io.sentry.SentryClient;
import io.sentry.environment.SentryEnvironment;
import io.sentry.event.Event;
import io.sentry.util.Util;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AsyncConnection implements Connection {
    private static final Logger lockdownLogger;
    private static final Logger logger = LoggerFactory.getLogger(AsyncConnection.class);
    private final Connection actualConnection;
    private volatile boolean closed;
    private final ExecutorService executorService;
    private boolean gracefulShutdown;
    private final ShutDownHook shutDownHook = new ShutDownHook();
    private final long shutdownTimeout;

    private final class EventSubmitter implements Runnable {
        private final Event event;
        private Map<String, String> mdcContext;

        private EventSubmitter(Event event, Map<String, String> map) {
            this.event = event;
            this.mdcContext = map;
        }

        /* JADX WARNING: Missing block: B:6:0x001f, code skipped:
            if (r0 == null) goto L_0x0021;
     */
        /* JADX WARNING: Missing block: B:18:0x0055, code skipped:
            if (r0 != null) goto L_0x0025;
     */
        public void run() {
            /*
            r4 = this;
            io.sentry.environment.SentryEnvironment.startManagingThread();
            r0 = org.slf4j.MDC.getCopyOfContextMap();
            r1 = r4.mdcContext;
            if (r1 != 0) goto L_0x000f;
        L_0x000b:
            org.slf4j.MDC.clear();
            goto L_0x0014;
        L_0x000f:
            r1 = r4.mdcContext;
            org.slf4j.MDC.setContextMap(r1);
        L_0x0014:
            r1 = io.sentry.connection.AsyncConnection.this;	 Catch:{ LockedDownException | TooManyRequestsException -> 0x003b, LockedDownException | TooManyRequestsException -> 0x003b, Exception -> 0x002e }
            r1 = r1.actualConnection;	 Catch:{ LockedDownException | TooManyRequestsException -> 0x003b, LockedDownException | TooManyRequestsException -> 0x003b, Exception -> 0x002e }
            r2 = r4.event;	 Catch:{ LockedDownException | TooManyRequestsException -> 0x003b, LockedDownException | TooManyRequestsException -> 0x003b, Exception -> 0x002e }
            r1.send(r2);	 Catch:{ LockedDownException | TooManyRequestsException -> 0x003b, LockedDownException | TooManyRequestsException -> 0x003b, Exception -> 0x002e }
            if (r0 != 0) goto L_0x0025;
        L_0x0021:
            org.slf4j.MDC.clear();
            goto L_0x0028;
        L_0x0025:
            org.slf4j.MDC.setContextMap(r0);
        L_0x0028:
            io.sentry.environment.SentryEnvironment.stopManagingThread();
            goto L_0x0058;
        L_0x002c:
            r1 = move-exception;
            goto L_0x0059;
        L_0x002e:
            r1 = move-exception;
            r2 = io.sentry.connection.AsyncConnection.logger;	 Catch:{ all -> 0x002c }
            r3 = "An exception occurred while sending the event to Sentry.";
            r2.error(r3, r1);	 Catch:{ all -> 0x002c }
            if (r0 != 0) goto L_0x0025;
        L_0x003a:
            goto L_0x0021;
        L_0x003b:
            r1 = io.sentry.connection.AsyncConnection.logger;	 Catch:{ all -> 0x002c }
            r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x002c }
            r2.<init>();	 Catch:{ all -> 0x002c }
            r3 = "Dropping an Event due to lockdown: ";
            r2.append(r3);	 Catch:{ all -> 0x002c }
            r3 = r4.event;	 Catch:{ all -> 0x002c }
            r2.append(r3);	 Catch:{ all -> 0x002c }
            r2 = r2.toString();	 Catch:{ all -> 0x002c }
            r1.debug(r2);	 Catch:{ all -> 0x002c }
            if (r0 != 0) goto L_0x0025;
        L_0x0057:
            goto L_0x0021;
        L_0x0058:
            return;
        L_0x0059:
            if (r0 != 0) goto L_0x005f;
        L_0x005b:
            org.slf4j.MDC.clear();
            goto L_0x0062;
        L_0x005f:
            org.slf4j.MDC.setContextMap(r0);
        L_0x0062:
            io.sentry.environment.SentryEnvironment.stopManagingThread();
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: io.sentry.connection.AsyncConnection$EventSubmitter.run():void");
        }
    }

    private final class ShutDownHook extends Thread {
        private volatile boolean enabled;

        private ShutDownHook() {
            this.enabled = true;
        }

        public void run() {
            if (this.enabled) {
                SentryEnvironment.startManagingThread();
                try {
                    AsyncConnection.this.doClose();
                } catch (Exception e) {
                    AsyncConnection.logger.error("An exception occurred while closing the connection.", e);
                } catch (Throwable th) {
                    SentryEnvironment.stopManagingThread();
                }
                SentryEnvironment.stopManagingThread();
            }
        }
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SentryClient.class.getName());
        stringBuilder.append(".lockdown");
        lockdownLogger = LoggerFactory.getLogger(stringBuilder.toString());
    }

    public AsyncConnection(Connection connection, ExecutorService executorService, boolean z, long j) {
        this.actualConnection = connection;
        if (executorService == null) {
            this.executorService = Executors.newSingleThreadExecutor();
        } else {
            this.executorService = executorService;
        }
        if (z) {
            this.gracefulShutdown = z;
            addShutdownHook();
        }
        this.shutdownTimeout = j;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(this.shutDownHook);
    }

    public void send(Event event) {
        if (!this.closed) {
            this.executorService.execute(new EventSubmitter(event, MDC.getCopyOfContextMap()));
        }
    }

    public void addEventSendCallback(EventSendCallback eventSendCallback) {
        this.actualConnection.addEventSendCallback(eventSendCallback);
    }

    public void close() throws IOException {
        if (this.gracefulShutdown) {
            Util.safelyRemoveShutdownHook(this.shutDownHook);
            this.shutDownHook.enabled = false;
        }
        doClose();
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0063 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(2:15|16) */
    /* JADX WARNING: Missing block: B:16:?, code skipped:
            java.lang.Thread.currentThread().interrupt();
            logger.warn("Graceful shutdown interrupted, forcing the shutdown.");
            logger.warn("{} tasks failed to execute before shutdown.", java.lang.Integer.valueOf(r5.executorService.shutdownNow().size()));
     */
    /* JADX WARNING: Missing block: B:18:0x0088, code skipped:
            r5.actualConnection.close();
     */
    private void doClose() throws java.io.IOException {
        /*
        r5 = this;
        r0 = logger;
        r1 = "Gracefully shutting down Sentry async threads.";
        r0.debug(r1);
        r0 = 1;
        r5.closed = r0;
        r0 = r5.executorService;
        r0.shutdown();
        r0 = r5.shutdownTimeout;	 Catch:{ InterruptedException -> 0x0063 }
        r2 = -1;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 != 0) goto L_0x002c;
    L_0x0017:
        r0 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
    L_0x0019:
        r2 = r5.executorService;	 Catch:{ InterruptedException -> 0x0063 }
        r3 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ InterruptedException -> 0x0063 }
        r2 = r2.awaitTermination(r0, r3);	 Catch:{ InterruptedException -> 0x0063 }
        if (r2 == 0) goto L_0x0024;
    L_0x0023:
        goto L_0x0054;
    L_0x0024:
        r2 = logger;	 Catch:{ InterruptedException -> 0x0063 }
        r3 = "Still waiting on async executor to terminate.";
        r2.debug(r3);	 Catch:{ InterruptedException -> 0x0063 }
        goto L_0x0019;
    L_0x002c:
        r0 = r5.executorService;	 Catch:{ InterruptedException -> 0x0063 }
        r1 = r5.shutdownTimeout;	 Catch:{ InterruptedException -> 0x0063 }
        r3 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ InterruptedException -> 0x0063 }
        r0 = r0.awaitTermination(r1, r3);	 Catch:{ InterruptedException -> 0x0063 }
        if (r0 != 0) goto L_0x0054;
    L_0x0038:
        r0 = logger;	 Catch:{ InterruptedException -> 0x0063 }
        r1 = "Graceful shutdown took too much time, forcing the shutdown.";
        r0.warn(r1);	 Catch:{ InterruptedException -> 0x0063 }
        r0 = r5.executorService;	 Catch:{ InterruptedException -> 0x0063 }
        r0 = r0.shutdownNow();	 Catch:{ InterruptedException -> 0x0063 }
        r1 = logger;	 Catch:{ InterruptedException -> 0x0063 }
        r2 = "{} tasks failed to execute before shutdown.";
        r0 = r0.size();	 Catch:{ InterruptedException -> 0x0063 }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ InterruptedException -> 0x0063 }
        r1.warn(r2, r0);	 Catch:{ InterruptedException -> 0x0063 }
    L_0x0054:
        r0 = logger;	 Catch:{ InterruptedException -> 0x0063 }
        r1 = "Shutdown finished.";
        r0.debug(r1);	 Catch:{ InterruptedException -> 0x0063 }
    L_0x005b:
        r0 = r5.actualConnection;
        r0.close();
        goto L_0x0087;
    L_0x0061:
        r0 = move-exception;
        goto L_0x0088;
    L_0x0063:
        r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0061 }
        r0.interrupt();	 Catch:{ all -> 0x0061 }
        r0 = logger;	 Catch:{ all -> 0x0061 }
        r1 = "Graceful shutdown interrupted, forcing the shutdown.";
        r0.warn(r1);	 Catch:{ all -> 0x0061 }
        r0 = r5.executorService;	 Catch:{ all -> 0x0061 }
        r0 = r0.shutdownNow();	 Catch:{ all -> 0x0061 }
        r1 = logger;	 Catch:{ all -> 0x0061 }
        r2 = "{} tasks failed to execute before shutdown.";
        r0 = r0.size();	 Catch:{ all -> 0x0061 }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ all -> 0x0061 }
        r1.warn(r2, r0);	 Catch:{ all -> 0x0061 }
        goto L_0x005b;
    L_0x0087:
        return;
    L_0x0088:
        r1 = r5.actualConnection;
        r1.close();
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.connection.AsyncConnection.doClose():void");
    }
}
