package com.squareup.picasso;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StatFs;
import android.provider.Settings.System;
import android.util.Log;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadFactory;

final class Utils {
    static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 15000;
    static final int DEFAULT_READ_TIMEOUT_MILLIS = 20000;
    static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 20000;
    private static final int KEY_PADDING = 50;
    static final char KEY_SEPARATOR = '\n';
    static final StringBuilder MAIN_THREAD_KEY_BUILDER = new StringBuilder();
    private static final int MAX_DISK_CACHE_SIZE = 52428800;
    private static final int MIN_DISK_CACHE_SIZE = 5242880;
    static final String OWNER_DISPATCHER = "Dispatcher";
    static final String OWNER_HUNTER = "Hunter";
    static final String OWNER_MAIN = "Main";
    private static final String PICASSO_CACHE = "picasso-cache";
    static final String THREAD_IDLE_NAME = "Picasso-Idle";
    static final int THREAD_LEAK_CLEANING_MS = 1000;
    static final String THREAD_PREFIX = "Picasso-";
    static final String VERB_BATCHED = "batched";
    static final String VERB_CANCELED = "canceled";
    static final String VERB_CHANGED = "changed";
    static final String VERB_COMPLETED = "completed";
    static final String VERB_CREATED = "created";
    static final String VERB_DECODED = "decoded";
    static final String VERB_DELIVERED = "delivered";
    static final String VERB_ENQUEUED = "enqueued";
    static final String VERB_ERRORED = "errored";
    static final String VERB_EXECUTING = "executing";
    static final String VERB_IGNORED = "ignored";
    static final String VERB_JOINED = "joined";
    static final String VERB_PAUSED = "paused";
    static final String VERB_REMOVED = "removed";
    static final String VERB_REPLAYING = "replaying";
    static final String VERB_RESUMED = "resumed";
    static final String VERB_RETRYING = "retrying";
    static final String VERB_TRANSFORMED = "transformed";
    private static final String WEBP_FILE_HEADER_RIFF = "RIFF";
    private static final int WEBP_FILE_HEADER_SIZE = 12;
    private static final String WEBP_FILE_HEADER_WEBP = "WEBP";

    @TargetApi(11)
    private static class ActivityManagerHoneycomb {
        private ActivityManagerHoneycomb() {
        }

        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    @TargetApi(12)
    private static class BitmapHoneycombMR1 {
        private BitmapHoneycombMR1() {
        }

        static int getByteCount(Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    }

    private static class OkHttpLoaderCreator {
        private OkHttpLoaderCreator() {
        }

        static Downloader create(Context context) {
            return new OkHttpDownloader(context);
        }
    }

    private static class PicassoThread extends Thread {
        public PicassoThread(Runnable runnable) {
            super(runnable);
        }

        public void run() {
            Process.setThreadPriority(10);
            super.run();
        }
    }

    static class PicassoThreadFactory implements ThreadFactory {
        PicassoThreadFactory() {
        }

        public Thread newThread(Runnable runnable) {
            return new PicassoThread(runnable);
        }
    }

    private Utils() {
    }

    static int getBitmapBytes(Bitmap bitmap) {
        int byteCount;
        if (VERSION.SDK_INT >= 12) {
            byteCount = BitmapHoneycombMR1.getByteCount(bitmap);
        } else {
            byteCount = bitmap.getRowBytes() * bitmap.getHeight();
        }
        if (byteCount >= 0) {
            return byteCount;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Negative size: ");
        stringBuilder.append(bitmap);
        throw new IllegalStateException(stringBuilder.toString());
    }

    static <T> T checkNotNull(T t, String str) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException(str);
    }

    static void checkNotMain() {
        if (isMain()) {
            throw new IllegalStateException("Method call should not happen from the main thread.");
        }
    }

    static void checkMain() {
        if (!isMain()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }
    }

