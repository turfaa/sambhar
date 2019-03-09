package io.sentry.connection;

import io.sentry.buffer.Buffer;
import io.sentry.environment.SentryEnvironment;
import io.sentry.event.Event;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(BufferedConnection.class);
    private Connection actualConnection;
    private Buffer buffer;
    private volatile boolean closed = false;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    });
    private boolean gracefulShutdown;
    private final ShutDownHook shutDownHook = new ShutDownHook(this, null);
    private long shutdownTimeout;

    private class Flusher implements Runnable {
        private long minAgeMillis;

        Flusher(long j) {
            this.minAgeMillis = j;
        }

        public void run() {
            BufferedConnection.logger.trace("Running Flusher");
            SentryEnvironment.startManagingThread();
            try {
                Iterator events = BufferedConnection.this.buffer.getEvents();
                while (events.hasNext() && !BufferedConnection.this.closed) {
                    Event event = (Event) events.next();
                    long currentTimeMillis = System.currentTimeMillis() - event.getTimestamp().getTime();
                    if (currentTimeMillis < this.minAgeMillis) {
                        Logger access$300 = BufferedConnection.logger;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Ignoring buffered event because it only ");
                        stringBuilder.append(currentTimeMillis);
                        stringBuilder.append("ms old.");
                        access$300.trace(stringBuilder.toString());
                        SentryEnvironment.stopManagingThread();
                        return;
                    }
                    Logger access$3002;
                    StringBuilder stringBuilder2;
                    try {
                        access$3002 = BufferedConnection.logger;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Flusher attempting to send Event: ");
                        stringBuilder2.append(event.getId());
                        access$3002.trace(stringBuilder2.toString());
                        BufferedConnection.this.send(event);
                        access$3002 = BufferedConnection.logger;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Flusher successfully sent Event: ");
                        stringBuilder2.append(event.getId());
                        access$3002.trace(stringBuilder2.toString());
                    } catch (Exception e) {
                        access$3002 = BufferedConnection.logger;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Flusher failed to send Event: ");
                        stringBuilder2.append(event.getId());
                        access$3002.debug(stringBuilder2.toString(), e);
                        BufferedConnection.logger.trace("Flusher run exiting early.");
                        SentryEnvironment.stopManagingThread();
                        return;
                    }
                }
                BufferedConnection.logger.trace("Flusher run exiting, no more events to send.");
            } catch (Exception e2) {
                BufferedConnection.logger.error("Error running Flusher: ", e2);
            } catch (Throwable th) {
                SentryEnvironment.stopManagingThread();
            }
            SentryEnvironment.stopManagingThread();
        }
    }

    private final class ShutDownHook extends Thread {
        private volatile boolean enabled;

        private ShutDownHook() {
            this.enabled = true;
        }

        /* synthetic */ ShutDownHook(BufferedConnection bufferedConnection, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void run() {
            if (this.enabled) {
                SentryEnvironment.startManagingThread();
                try {
                    BufferedConnection.this.close();
                } catch (Exception e) {
                    BufferedConnection.logger.error("An exception occurred while closing the connection.", e);
                } catch (Throwable th) {
                    SentryEnvironment.stopManagingThread();
                }
                SentryEnvironment.stopManagingThread();
            }
        }
    }

    public BufferedConnection(Connection connection, Buffer buffer, long j, boolean z, long j2) {
        this.actualConnection = connection;
        this.buffer = buffer;
        this.gracefulShutdown = z;
        this.shutdownTimeout = j2;
        if (z) {
            Runtime.getRuntime().addShutdownHook(this.shutDownHook);
        }
        this.executorService.scheduleWithFixedDelay(new Flusher(j), j, j, TimeUnit.MILLISECONDS);
    }

    public void send(Event event) {
        try {
            this.actualConnection.send(event);
            this.buffer.discard(event);
        } catch (ConnectionException e) {
            boolean z = e.getCause() instanceof NotSerializableException;
            Integer responseCode = e.getResponseCode();
            if (z || responseCode != null) {
                this.buffer.discard(event);
            }
            throw e;
        }
    }

    public void addEventSendCallback(EventSendCallback eventSendCallback) {
        this.actualConnection.addEventSendCallback(eventSendCallback);
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:18:0x0072 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(2:18|19) */
    /* JADX WARNING: Missing block: B:19:?, code skipped:
            java.lang.Thread.currentThread().interrupt();
            logger.warn("Graceful shutdown interrupted, forcing the shutdown.");
            logger.warn("{} tasks failed to execute before the shutdown.", java.lang.Integer.valueOf(r5.executorService.shutdownNow().size()));
     */
    /* JADX WARNING: Missing block: B:21:0x0097, code skipped:
            r5.actualConnection.close();
     */
    public void close() throws java.io.IOException {
        /*
        r5 = this;
        r0 = r5.gracefulShutdown;
        if (r0 == 0) goto L_0x000f;
    L_0x0004:
        r0 = r5.shutDownHook;
        io.sentry.util.Util.safelyRemoveShutdownHook(r0);
        r0 = r5.shutDownHook;
        r1 = 0;
        r0.enabled = r1;
    L_0x000f:
        r0 = logger;
        r1 = "Gracefully shutting down Sentry buffer threads.";
        r0.debug(r1);
        r0 = 1;
        r5.closed = r0;
        r0 = r5.executorService;
        r0.shutdown();
        r0 = r5.shutdownTimeout;	 Catch:{ InterruptedException -> 0x0072 }
        r2 = -1;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 != 0) goto L_0x003b;
    L_0x0026:
        r0 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
    L_0x0028:
        r2 = r5.executorService;	 Catch:{ InterruptedException -> 0x0072 }
        r3 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ InterruptedException -> 0x0072 }
        r2 = r2.awaitTermination(r0, r3);	 Catch:{ InterruptedException -> 0x0072 }
        if (r2 == 0) goto L_0x0033;
    L_0x0032:
        goto L_0x0063;
    L_0x0033:
        r2 = logger;	 Catch:{ InterruptedException -> 0x0072 }
        r3 = "Still waiting on buffer flusher executor to terminate.";
        r2.debug(r3);	 Catch:{ InterruptedException -> 0x0072 }
        goto L_0x0028;
    L_0x003b:
        r0 = r5.executorService;	 Catch:{ InterruptedException -> 0x0072 }
        r1 = r5.shutdownTimeout;	 Catch:{ InterruptedException -> 0x0072 }
        r3 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ InterruptedException -> 0x0072 }
        r0 = r0.awaitTermination(r1, r3);	 Catch:{ InterruptedException -> 0x0072 }
        if (r0 != 0) goto L_0x0063;
    L_0x0047:
        r0 = logger;	 Catch:{ InterruptedException -> 0x0072 }
        r1 = "Graceful shutdown took too much time, forcing the shutdown.";
        r0.warn(r1);	 Catch:{ InterruptedException -> 0x0072 }
        r0 = r5.executorService;	 Catch:{ InterruptedException -> 0x0072 }
        r0 = r0.shutdownNow();	 Catch:{ InterruptedException -> 0x0072 }
        r1 = logger;	 Catch:{ InterruptedException -> 0x0072 }
        r2 = "{} tasks failed to execute before the shutdown.";
        r0 = r0.size();	 Catch:{ InterruptedException -> 0x0072 }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ InterruptedException -> 0x0072 }
        r1.warn(r2, r0);	 Catch:{ InterruptedException -> 0x0072 }
    L_0x0063:
        r0 = logger;	 Catch:{ InterruptedException -> 0x0072 }
        r1 = "Shutdown finished.";
        r0.debug(r1);	 Catch:{ InterruptedException -> 0x0072 }
    L_0x006a:
        r0 = r5.actualConnection;
        r0.close();
        goto L_0x0096;
    L_0x0070:
        r0 = move-exception;
        goto L_0x0097;
    L_0x0072:
        r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0070 }
        r0.interrupt();	 Catch:{ all -> 0x0070 }
        r0 = logger;	 Catch:{ all -> 0x0070 }
        r1 = "Graceful shutdown interrupted, forcing the shutdown.";
        r0.warn(r1);	 Catch:{ all -> 0x0070 }
        r0 = r5.executorService;	 Catch:{ all -> 0x0070 }
        r0 = r0.shutdownNow();	 Catch:{ all -> 0x0070 }
        r1 = logger;	 Catch:{ all -> 0x0070 }
        r2 = "{} tasks failed to execute before the shutdown.";
        r0 = r0.size();	 Catch:{ all -> 0x0070 }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ all -> 0x0070 }
        r1.warn(r2, r0);	 Catch:{ all -> 0x0070 }
        goto L_0x006a;
    L_0x0096:
        return;
    L_0x0097:
        r1 = r5.actualConnection;
        r1.close();
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.connection.BufferedConnection.close():void");
    }

    public Connection wrapConnectionWithBufferWriter(final Connection connection) {
        return new Connection() {
            final Connection wrappedConnection = connection;

            public void send(Event event) throws ConnectionException {
                try {
                    BufferedConnection.this.buffer.add(event);
                } catch (Exception e) {
                    BufferedConnection.logger.error("Exception occurred while attempting to add Event to buffer: ", e);
                }
                this.wrappedConnection.send(event);
            }

            public void addEventSendCallback(EventSendCallback eventSendCallback) {
                this.wrappedConnection.addEventSendCallback(eventSendCallback);
            }

            public void close() throws IOException {
                this.wrappedConnection.close();
            }
        };
    }
}
