package okhttp3.internal.connection;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.List;
import okhttp3.Address;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.ConnectionPool;
import okhttp3.EventListener;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RouteSelector.Selection;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.StreamResetException;

public final class StreamAllocation {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public final Address address;
    public final Call call;
    private final Object callStackTrace;
    private boolean canceled;
    private HttpCodec codec;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    public final EventListener eventListener;
    private int refusedStreamCount;
    private boolean released;
    private boolean reportedAcquired;
    private Route route;
    private Selection routeSelection;
    private final RouteSelector routeSelector;

    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation streamAllocation, Object obj) {
            super(streamAllocation);
            this.callStackTrace = obj;
        }
    }

    public StreamAllocation(ConnectionPool connectionPool, Address address, Call call, EventListener eventListener, Object obj) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(address, routeDatabase(), call, eventListener);
        this.callStackTrace = obj;
    }

    public HttpCodec newStream(OkHttpClient okHttpClient, Chain chain, boolean z) {
        try {
            HttpCodec newCodec = findHealthyConnection(chain.connectTimeoutMillis(), chain.readTimeoutMillis(), chain.writeTimeoutMillis(), okHttpClient.pingIntervalMillis(), okHttpClient.retryOnConnectionFailure(), z).newCodec(okHttpClient, chain, this);
            synchronized (this.connectionPool) {
                this.codec = newCodec;
            }
            return newCodec;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code skipped:
            if (r0.isHealthy(r9) != false) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:11:0x0018, code skipped:
            return r0;
     */
    private okhttp3.internal.connection.RealConnection findHealthyConnection(int r4, int r5, int r6, int r7, boolean r8, boolean r9) throws java.io.IOException {
        /*
        r3 = this;
    L_0x0000:
        r0 = r3.findConnection(r4, r5, r6, r7, r8);
        r1 = r3.connectionPool;
        monitor-enter(r1);
        r2 = r0.successCount;	 Catch:{ all -> 0x0019 }
        if (r2 != 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x0019 }
        return r0;
    L_0x000d:
        monitor-exit(r1);	 Catch:{ all -> 0x0019 }
        r1 = r0.isHealthy(r9);
        if (r1 != 0) goto L_0x0018;
    L_0x0014:
        r3.noNewStreams();
        goto L_0x0000;
    L_0x0018:
        return r0;
    L_0x0019:
        r4 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0019 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.StreamAllocation.findHealthyConnection(int, int, int, int, boolean, boolean):okhttp3.internal.connection.RealConnection");
    }

    private RealConnection findConnection(int i, int i2, int i3, int i4, boolean z) throws IOException {
        Socket releaseIfNoNewStreams;
        Socket socket;
        Connection connection;
        Connection connection2;
        Route route;
        Object obj;
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            } else if (this.codec != null) {
                throw new IllegalStateException("codec != null");
            } else if (this.canceled) {
                throw new IOException("Canceled");
            } else {
                RealConnection realConnection = this.connection;
                releaseIfNoNewStreams = releaseIfNoNewStreams();
                socket = null;
                if (this.connection != null) {
                    realConnection = this.connection;
                    connection = null;
                } else {
                    connection = realConnection;
                    realConnection = null;
                }
                if (!this.reportedAcquired) {
                    connection = null;
                }
                if (realConnection == null) {
                    Internal.instance.get(this.connectionPool, this.address, this, null);
                    if (this.connection != null) {
                        connection2 = this.connection;
                        route = null;
                        obj = 1;
                    } else {
                        route = this.route;
                        connection2 = realConnection;
                    }
                } else {
                    connection2 = realConnection;
                    route = null;
                }
                obj = null;
            }
        }
        Util.closeQuietly(releaseIfNoNewStreams);
        if (connection != null) {
            this.eventListener.connectionReleased(this.call, connection);
        }
        if (obj != null) {
            this.eventListener.connectionAcquired(this.call, connection2);
        }
        if (connection2 != null) {
            return connection2;
        }
        Object obj2;
        if (route != null || (this.routeSelection != null && this.routeSelection.hasNext())) {
            obj2 = null;
        } else {
            this.routeSelection = this.routeSelector.next();
            obj2 = 1;
        }
        synchronized (this.connectionPool) {
            if (this.canceled) {
                throw new IOException("Canceled");
            }
            if (obj2 != null) {
                List all = this.routeSelection.getAll();
                int size = all.size();
                for (int i5 = 0; i5 < size; i5++) {
                    Route route2 = (Route) all.get(i5);
                    Internal.instance.get(this.connectionPool, this.address, this, route2);
                    if (this.connection != null) {
                        connection2 = this.connection;
                        this.route = route2;
                        obj = 1;
                        break;
                    }
                }
            }
            if (obj == null) {
                if (route == null) {
                    route = this.routeSelection.next();
                }
                this.route = route;
                this.refusedStreamCount = 0;
                connection2 = new RealConnection(this.connectionPool, route);
                acquire(connection2, false);
            }
        }
        if (obj != null) {
            this.eventListener.connectionAcquired(this.call, connection2);
            return connection2;
        }
        connection2.connect(i, i2, i3, i4, z, this.call, this.eventListener);
        routeDatabase().connected(connection2.route());
        synchronized (this.connectionPool) {
            this.reportedAcquired = true;
            Internal.instance.put(this.connectionPool, connection2);
            if (connection2.isMultiplexed()) {
                socket = Internal.instance.deduplicate(this.connectionPool, this.address, this);
                connection2 = this.connection;
            }
        }
        Util.closeQuietly(socket);
        this.eventListener.connectionAcquired(this.call, connection2);
        return connection2;
    }

    private Socket releaseIfNoNewStreams() {
        RealConnection realConnection = this.connection;
        return (realConnection == null || !realConnection.noNewStreams) ? null : deallocate(false, false, true);
    }

    public void streamFinished(boolean z, HttpCodec httpCodec, long j, IOException iOException) {
        Connection connection;
        Socket deallocate;
        boolean z2;
        this.eventListener.responseBodyEnd(this.call, j);
        synchronized (this.connectionPool) {
            if (httpCodec != null) {
                if (httpCodec == this.codec) {
                    if (!z) {
                        RealConnection realConnection = this.connection;
                        realConnection.successCount++;
                    }
                    connection = this.connection;
                    deallocate = deallocate(z, false, true);
                    if (this.connection != null) {
                        connection = null;
                    }
                    z2 = this.released;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("expected ");
            stringBuilder.append(this.codec);
            stringBuilder.append(" but was ");
            stringBuilder.append(httpCodec);
            throw new IllegalStateException(stringBuilder.toString());
        }
        Util.closeQuietly(deallocate);
        if (connection != null) {
            this.eventListener.connectionReleased(this.call, connection);
        }
        if (iOException != null) {
            this.eventListener.callFailed(this.call, iOException);
        } else if (z2) {
            this.eventListener.callEnd(this.call);
        }
    }

    public HttpCodec codec() {
        HttpCodec httpCodec;
        synchronized (this.connectionPool) {
            httpCodec = this.codec;
        }
        return httpCodec;
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public Route route() {
        return this.route;
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        Connection connection;
        Socket deallocate;
        synchronized (this.connectionPool) {
            connection = this.connection;
            deallocate = deallocate(false, true, false);
            if (this.connection != null) {
                connection = null;
            }
        }
        Util.closeQuietly(deallocate);
        if (connection != null) {
            this.eventListener.connectionReleased(this.call, connection);
        }
    }

    public void noNewStreams() {
        Connection connection;
        Socket deallocate;
        synchronized (this.connectionPool) {
            connection = this.connection;
            deallocate = deallocate(true, false, false);
            if (this.connection != null) {
                connection = null;
            }
        }
        Util.closeQuietly(deallocate);
        if (connection != null) {
            this.eventListener.connectionReleased(this.call, connection);
        }
    }

    private Socket deallocate(boolean z, boolean z2, boolean z3) {
        if (z3) {
            this.codec = null;
        }
        if (z2) {
            this.released = true;
        }
        if (this.connection != null) {
            if (z) {
                this.connection.noNewStreams = true;
            }
            if (this.codec == null && (this.released || this.connection.noNewStreams)) {
                Socket socket;
                release(this.connection);
                if (this.connection.allocations.isEmpty()) {
                    this.connection.idleAtNanos = System.nanoTime();
                    if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                        socket = this.connection.socket();
                        this.connection = null;
                        return socket;
                    }
                }
                socket = null;
                this.connection = null;
                return socket;
            }
        }
        return null;
    }

    public void cancel() {
        HttpCodec httpCodec;
        RealConnection realConnection;
        synchronized (this.connectionPool) {
            this.canceled = true;
            httpCodec = this.codec;
            realConnection = this.connection;
        }
        if (httpCodec != null) {
            httpCodec.cancel();
        } else if (realConnection != null) {
            realConnection.cancel();
        }
    }

    public void streamFailed(IOException iOException) {
        Connection connection;
        Socket deallocate;
        synchronized (this.connectionPool) {
            boolean z;
            if (iOException instanceof StreamResetException) {
                StreamResetException streamResetException = (StreamResetException) iOException;
                if (streamResetException.errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                }
                if (streamResetException.errorCode != ErrorCode.REFUSED_STREAM || this.refusedStreamCount > 1) {
                    this.route = null;
                }
                z = false;
                connection = this.connection;
                deallocate = deallocate(z, false, true);
                if (!(this.connection == null && this.reportedAcquired)) {
                    connection = null;
                }
            } else {
                if (this.connection != null && (!this.connection.isMultiplexed() || (iOException instanceof ConnectionShutdownException))) {
                    if (this.connection.successCount == 0) {
                        if (!(this.route == null || iOException == null)) {
                            this.routeSelector.connectFailed(this.route, iOException);
                        }
                        this.route = null;
                    }
                }
                z = false;
                connection = this.connection;
                deallocate = deallocate(z, false, true);
                connection = null;
            }
            z = true;
            connection = this.connection;
            deallocate = deallocate(z, false, true);
            connection = null;
        }
        Util.closeQuietly(deallocate);
        if (connection != null) {
            this.eventListener.connectionReleased(this.call, connection);
        }
    }

    public void acquire(RealConnection realConnection, boolean z) {
        if (this.connection == null) {
            this.connection = realConnection;
            this.reportedAcquired = z;
            realConnection.allocations.add(new StreamAllocationReference(this, this.callStackTrace));
            return;
        }
        throw new IllegalStateException();
    }

    private void release(RealConnection realConnection) {
        int size = realConnection.allocations.size();
        for (int i = 0; i < size; i++) {
            if (((Reference) realConnection.allocations.get(i)).get() == this) {
                realConnection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public Socket releaseAndAcquire(RealConnection realConnection) {
        if (this.codec == null && this.connection.allocations.size() == 1) {
            Reference reference = (Reference) this.connection.allocations.get(0);
            Socket deallocate = deallocate(true, false, false);
            this.connection = realConnection;
            realConnection.allocations.add(reference);
            return deallocate;
        }
        throw new IllegalStateException();
    }

    public boolean hasMoreRoutes() {
        return this.route != null || ((this.routeSelection != null && this.routeSelection.hasNext()) || this.routeSelector.hasNext());
    }

    public String toString() {
        RealConnection connection = connection();
        return connection != null ? connection.toString() : this.address.toString();
    }
}