    static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    static String getLogIdsForHunter(BitmapHunter bitmapHunter) {
        return getLogIdsForHunter(bitmapHunter, "");
    }

    static String getLogIdsForHunter(BitmapHunter bitmapHunter, String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        Action action = bitmapHunter.getAction();
        if (action != null) {
            stringBuilder.append(action.request.logId());
        }
        List actions = bitmapHunter.getActions();
        if (actions != null) {
            int size = actions.size();
            for (int i = 0; i < size; i++) {
                if (i > 0 || action != null) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(((Action) actions.get(i)).request.logId());
            }
        }
        return stringBuilder.toString();
    }

    static void log(String str, String str2, String str3) {
        log(str, str2, str3, "");
    }

    static void log(String str, String str2, String str3, String str4) {
        Log.d("Picasso", String.format("%1$-11s %2$-12s %3$s %4$s", new Object[]{str, str2, str3, str4}));
    }

    static String createKey(Request request) {
        String createKey = createKey(request, MAIN_THREAD_KEY_BUILDER);
        MAIN_THREAD_KEY_BUILDER.setLength(0);
        return createKey;
    }

    static String createKey(Request request, StringBuilder stringBuilder) {
        if (request.stableKey != null) {
            stringBuilder.ensureCapacity(request.stableKey.length() + 50);
            stringBuilder.append(request.stableKey);
        } else if (request.uri != null) {
            String uri = request.uri.toString();
            stringBuilder.ensureCapacity(uri.length() + 50);
            stringBuilder.append(uri);
        } else {
            stringBuilder.ensureCapacity(50);
            stringBuilder.append(request.resourceId);
        }
        stringBuilder.append(KEY_SEPARATOR);
        if (request.rotationDegrees != 0.0f) {
            stringBuilder.append("rotation:");
            stringBuilder.append(request.rotationDegrees);
            if (request.hasRotationPivot) {
                stringBuilder.append('@');
                stringBuilder.append(request.rotationPivotX);
                stringBuilder.append('x');
                stringBuilder.append(request.rotationPivotY);
            }
            stringBuilder.append(KEY_SEPARATOR);
        }
        if (request.hasSize()) {
            stringBuilder.append("resize:");
            stringBuilder.append(request.targetWidth);
            stringBuilder.append('x');
            stringBuilder.append(request.targetHeight);
            stringBuilder.append(KEY_SEPARATOR);
        }
        if (request.centerCrop) {
            stringBuilder.append("centerCrop");
            stringBuilder.append(KEY_SEPARATOR);
        } else if (request.centerInside) {
            stringBuilder.append("centerInside");
            stringBuilder.append(KEY_SEPARATOR);
        }
        if (request.transformations != null) {
            int size = request.transformations.size();
            for (int i = 0; i < size; i++) {
                stringBuilder.append(((Transformation) request.transformations.get(i)).key());
                stringBuilder.append(KEY_SEPARATOR);
            }
        }
        return stringBuilder.toString();
    }

    static void closeQuietly(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException unused) {
            }
        }
    }

    static boolean parseResponseSourceHeader(String str) {
        boolean z = false;
        if (str == null) {
            return false;
        }
        String[] split = str.split(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, 2);
        if ("CACHE".equals(split[0])) {
            return true;
        }
        if (split.length == 1) {
            return false;
        }
        try {
            if ("CONDITIONAL_CACHE".equals(split[0]) && Integer.parseInt(split[1]) == 304) {
                z = true;
            }
            return z;
        } catch (NumberFormatException unused) {
            return false;
        }
    }

    static Downloader createDefaultDownloader(Context context) {
        try {
            Class.forName("com.squareup.okhttp.OkHttpClient");
            return OkHttpLoaderCreator.create(context);
        } catch (ClassNotFoundException unused) {
            return new UrlConnectionDownloader(context);
        }
    }

