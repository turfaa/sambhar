package okhttp3.internal.connection;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSession;
import okhttp3.Address;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.ConnectionPool;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http1.Http1Codec;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Codec;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.Http2Connection.Builder;
import okhttp3.internal.http2.Http2Connection.Listener;
import okhttp3.internal.http2.Http2Stream;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.internal.ws.RealWebSocket.Streams;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public final class RealConnection extends Listener implements Connection {
    private static final int MAX_TUNNEL_ATTEMPTS = 21;
    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    public int allocationLimit = 1;
    public final List<Reference<StreamAllocation>> allocations = new ArrayList();
    private final ConnectionPool connectionPool;
    private Handshake handshake;
    private Http2Connection http2Connection;
    public long idleAtNanos = Long.MAX_VALUE;
    public boolean noNewStreams;
    private Protocol protocol;
    private Socket rawSocket;
    private final Route route;
    private BufferedSink sink;
    private Socket socket;
    private BufferedSource source;
    public int successCount;

    public RealConnection(ConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    public static RealConnection testConnection(ConnectionPool connectionPool, Route route, Socket socket, long j) {
        RealConnection realConnection = new RealConnection(connectionPool, route);
        realConnection.socket = socket;
        realConnection.idleAtNanos = j;
        return realConnection;
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x011d  */
    /* JADX WARNING: Missing block: B:26:0x00ba, code skipped:
            if (r7.route.requiresTunnel() == false) goto L_0x00ce;
     */
    /* JADX WARNING: Missing block: B:28:0x00be, code skipped:
            if (r7.rawSocket == null) goto L_0x00c1;
     */
    /* JADX WARNING: Missing block: B:30:0x00cd, code skipped:
            throw new okhttp3.internal.connection.RouteException(new java.net.ProtocolException("Too many tunnel connections attempted: 21"));
     */
    /* JADX WARNING: Missing block: B:32:0x00d0, code skipped:
            if (r7.http2Connection == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:33:0x00d2, code skipped:
            r1 = r7.connectionPool;
     */
    /* JADX WARNING: Missing block: B:34:0x00d4, code skipped:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:36:?, code skipped:
            r7.allocationLimit = r7.http2Connection.maxConcurrentStreams();
     */
    /* JADX WARNING: Missing block: B:37:0x00dd, code skipped:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:62:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:63:?, code skipped:
            return;
     */
    public void connect(int r17, int r18, int r19, int r20, boolean r21, okhttp3.Call r22, okhttp3.EventListener r23) {
        /*
        r16 = this;
        r7 = r16;
        r8 = r22;
        r9 = r23;
        r0 = r7.protocol;
        if (r0 != 0) goto L_0x0132;
    L_0x000a:
        r0 = r7.route;
        r0 = r0.address();
        r0 = r0.connectionSpecs();
        r10 = new okhttp3.internal.connection.ConnectionSpecSelector;
        r10.<init>(r0);
        r1 = r7.route;
        r1 = r1.address();
        r1 = r1.sslSocketFactory();
        if (r1 != 0) goto L_0x0074;
    L_0x0025:
        r1 = okhttp3.ConnectionSpec.CLEARTEXT;
        r0 = r0.contains(r1);
        if (r0 == 0) goto L_0x0067;
    L_0x002d:
        r0 = r7.route;
        r0 = r0.address();
        r0 = r0.url();
        r0 = r0.host();
        r1 = okhttp3.internal.platform.Platform.get();
        r1 = r1.isCleartextTrafficPermitted(r0);
        if (r1 == 0) goto L_0x0046;
    L_0x0045:
        goto L_0x0074;
    L_0x0046:
        r1 = new okhttp3.internal.connection.RouteException;
        r2 = new java.net.UnknownServiceException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "CLEARTEXT communication to ";
        r3.append(r4);
        r3.append(r0);
        r0 = " not permitted by network security policy";
        r3.append(r0);
        r0 = r3.toString();
        r2.<init>(r0);
        r1.<init>(r2);
        throw r1;
    L_0x0067:
        r0 = new okhttp3.internal.connection.RouteException;
        r1 = new java.net.UnknownServiceException;
        r2 = "CLEARTEXT communication not enabled for client";
        r1.<init>(r2);
        r0.<init>(r1);
        throw r0;
    L_0x0074:
        r11 = 0;
        r12 = r11;
    L_0x0076:
        r0 = r7.route;	 Catch:{ IOException -> 0x00e7 }
        r0 = r0.requiresTunnel();	 Catch:{ IOException -> 0x00e7 }
        if (r0 == 0) goto L_0x0097;
    L_0x007e:
        r1 = r16;
        r2 = r17;
        r3 = r18;
        r4 = r19;
        r5 = r22;
        r6 = r23;
        r1.connectTunnel(r2, r3, r4, r5, r6);	 Catch:{ IOException -> 0x00e7 }
        r0 = r7.rawSocket;	 Catch:{ IOException -> 0x00e7 }
        if (r0 != 0) goto L_0x0092;
    L_0x0091:
        goto L_0x00b4;
    L_0x0092:
        r13 = r17;
        r14 = r18;
        goto L_0x009e;
    L_0x0097:
        r13 = r17;
        r14 = r18;
        r7.connectSocket(r13, r14, r8, r9);	 Catch:{ IOException -> 0x00e5 }
    L_0x009e:
        r15 = r20;
        r7.establishProtocol(r10, r15, r8, r9);	 Catch:{ IOException -> 0x00e3 }
        r0 = r7.route;	 Catch:{ IOException -> 0x00e3 }
        r0 = r0.socketAddress();	 Catch:{ IOException -> 0x00e3 }
        r1 = r7.route;	 Catch:{ IOException -> 0x00e3 }
        r1 = r1.proxy();	 Catch:{ IOException -> 0x00e3 }
        r2 = r7.protocol;	 Catch:{ IOException -> 0x00e3 }
        r9.connectEnd(r8, r0, r1, r2);	 Catch:{ IOException -> 0x00e3 }
    L_0x00b4:
        r0 = r7.route;
        r0 = r0.requiresTunnel();
        if (r0 == 0) goto L_0x00ce;
    L_0x00bc:
        r0 = r7.rawSocket;
        if (r0 == 0) goto L_0x00c1;
    L_0x00c0:
        goto L_0x00ce;
    L_0x00c1:
        r0 = new java.net.ProtocolException;
        r1 = "Too many tunnel connections attempted: 21";
        r0.<init>(r1);
        r1 = new okhttp3.internal.connection.RouteException;
        r1.<init>(r0);
        throw r1;
    L_0x00ce:
        r0 = r7.http2Connection;
        if (r0 == 0) goto L_0x00e2;
    L_0x00d2:
        r1 = r7.connectionPool;
        monitor-enter(r1);
        r0 = r7.http2Connection;	 Catch:{ all -> 0x00df }
        r0 = r0.maxConcurrentStreams();	 Catch:{ all -> 0x00df }
        r7.allocationLimit = r0;	 Catch:{ all -> 0x00df }
        monitor-exit(r1);	 Catch:{ all -> 0x00df }
        goto L_0x00e2;
    L_0x00df:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x00df }
        throw r0;
    L_0x00e2:
        return;
    L_0x00e3:
        r0 = move-exception;
        goto L_0x00ee;
    L_0x00e5:
        r0 = move-exception;
        goto L_0x00ec;
    L_0x00e7:
        r0 = move-exception;
        r13 = r17;
        r14 = r18;
    L_0x00ec:
        r15 = r20;
    L_0x00ee:
        r1 = r7.socket;
        okhttp3.internal.Util.closeQuietly(r1);
        r1 = r7.rawSocket;
        okhttp3.internal.Util.closeQuietly(r1);
        r7.socket = r11;
        r7.rawSocket = r11;
        r7.source = r11;
        r7.sink = r11;
        r7.handshake = r11;
        r7.protocol = r11;
        r7.http2Connection = r11;
        r1 = r7.route;
        r3 = r1.socketAddress();
        r1 = r7.route;
        r4 = r1.proxy();
        r5 = 0;
        r1 = r23;
        r2 = r22;
        r6 = r0;
        r1.connectFailed(r2, r3, r4, r5, r6);
        if (r12 != 0) goto L_0x0124;
    L_0x011d:
        r1 = new okhttp3.internal.connection.RouteException;
        r1.<init>(r0);
        r12 = r1;
        goto L_0x0127;
    L_0x0124:
        r12.addConnectException(r0);
    L_0x0127:
        if (r21 == 0) goto L_0x0131;
    L_0x0129:
        r0 = r10.connectionFailed(r0);
        if (r0 == 0) goto L_0x0131;
    L_0x012f:
        goto L_0x0076;
    L_0x0131:
        throw r12;
    L_0x0132:
        r0 = new java.lang.IllegalStateException;
        r1 = "already connected";
        r0.<init>(r1);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.connect(int, int, int, int, boolean, okhttp3.Call, okhttp3.EventListener):void");
    }

    private void connectTunnel(int i, int i2, int i3, Call call, EventListener eventListener) throws IOException {
        Request createTunnelRequest = createTunnelRequest();
        HttpUrl url = createTunnelRequest.url();
        int i4 = 0;
        while (i4 < 21) {
            connectSocket(i, i2, call, eventListener);
            createTunnelRequest = createTunnel(i2, i3, createTunnelRequest, url);
            if (createTunnelRequest != null) {
                Util.closeQuietly(this.rawSocket);
                this.rawSocket = null;
                this.sink = null;
                this.source = null;
                eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), null);
                i4++;
            } else {
                return;
            }
        }
    }

    private void connectSocket(int i, int i2, Call call, EventListener eventListener) throws IOException {
        Socket createSocket;
        Proxy proxy = this.route.proxy();
        Address address = this.route.address();
        if (proxy.type() == Type.DIRECT || proxy.type() == Type.HTTP) {
            createSocket = address.socketFactory().createSocket();
        } else {
            createSocket = new Socket(proxy);
        }
        this.rawSocket = createSocket;
        eventListener.connectStart(call, this.route.socketAddress(), proxy);
        this.rawSocket.setSoTimeout(i2);
        try {
            Platform.get().connectSocket(this.rawSocket, this.route.socketAddress(), i);
            try {
                this.source = Okio.buffer(Okio.source(this.rawSocket));
                this.sink = Okio.buffer(Okio.sink(this.rawSocket));
            } catch (NullPointerException e) {
                if (NPE_THROW_WITH_NULL.equals(e.getMessage())) {
                    throw new IOException(e);
                }
            }
        } catch (ConnectException e2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to connect to ");
            stringBuilder.append(this.route.socketAddress());
            ConnectException connectException = new ConnectException(stringBuilder.toString());
            connectException.initCause(e2);
            throw connectException;
        }
    }

    private void establishProtocol(ConnectionSpecSelector connectionSpecSelector, int i, Call call, EventListener eventListener) throws IOException {
        if (this.route.address().sslSocketFactory() == null) {
            this.protocol = Protocol.HTTP_1_1;
            this.socket = this.rawSocket;
            return;
        }
        eventListener.secureConnectStart(call);
        connectTls(connectionSpecSelector);
        eventListener.secureConnectEnd(call, this.handshake);
        if (this.protocol == Protocol.HTTP_2) {
            this.socket.setSoTimeout(0);
            this.http2Connection = new Builder(true).socket(this.socket, this.route.address().url().host(), this.source, this.sink).listener(this).pingIntervalMillis(i).build();
            this.http2Connection.start();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0125 A:{Catch:{ all -> 0x0115 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011f A:{Catch:{ all -> 0x0115 }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0128  */
    private void connectTls(okhttp3.internal.connection.ConnectionSpecSelector r8) throws java.io.IOException {
        /*
        r7 = this;
        r0 = r7.route;
        r0 = r0.address();
        r1 = r0.sslSocketFactory();
        r2 = 0;
        r3 = r7.rawSocket;	 Catch:{ AssertionError -> 0x0118 }
        r4 = r0.url();	 Catch:{ AssertionError -> 0x0118 }
        r4 = r4.host();	 Catch:{ AssertionError -> 0x0118 }
        r5 = r0.url();	 Catch:{ AssertionError -> 0x0118 }
        r5 = r5.port();	 Catch:{ AssertionError -> 0x0118 }
        r6 = 1;
        r1 = r1.createSocket(r3, r4, r5, r6);	 Catch:{ AssertionError -> 0x0118 }
        r1 = (javax.net.ssl.SSLSocket) r1;	 Catch:{ AssertionError -> 0x0118 }
        r8 = r8.configureSecureSocket(r1);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3 = r8.supportsTlsExtensions();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r3 == 0) goto L_0x0041;
    L_0x002e:
        r3 = okhttp3.internal.platform.Platform.get();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r4 = r0.url();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r4 = r4.host();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r5 = r0.protocols();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.configureTlsExtensions(r1, r4, r5);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
    L_0x0041:
        r1.startHandshake();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3 = r1.getSession();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r4 = r7.isValid(r3);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r4 == 0) goto L_0x0108;
    L_0x004e:
        r4 = okhttp3.Handshake.get(r3);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r5 = r0.hostnameVerifier();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r6 = r0.url();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r6 = r6.host();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3 = r5.verify(r6, r3);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r3 == 0) goto L_0x00b6;
    L_0x0064:
        r3 = r0.certificatePinner();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r0.url();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r0.host();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r5 = r4.peerCertificates();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.check(r0, r5);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = r8.supportsTlsExtensions();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r8 == 0) goto L_0x0085;
    L_0x007d:
        r8 = okhttp3.internal.platform.Platform.get();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r2 = r8.getSelectedProtocol(r1);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
    L_0x0085:
        r7.socket = r1;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = r7.socket;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = okio.Okio.source(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = okio.Okio.buffer(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r7.source = r8;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = r7.socket;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = okio.Okio.sink(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = okio.Okio.buffer(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r7.sink = r8;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r7.handshake = r4;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r2 == 0) goto L_0x00a8;
    L_0x00a3:
        r8 = okhttp3.Protocol.get(r2);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        goto L_0x00aa;
    L_0x00a8:
        r8 = okhttp3.Protocol.HTTP_1_1;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
    L_0x00aa:
        r7.protocol = r8;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        if (r1 == 0) goto L_0x00b5;
    L_0x00ae:
        r8 = okhttp3.internal.platform.Platform.get();
        r8.afterHandshake(r1);
    L_0x00b5:
        return;
    L_0x00b6:
        r8 = r4.peerCertificates();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r2 = 0;
        r8 = r8.get(r2);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = (java.security.cert.X509Certificate) r8;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r2 = new javax.net.ssl.SSLPeerUnverifiedException;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3 = new java.lang.StringBuilder;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.<init>();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r4 = "Hostname ";
        r3.append(r4);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r0.url();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r0.host();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = " not verified:\n    certificate: ";
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = okhttp3.CertificatePinner.pin(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = "\n    DN: ";
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r8.getSubjectDN();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = r0.getName();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = "\n    subjectAltNames: ";
        r3.append(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = okhttp3.internal.tls.OkHostnameVerifier.allSubjectAltNames(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r3.append(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r8 = r3.toString();	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r2.<init>(r8);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        throw r2;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
    L_0x0108:
        r8 = new java.io.IOException;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        r0 = "a valid ssl session was not established";
        r8.<init>(r0);	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
        throw r8;	 Catch:{ AssertionError -> 0x0112, all -> 0x0110 }
    L_0x0110:
        r8 = move-exception;
        goto L_0x0126;
    L_0x0112:
        r8 = move-exception;
        r2 = r1;
        goto L_0x0119;
    L_0x0115:
        r8 = move-exception;
        r1 = r2;
        goto L_0x0126;
    L_0x0118:
        r8 = move-exception;
    L_0x0119:
        r0 = okhttp3.internal.Util.isAndroidGetsocknameError(r8);	 Catch:{ all -> 0x0115 }
        if (r0 == 0) goto L_0x0125;
    L_0x011f:
        r0 = new java.io.IOException;	 Catch:{ all -> 0x0115 }
        r0.<init>(r8);	 Catch:{ all -> 0x0115 }
        throw r0;	 Catch:{ all -> 0x0115 }
    L_0x0125:
        throw r8;	 Catch:{ all -> 0x0115 }
    L_0x0126:
        if (r1 == 0) goto L_0x012f;
    L_0x0128:
        r0 = okhttp3.internal.platform.Platform.get();
        r0.afterHandshake(r1);
    L_0x012f:
        okhttp3.internal.Util.closeQuietly(r1);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.connectTls(okhttp3.internal.connection.ConnectionSpecSelector):void");
    }

    private boolean isValid(SSLSession sSLSession) {
        return ("NONE".equals(sSLSession.getProtocol()) || "SSL_NULL_WITH_NULL_NULL".equals(sSLSession.getCipherSuite())) ? false : true;
    }

    private Request createTunnel(int i, int i2, Request request, HttpUrl httpUrl) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CONNECT ");
        stringBuilder.append(Util.hostHeader(httpUrl, true));
        stringBuilder.append(" HTTP/1.1");
        String stringBuilder2 = stringBuilder.toString();
        while (true) {
            Http1Codec http1Codec = new Http1Codec(null, null, this.source, this.sink);
            this.source.timeout().timeout((long) i, TimeUnit.MILLISECONDS);
            this.sink.timeout().timeout((long) i2, TimeUnit.MILLISECONDS);
            http1Codec.writeRequest(request.headers(), stringBuilder2);
            http1Codec.finishRequest();
            Response build = http1Codec.readResponseHeaders(false).request(request).build();
            long contentLength = HttpHeaders.contentLength(build);
            if (contentLength == -1) {
                contentLength = 0;
            }
            Source newFixedLengthSource = http1Codec.newFixedLengthSource(contentLength);
            Util.skipAll(newFixedLengthSource, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, TimeUnit.MILLISECONDS);
            newFixedLengthSource.close();
            int code = build.code();
            if (code != Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                if (code == 407) {
                    Request authenticate = this.route.address().proxyAuthenticator().authenticate(this.route, build);
                    if (authenticate == null) {
                        throw new IOException("Failed to authenticate with proxy");
                    } else if ("close".equalsIgnoreCase(build.header("Connection"))) {
                        return authenticate;
                    } else {
                        request = authenticate;
                    }
                } else {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Unexpected response code for CONNECT: ");
                    stringBuilder3.append(build.code());
                    throw new IOException(stringBuilder3.toString());
                }
            } else if (this.source.buffer().exhausted() && this.sink.buffer().exhausted()) {
                return null;
            } else {
                throw new IOException("TLS tunnel buffered too many bytes!");
            }
        }
    }

    private Request createTunnelRequest() {
        return new Request.Builder().url(this.route.address().url()).header("Host", Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
    }

    /* JADX WARNING: Missing block: B:35:0x00a6, code skipped:
            return false;
     */
    public boolean isEligible(okhttp3.Address r5, @javax.annotation.Nullable okhttp3.Route r6) {
        /*
        r4 = this;
        r0 = r4.allocations;
        r0 = r0.size();
        r1 = r4.allocationLimit;
        r2 = 0;
        if (r0 >= r1) goto L_0x00a6;
    L_0x000b:
        r0 = r4.noNewStreams;
        if (r0 == 0) goto L_0x0011;
    L_0x000f:
        goto L_0x00a6;
    L_0x0011:
        r0 = okhttp3.internal.Internal.instance;
        r1 = r4.route;
        r1 = r1.address();
        r0 = r0.equalsNonHost(r1, r5);
        if (r0 != 0) goto L_0x0020;
    L_0x001f:
        return r2;
    L_0x0020:
        r0 = r5.url();
        r0 = r0.host();
        r1 = r4.route();
        r1 = r1.address();
        r1 = r1.url();
        r1 = r1.host();
        r0 = r0.equals(r1);
        r1 = 1;
        if (r0 == 0) goto L_0x0040;
    L_0x003f:
        return r1;
    L_0x0040:
        r0 = r4.http2Connection;
        if (r0 != 0) goto L_0x0045;
    L_0x0044:
        return r2;
    L_0x0045:
        if (r6 != 0) goto L_0x0048;
    L_0x0047:
        return r2;
    L_0x0048:
        r0 = r6.proxy();
        r0 = r0.type();
        r3 = java.net.Proxy.Type.DIRECT;
        if (r0 == r3) goto L_0x0055;
    L_0x0054:
        return r2;
    L_0x0055:
        r0 = r4.route;
        r0 = r0.proxy();
        r0 = r0.type();
        r3 = java.net.Proxy.Type.DIRECT;
        if (r0 == r3) goto L_0x0064;
    L_0x0063:
        return r2;
    L_0x0064:
        r0 = r4.route;
        r0 = r0.socketAddress();
        r3 = r6.socketAddress();
        r0 = r0.equals(r3);
        if (r0 != 0) goto L_0x0075;
    L_0x0074:
        return r2;
    L_0x0075:
        r6 = r6.address();
        r6 = r6.hostnameVerifier();
        r0 = okhttp3.internal.tls.OkHostnameVerifier.INSTANCE;
        if (r6 == r0) goto L_0x0082;
    L_0x0081:
        return r2;
    L_0x0082:
        r6 = r5.url();
        r6 = r4.supportsUrl(r6);
        if (r6 != 0) goto L_0x008d;
    L_0x008c:
        return r2;
    L_0x008d:
        r6 = r5.certificatePinner();	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        r5 = r5.url();	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        r5 = r5.host();	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        r0 = r4.handshake();	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        r0 = r0.peerCertificates();	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        r6.check(r5, r0);	 Catch:{ SSLPeerUnverifiedException -> 0x00a5 }
        return r1;
    L_0x00a5:
        return r2;
    L_0x00a6:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.isEligible(okhttp3.Address, okhttp3.Route):boolean");
    }

    public boolean supportsUrl(HttpUrl httpUrl) {
        if (httpUrl.port() != this.route.address().url().port()) {
            return false;
        }
        boolean z = true;
        if (httpUrl.host().equals(this.route.address().url().host())) {
            return true;
        }
        if (this.handshake == null || !OkHostnameVerifier.INSTANCE.verify(httpUrl.host(), (X509Certificate) this.handshake.peerCertificates().get(0))) {
            z = false;
        }
        return z;
    }

    public HttpCodec newCodec(OkHttpClient okHttpClient, Chain chain, StreamAllocation streamAllocation) throws SocketException {
        if (this.http2Connection != null) {
            return new Http2Codec(okHttpClient, chain, streamAllocation, this.http2Connection);
        }
        this.socket.setSoTimeout(chain.readTimeoutMillis());
        this.source.timeout().timeout((long) chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        this.sink.timeout().timeout((long) chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
        return new Http1Codec(okHttpClient, streamAllocation, this.source, this.sink);
    }

    public Streams newWebSocketStreams(StreamAllocation streamAllocation) {
        final StreamAllocation streamAllocation2 = streamAllocation;
        return new Streams(true, this.source, this.sink) {
            public void close() throws IOException {
                streamAllocation2.streamFinished(true, streamAllocation2.codec(), -1, null);
            }
        };
    }

    public Route route() {
        return this.route;
    }

    public void cancel() {
        Util.closeQuietly(this.rawSocket);
    }

    public Socket socket() {
        return this.socket;
    }

    public boolean isHealthy(boolean z) {
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        if (this.http2Connection != null) {
            return this.http2Connection.isShutdown() ^ 1;
        }
        if (z) {
            int soTimeout;
            try {
                soTimeout = this.socket.getSoTimeout();
                this.socket.setSoTimeout(1);
                if (this.source.exhausted()) {
                    this.socket.setSoTimeout(soTimeout);
                    return false;
                }
                this.socket.setSoTimeout(soTimeout);
                return true;
            } catch (SocketTimeoutException unused) {
            } catch (IOException unused2) {
                return false;
            } catch (Throwable th) {
                this.socket.setSoTimeout(soTimeout);
            }
        }
        return true;
    }

    public void onStream(Http2Stream http2Stream) throws IOException {
        http2Stream.close(ErrorCode.REFUSED_STREAM);
    }

    public void onSettings(Http2Connection http2Connection) {
        synchronized (this.connectionPool) {
            this.allocationLimit = http2Connection.maxConcurrentStreams();
        }
    }

    public Handshake handshake() {
        return this.handshake;
    }

    public boolean isMultiplexed() {
        return this.http2Connection != null;
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Connection{");
        stringBuilder.append(this.route.address().url().host());
        stringBuilder.append(":");
        stringBuilder.append(this.route.address().url().port());
        stringBuilder.append(", proxy=");
        stringBuilder.append(this.route.proxy());
        stringBuilder.append(" hostAddress=");
        stringBuilder.append(this.route.socketAddress());
        stringBuilder.append(" cipherSuite=");
        stringBuilder.append(this.handshake != null ? this.handshake.cipherSuite() : "none");
        stringBuilder.append(" protocol=");
        stringBuilder.append(this.protocol);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
