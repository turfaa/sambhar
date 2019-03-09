package com.twitter.sdk.android.core.internal;

import com.twitter.sdk.android.core.Twitter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ExecutorUtils {
    private static final int CORE_POOL_SIZE = (CPU_COUNT + 1);
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final long DEFAULT_TERMINATION_TIMEOUT = 1;
    private static final long KEEP_ALIVE = 1;
    private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);

    private ExecutorUtils() {
    }

    public static ExecutorService buildThreadPoolExecutorService(String str) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(), getNamedThreadFactory(str));
        addDelayedShutdownHook(str, threadPoolExecutor);
        return threadPoolExecutor;
    }

    public static ScheduledExecutorService buildSingleThreadScheduledExecutorService(String str) {
        ScheduledExecutorService newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor(getNamedThreadFactory(str));
        addDelayedShutdownHook(str, newSingleThreadScheduledExecutor);
        return newSingleThreadScheduledExecutor;
    }

    static ThreadFactory getNamedThreadFactory(final String str) {
        final AtomicLong atomicLong = new AtomicLong(1);
        return new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread newThread = Executors.defaultThreadFactory().newThread(runnable);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append(atomicLong.getAndIncrement());
                newThread.setName(stringBuilder.toString());
                return newThread;
            }
        };
    }

    static void addDelayedShutdownHook(String str, ExecutorService executorService) {
        addDelayedShutdownHook(str, executorService, 1, TimeUnit.SECONDS);
    }

    static void addDelayedShutdownHook(String str, ExecutorService executorService, long j, TimeUnit timeUnit) {
        Runtime runtime = Runtime.getRuntime();
        final ExecutorService executorService2 = executorService;
        final long j2 = j;
        final TimeUnit timeUnit2 = timeUnit;
        final String str2 = str;
        AnonymousClass2 anonymousClass2 = new Runnable() {
            public void run() {
                try {
                    executorService2.shutdown();
                    if (!executorService2.awaitTermination(j2, timeUnit2)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(str2);
                        stringBuilder.append(" did not shutdown in the allocated time. Requesting immediate shutdown.");
                        Twitter.getLogger().d("Twitter", stringBuilder.toString());
                        executorService2.shutdownNow();
                    }
                } catch (InterruptedException unused) {
                    Twitter.getLogger().d("Twitter", String.format(Locale.US, "Interrupted while waiting for %s to shut down. Requesting immediate shutdown.", new Object[]{str2}));
                    executorService2.shutdownNow();
                }
            }
        };
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Twitter Shutdown Hook for ");
        stringBuilder.append(str);
        runtime.addShutdownHook(new Thread(anonymousClass2, stringBuilder.toString()));
    }
}
