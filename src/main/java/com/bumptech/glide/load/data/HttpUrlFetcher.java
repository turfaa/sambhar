package com.bumptech.glide.load.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.data.DataFetcher.DataCallback;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ContentLengthInputStream;
import com.bumptech.glide.util.LogTime;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUrlFetcher implements DataFetcher<InputStream> {
    @VisibleForTesting
    static final HttpUrlConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultHttpUrlConnectionFactory();
    private static final int INVALID_STATUS_CODE = -1;
    private static final int MAXIMUM_REDIRECTS = 5;
    private static final String TAG = "HttpUrlFetcher";
    private final HttpUrlConnectionFactory connectionFactory;
    private final GlideUrl glideUrl;
    private volatile boolean isCancelled;
    private InputStream stream;
    private final int timeout;
    private HttpURLConnection urlConnection;

    interface HttpUrlConnectionFactory {
        HttpURLConnection build(URL url) throws IOException;
    }

    private static class DefaultHttpUrlConnectionFactory implements HttpUrlConnectionFactory {
        DefaultHttpUrlConnectionFactory() {
        }

        public HttpURLConnection build(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }

    public HttpUrlFetcher(GlideUrl glideUrl, int i) {
        this(glideUrl, i, DEFAULT_CONNECTION_FACTORY);
    }

    @VisibleForTesting
    HttpUrlFetcher(GlideUrl glideUrl, int i, HttpUrlConnectionFactory httpUrlConnectionFactory) {
        this.glideUrl = glideUrl;
        this.timeout = i;
        this.connectionFactory = httpUrlConnectionFactory;
    }

    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> dataCallback) {
        long logTime = LogTime.getLogTime();
        String str;
        StringBuilder stringBuilder;
        try {
            dataCallback.onDataReady(loadDataWithRedirects(this.glideUrl.toURL(), 0, null, this.glideUrl.getHeaders()));
            if (Log.isLoggable(TAG, 2)) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Finished http url fetcher fetch in ");
                stringBuilder.append(LogTime.getElapsedMillis(logTime));
                Log.v(str, stringBuilder.toString());
            }
        } catch (IOException e) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Failed to load data for url", e);
            }
            dataCallback.onLoadFailed(e);
            if (Log.isLoggable(TAG, 2)) {
                str = TAG;
                stringBuilder = new StringBuilder();
            }
        } catch (Throwable th) {
            if (Log.isLoggable(TAG, 2)) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Finished http url fetcher fetch in ");
                stringBuilder2.append(LogTime.getElapsedMillis(logTime));
                Log.v(TAG, stringBuilder2.toString());
            }
        }
    }

    private InputStream loadDataWithRedirects(URL url, int i, URL url2, Map<String, String> map) throws IOException {
        if (i < 5) {
            if (url2 != null) {
                try {
                    if (url.toURI().equals(url2.toURI())) {
                        throw new HttpException("In re-direct loop");
                    }
                } catch (URISyntaxException unused) {
                }
            }
            this.urlConnection = this.connectionFactory.build(url);
            for (Entry entry : map.entrySet()) {
                this.urlConnection.addRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
            this.urlConnection.setConnectTimeout(this.timeout);
            this.urlConnection.setReadTimeout(this.timeout);
            this.urlConnection.setUseCaches(false);
            this.urlConnection.setDoInput(true);
            this.urlConnection.setInstanceFollowRedirects(false);
            this.urlConnection.connect();
            this.stream = this.urlConnection.getInputStream();
            if (this.isCancelled) {
                return null;
            }
            int responseCode = this.urlConnection.getResponseCode();
            if (isHttpOk(responseCode)) {
                return getStreamForSuccessfulRequest(this.urlConnection);
            }
            if (isHttpRedirect(responseCode)) {
                String headerField = this.urlConnection.getHeaderField("Location");
                if (TextUtils.isEmpty(headerField)) {
                    throw new HttpException("Received empty or null redirect url");
                }
                URL url3 = new URL(url, headerField);
                cleanup();
                return loadDataWithRedirects(url3, i + 1, url, map);
            } else if (responseCode == -1) {
                throw new HttpException(responseCode);
            } else {
                throw new HttpException(this.urlConnection.getResponseMessage(), responseCode);
            }
        }
        throw new HttpException("Too many (> 5) redirects!");
    }

    private static boolean isHttpOk(int i) {
        return i / 100 == 2;
    }

    private static boolean isHttpRedirect(int i) {
        return i / 100 == 3;
    }

    private InputStream getStreamForSuccessfulRequest(HttpURLConnection httpURLConnection) throws IOException {
        if (TextUtils.isEmpty(httpURLConnection.getContentEncoding())) {
            this.stream = ContentLengthInputStream.obtain(httpURLConnection.getInputStream(), (long) httpURLConnection.getContentLength());
        } else {
            if (Log.isLoggable(TAG, 3)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Got non empty content encoding: ");
                stringBuilder.append(httpURLConnection.getContentEncoding());
                Log.d(str, stringBuilder.toString());
            }
            this.stream = httpURLConnection.getInputStream();
        }
        return this.stream;
    }

    public void cleanup() {
        if (this.stream != null) {
            try {
                this.stream.close();
            } catch (IOException unused) {
            }
        }
        if (this.urlConnection != null) {
            this.urlConnection.disconnect();
        }
        this.urlConnection = null;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    @NonNull
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
