package bolts;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CancellationTokenSource implements Closeable {
    private boolean cancellationRequested;
    private boolean closed;
    private final ScheduledExecutorService executor = BoltsExecutors.scheduled();
    private final Object lock = new Object();
    private final List<CancellationTokenRegistration> registrations = new ArrayList();
    private ScheduledFuture<?> scheduledCancellation;

    public boolean isCancellationRequested() {
        boolean z;
        synchronized (this.lock) {
            throwIfClosed();
            z = this.cancellationRequested;
        }
        return z;
    }

    public CancellationToken getToken() {
        CancellationToken cancellationToken;
        synchronized (this.lock) {
            throwIfClosed();
            cancellationToken = new CancellationToken(this);
        }
        return cancellationToken;
    }

    public void cancel() {
        synchronized (this.lock) {
            throwIfClosed();
            if (this.cancellationRequested) {
                return;
            }
            cancelScheduledCancellation();
            this.cancellationRequested = true;
            ArrayList arrayList = new ArrayList(this.registrations);
            notifyListeners(arrayList);
        }
    }

    public void cancelAfter(long j) {
        cancelAfter(j, TimeUnit.MILLISECONDS);
    }

    /* JADX WARNING: Missing block: B:17:0x002e, code skipped:
            return;
     */
    private void cancelAfter(long r6, java.util.concurrent.TimeUnit r8) {
        /*
        r5 = this;
        r0 = -1;
        r2 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r2 < 0) goto L_0x0032;
    L_0x0006:
        r2 = 0;
        r4 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1));
        if (r4 != 0) goto L_0x0010;
    L_0x000c:
        r5.cancel();
        return;
    L_0x0010:
        r2 = r5.lock;
        monitor-enter(r2);
        r3 = r5.cancellationRequested;	 Catch:{ all -> 0x002f }
        if (r3 == 0) goto L_0x0019;
    L_0x0017:
        monitor-exit(r2);	 Catch:{ all -> 0x002f }
        return;
    L_0x0019:
        r5.cancelScheduledCancellation();	 Catch:{ all -> 0x002f }
        r3 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r3 == 0) goto L_0x002d;
    L_0x0020:
        r0 = r5.executor;	 Catch:{ all -> 0x002f }
        r1 = new bolts.CancellationTokenSource$1;	 Catch:{ all -> 0x002f }
        r1.<init>();	 Catch:{ all -> 0x002f }
        r6 = r0.schedule(r1, r6, r8);	 Catch:{ all -> 0x002f }
        r5.scheduledCancellation = r6;	 Catch:{ all -> 0x002f }
    L_0x002d:
        monitor-exit(r2);	 Catch:{ all -> 0x002f }
        return;
    L_0x002f:
        r6 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x002f }
        throw r6;
    L_0x0032:
        r6 = new java.lang.IllegalArgumentException;
        r7 = "Delay must be >= -1";
        r6.<init>(r7);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: bolts.CancellationTokenSource.cancelAfter(long, java.util.concurrent.TimeUnit):void");
    }

    public void close() {
        synchronized (this.lock) {
            if (this.closed) {
                return;
            }
            cancelScheduledCancellation();
            for (CancellationTokenRegistration close : this.registrations) {
                close.close();
            }
            this.registrations.clear();
            this.closed = true;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public CancellationTokenRegistration register(Runnable runnable) {
        CancellationTokenRegistration cancellationTokenRegistration;
        synchronized (this.lock) {
            throwIfClosed();
            cancellationTokenRegistration = new CancellationTokenRegistration(this, runnable);
            if (this.cancellationRequested) {
                cancellationTokenRegistration.runAction();
            } else {
                this.registrations.add(cancellationTokenRegistration);
            }
        }
        return cancellationTokenRegistration;
    }

    /* Access modifiers changed, original: 0000 */
    public void throwIfCancellationRequested() throws CancellationException {
        synchronized (this.lock) {
            throwIfClosed();
            if (this.cancellationRequested) {
                throw new CancellationException();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void unregister(CancellationTokenRegistration cancellationTokenRegistration) {
        synchronized (this.lock) {
            throwIfClosed();
            this.registrations.remove(cancellationTokenRegistration);
        }
    }

    private void notifyListeners(List<CancellationTokenRegistration> list) {
        for (CancellationTokenRegistration runAction : list) {
            runAction.runAction();
        }
    }

    public String toString() {
        return String.format(Locale.US, "%s@%s[cancellationRequested=%s]", new Object[]{getClass().getName(), Integer.toHexString(hashCode()), Boolean.toString(isCancellationRequested())});
    }

    private void throwIfClosed() {
        if (this.closed) {
            throw new IllegalStateException("Object already closed");
        }
    }

    private void cancelScheduledCancellation() {
        if (this.scheduledCancellation != null) {
            this.scheduledCancellation.cancel(true);
            this.scheduledCancellation = null;
        }
    }
}