    static File createDefaultCacheDir(Context context) {
        File file = new File(context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    static long calculateDiskCacheSize(File file) {
        long blockCount;
        try {
            StatFs statFs = new StatFs(file.getAbsolutePath());
            blockCount = (((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize())) / 50;
        } catch (IllegalArgumentException unused) {
            blockCount = 5242880;
        }
        return Math.max(Math.min(blockCount, 52428800), 5242880);
    }

    static int calculateMemoryCacheSize(Context context) {
        ActivityManager activityManager = (ActivityManager) getService(context, "activity");
        Object obj = (context.getApplicationInfo().flags & 1048576) != 0 ? 1 : null;
        int memoryClass = activityManager.getMemoryClass();
        if (obj != null && VERSION.SDK_INT >= 11) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(activityManager);
        }
        return (memoryClass * 1048576) / 7;
    }

    static boolean isAirplaneModeOn(Context context) {
        boolean z = false;
        try {
            if (System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    static <T> T getService(Context context, String str) {
        return context.getSystemService(str);
    }

    static boolean hasPermission(Context context, String str) {
        return context.checkCallingOrSelfPermission(str) == 0;
    }

    static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[4096];
        while (true) {
            int read = inputStream.read(bArr);
            if (-1 == read) {
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    static boolean isWebPFile(InputStream inputStream) throws IOException {
        byte[] bArr = new byte[12];
        if (inputStream.read(bArr, 0, 12) == 12 && WEBP_FILE_HEADER_RIFF.equals(new String(bArr, 0, 4, "US-ASCII")) && WEBP_FILE_HEADER_WEBP.equals(new String(bArr, 8, 4, "US-ASCII"))) {
            return true;
        }
        return false;
    }

    static int getResourceId(Resources resources, Request request) throws FileNotFoundException {
        if (request.resourceId != 0 || request.uri == null) {
            return request.resourceId;
        }
        String authority = request.uri.getAuthority();
        StringBuilder stringBuilder;
        if (authority != null) {
            List pathSegments = request.uri.getPathSegments();
            if (pathSegments == null || pathSegments.isEmpty()) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("No path segments: ");
                stringBuilder.append(request.uri);
                throw new FileNotFoundException(stringBuilder.toString());
            }
            int parseInt;
            if (pathSegments.size() == 1) {
                try {
                    parseInt = Integer.parseInt((String) pathSegments.get(0));
                } catch (NumberFormatException unused) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Last path segment is not a resource ID: ");
                    stringBuilder.append(request.uri);
                    throw new FileNotFoundException(stringBuilder.toString());
                }
            } else if (pathSegments.size() == 2) {
                parseInt = resources.getIdentifier((String) pathSegments.get(1), (String) pathSegments.get(0), authority);
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("More than two path segments: ");
                stringBuilder.append(request.uri);
                throw new FileNotFoundException(stringBuilder.toString());
            }
            return parseInt;
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("No package provided: ");
        stringBuilder.append(request.uri);
        throw new FileNotFoundException(stringBuilder.toString());
    }

    static Resources getResources(Context context, Request request) throws FileNotFoundException {
        StringBuilder stringBuilder;
        if (request.resourceId != 0 || request.uri == null) {
            return context.getResources();
        }
        String authority = request.uri.getAuthority();
        if (authority != null) {
            try {
                return context.getPackageManager().getResourcesForApplication(authority);
            } catch (NameNotFoundException unused) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to obtain resources for package: ");
                stringBuilder.append(request.uri);
                throw new FileNotFoundException(stringBuilder.toString());
            }
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("No package provided: ");
        stringBuilder.append(request.uri);
        throw new FileNotFoundException(stringBuilder.toString());
    }

    static void flushStackLocalLeaks(Looper looper) {
        AnonymousClass1 anonymousClass1 = new Handler(looper) {
            public void handleMessage(Message message) {
                sendMessageDelayed(obtainMessage(), 1000);
            }
        };
        anonymousClass1.sendMessageDelayed(anonymousClass1.obtainMessage(), 1000);
    }
}
