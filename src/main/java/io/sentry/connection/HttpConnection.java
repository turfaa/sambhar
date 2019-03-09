package io.sentry.connection;

import com.bumptech.glide.load.Key;
import io.sentry.environment.SentryEnvironment;
import io.sentry.marshaller.Marshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnection extends AbstractConnection {
    private static final int DEFAULT_CONNECTION_TIMEOUT = ((int) TimeUnit.SECONDS.toMillis(1));
    private static final int DEFAULT_READ_TIMEOUT = ((int) TimeUnit.SECONDS.toMillis(5));
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final HostnameVerifier NAIVE_VERIFIER = new HostnameVerifier() {
        public boolean verify(String str, SSLSession sSLSession) {
            return true;
        }
    };
    private static final String SENTRY_AUTH = "X-Sentry-Auth";
    private static final String USER_AGENT = "User-Agent";
    private static final Charset UTF_8 = Charset.forName(Key.STRING_CHARSET_NAME);
    private static final Logger logger = LoggerFactory.getLogger(HttpConnection.class);
    private boolean bypassSecurity = false;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private EventSampler eventSampler;
    private Marshaller marshaller;
    private final Proxy proxy;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private final URL sentryUrl;

    public void close() throws IOException {
    }

    public HttpConnection(URL url, String str, String str2, Proxy proxy, EventSampler eventSampler) {
        super(str, str2);
        this.sentryUrl = url;
        this.proxy = proxy;
        this.eventSampler = eventSampler;
    }

    public static URL getSentryApiUrl(URI uri, String str) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(uri.toString());
            stringBuilder.append("api/");
            stringBuilder.append(str);
            stringBuilder.append("/store/");
            return new URL(stringBuilder.toString());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Couldn't build a valid URL from the Sentry API.", e);
        }
    }

    /* Access modifiers changed, original: protected */
    public HttpURLConnection getConnection() {
        try {
            HttpURLConnection httpURLConnection;
            if (this.proxy != null) {
                httpURLConnection = (HttpURLConnection) this.sentryUrl.openConnection(this.proxy);
            } else {
                httpURLConnection = (HttpURLConnection) this.sentryUrl.openConnection();
            }
            if (this.bypassSecurity && (httpURLConnection instanceof HttpsURLConnection)) {
                ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(NAIVE_VERIFIER);
            }
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(this.connectionTimeout);
            httpURLConnection.setReadTimeout(this.readTimeout);
            httpURLConnection.setRequestProperty(USER_AGENT, SentryEnvironment.getSentryName());
            httpURLConnection.setRequestProperty(SENTRY_AUTH, getAuthHeader());
            if (this.marshaller.getContentType() != null) {
                httpURLConnection.setRequestProperty("Content-Type", this.marshaller.getContentType());
            }
            if (this.marshaller.getContentEncoding() != null) {
                httpURLConnection.setRequestProperty("Content-Encoding", this.marshaller.getContentEncoding());
            }
            return httpURLConnection;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't set up a connection to the Sentry server.", e);
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007d A:{SYNTHETIC, Splitter:B:28:0x007d} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005a A:{Catch:{ IOException -> 0x002e, all -> 0x002b, IOException -> 0x008f }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0095 A:{Catch:{ IOException -> 0x002e, all -> 0x002b, IOException -> 0x008f }} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:36:0x008f */
    public void doSend(io.sentry.event.Event r9) throws io.sentry.connection.ConnectionException {
        /*
        r8 = this;
        r0 = r8.eventSampler;
        if (r0 == 0) goto L_0x000d;
    L_0x0004:
        r0 = r8.eventSampler;
        r0 = r0.shouldSendEvent(r9);
        if (r0 != 0) goto L_0x000d;
    L_0x000c:
        return;
    L_0x000d:
        r0 = r8.getConnection();
        r0.connect();	 Catch:{ IOException -> 0x002e }
        r1 = r0.getOutputStream();	 Catch:{ IOException -> 0x002e }
        r2 = r8.marshaller;	 Catch:{ IOException -> 0x002e }
        r2.marshall(r9, r1);	 Catch:{ IOException -> 0x002e }
        r1.close();	 Catch:{ IOException -> 0x002e }
        r1 = r0.getInputStream();	 Catch:{ IOException -> 0x002e }
        r1.close();	 Catch:{ IOException -> 0x002e }
        r0.disconnect();
        return;
    L_0x002b:
        r9 = move-exception;
        goto L_0x00a9;
    L_0x002e:
        r1 = move-exception;
        r2 = "Retry-After";
        r2 = r0.getHeaderField(r2);	 Catch:{ all -> 0x002b }
        r3 = 0;
        if (r2 == 0) goto L_0x0049;
    L_0x0038:
        r4 = java.lang.Double.parseDouble(r2);	 Catch:{ NumberFormatException -> 0x0049 }
        r6 = 4652007308841189376; // 0x408f400000000000 float:0.0 double:1000.0;
        r4 = r4 * r6;
        r4 = (long) r4;	 Catch:{ NumberFormatException -> 0x0049 }
        r2 = java.lang.Long.valueOf(r4);	 Catch:{ NumberFormatException -> 0x0049 }
        goto L_0x004a;
    L_0x0049:
        r2 = r3;
    L_0x004a:
        r4 = r0.getResponseCode();	 Catch:{ IOException -> 0x008e }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ IOException -> 0x008e }
        r5 = r4.intValue();	 Catch:{ IOException -> 0x008f }
        r6 = 403; // 0x193 float:5.65E-43 double:1.99E-321;
        if (r5 != r6) goto L_0x007d;
    L_0x005a:
        r5 = logger;	 Catch:{ IOException -> 0x008f }
        r6 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x008f }
        r6.<init>();	 Catch:{ IOException -> 0x008f }
        r7 = "Event '";
        r6.append(r7);	 Catch:{ IOException -> 0x008f }
        r9 = r9.getId();	 Catch:{ IOException -> 0x008f }
        r6.append(r9);	 Catch:{ IOException -> 0x008f }
        r9 = "' was rejected by the Sentry server due to a filter.";
        r6.append(r9);	 Catch:{ IOException -> 0x008f }
        r9 = r6.toString();	 Catch:{ IOException -> 0x008f }
        r5.debug(r9);	 Catch:{ IOException -> 0x008f }
        r0.disconnect();
        return;
    L_0x007d:
        r9 = r4.intValue();	 Catch:{ IOException -> 0x008f }
        r5 = 429; // 0x1ad float:6.01E-43 double:2.12E-321;
        if (r9 == r5) goto L_0x0086;
    L_0x0085:
        goto L_0x008f;
    L_0x0086:
        r9 = new io.sentry.connection.TooManyRequestsException;	 Catch:{ IOException -> 0x008f }
        r5 = "Too many requests to Sentry: https://docs.sentry.io/learn/quotas/";
        r9.<init>(r5, r1, r2, r4);	 Catch:{ IOException -> 0x008f }
        throw r9;	 Catch:{ IOException -> 0x008f }
    L_0x008e:
        r4 = r3;
    L_0x008f:
        r9 = r0.getErrorStream();	 Catch:{ all -> 0x002b }
        if (r9 == 0) goto L_0x0099;
    L_0x0095:
        r3 = r8.getErrorMessageFromStream(r9);	 Catch:{ all -> 0x002b }
    L_0x0099:
        if (r3 == 0) goto L_0x00a1;
    L_0x009b:
        r9 = r3.isEmpty();	 Catch:{ all -> 0x002b }
        if (r9 == 0) goto L_0x00a3;
    L_0x00a1:
        r3 = "An exception occurred while submitting the event to the Sentry server.";
    L_0x00a3:
        r9 = new io.sentry.connection.ConnectionException;	 Catch:{ all -> 0x002b }
        r9.<init>(r3, r1, r2, r4);	 Catch:{ all -> 0x002b }
        throw r9;	 Catch:{ all -> 0x002b }
    L_0x00a9:
        r0.disconnect();
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.connection.HttpConnection.doSend(io.sentry.event.Event):void");
    }

    private String getErrorMessageFromStream(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        Object obj = 1;
        while (true) {
            try {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                if (obj == null) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(readLine);
                obj = null;
            } catch (Exception e) {
                logger.error("Exception while reading the error message from the connection.", e);
            }
        }
        return stringBuilder.toString();
    }

    @Deprecated
    public void setTimeout(int i) {
        this.connectionTimeout = i;
    }

    public void setConnectionTimeout(int i) {
        this.connectionTimeout = i;
    }

    public void setReadTimeout(int i) {
        this.readTimeout = i;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setBypassSecurity(boolean z) {
        this.bypassSecurity = z;
    }
}
