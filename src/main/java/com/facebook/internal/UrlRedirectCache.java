package com.facebook.internal;

import android.net.Uri;
import com.facebook.LoggingBehavior;
import com.facebook.internal.FileLruCache.Limits;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

class UrlRedirectCache {
    private static final String REDIRECT_CONTENT_TAG;
    static final String TAG = "UrlRedirectCache";
    private static FileLruCache urlRedirectCache;

    UrlRedirectCache() {
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TAG);
        stringBuilder.append("_Redirect");
        REDIRECT_CONTENT_TAG = stringBuilder.toString();
    }

    static synchronized FileLruCache getCache() throws IOException {
        FileLruCache fileLruCache;
        synchronized (UrlRedirectCache.class) {
            if (urlRedirectCache == null) {
                urlRedirectCache = new FileLruCache(TAG, new Limits());
            }
            fileLruCache = urlRedirectCache;
        }
        return fileLruCache;
    }

    static Uri getRedirectedUri(Uri uri) {
        Throwable th;
        if (uri == null) {
            return null;
        }
        String uri2 = uri.toString();
        Closeable inputStreamReader;
        try {
            FileLruCache cache = getCache();
            Closeable closeable = null;
            Object obj = null;
            while (true) {
                try {
                    InputStream inputStream = cache.get(uri2, REDIRECT_CONTENT_TAG);
                    if (inputStream == null) {
                        break;
                    }
                    obj = 1;
                    inputStreamReader = new InputStreamReader(inputStream);
                    try {
                        char[] cArr = new char[128];
                        StringBuilder stringBuilder = new StringBuilder();
                        while (true) {
                            int read = inputStreamReader.read(cArr, 0, cArr.length);
                            if (read <= 0) {
                                break;
                            }
                            stringBuilder.append(cArr, 0, read);
                        }
                        Utility.closeQuietly(inputStreamReader);
                        closeable = inputStreamReader;
                        uri2 = stringBuilder.toString();
                    } catch (IOException unused) {
                        Utility.closeQuietly(inputStreamReader);
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        Utility.closeQuietly(inputStreamReader);
                        throw th;
                    }
                } catch (IOException unused2) {
                    inputStreamReader = closeable;
                    Utility.closeQuietly(inputStreamReader);
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    inputStreamReader = closeable;
                    Utility.closeQuietly(inputStreamReader);
                    throw th;
                }
            }
            if (obj != null) {
                uri = Uri.parse(uri2);
                Utility.closeQuietly(closeable);
                return uri;
            }
            Utility.closeQuietly(closeable);
            return null;
        } catch (IOException unused3) {
            inputStreamReader = null;
            Utility.closeQuietly(inputStreamReader);
            return null;
        } catch (Throwable th4) {
            th = th4;
            inputStreamReader = null;
            Utility.closeQuietly(inputStreamReader);
            throw th;
        }
    }

    static void cacheUriRedirect(Uri uri, Uri uri2) {
        Throwable th;
        if (uri != null && uri2 != null) {
            Closeable closeable = null;
            try {
                OutputStream openPutStream = getCache().openPutStream(uri.toString(), REDIRECT_CONTENT_TAG);
                try {
                    openPutStream.write(uri2.toString().getBytes());
                    Utility.closeQuietly(openPutStream);
                } catch (IOException unused) {
                    closeable = openPutStream;
                    Utility.closeQuietly(closeable);
                } catch (Throwable th2) {
                    th = th2;
                    closeable = openPutStream;
                    Utility.closeQuietly(closeable);
                    throw th;
                }
            } catch (IOException unused2) {
                Utility.closeQuietly(closeable);
            } catch (Throwable th3) {
                th = th3;
                Utility.closeQuietly(closeable);
                throw th;
            }
        }
    }

    static void clearCache() {
        try {
            getCache().clearCache();
        } catch (IOException e) {
            LoggingBehavior loggingBehavior = LoggingBehavior.CACHE;
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("clearCache failed ");
            stringBuilder.append(e.getMessage());
            Logger.log(loggingBehavior, 5, str, stringBuilder.toString());
        }
    }
}
