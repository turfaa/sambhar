package io.sentry.environment;

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SentryEnvironment {
    public static final String SDK_NAME = "sentry-java";
    public static final String SDK_VERSION = "1.7.16-9b60b";
    protected static final ThreadLocal<AtomicInteger> SENTRY_THREAD = new ThreadLocal<AtomicInteger>() {
        /* Access modifiers changed, original: protected */
        public AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    private static final Logger logger = LoggerFactory.getLogger(SentryEnvironment.class);

    public static String getSentryName() {
        return "sentry-java/1.7.16-9b60b";
    }

    private SentryEnvironment() {
    }

    public static void startManagingThread() {
        try {
            if (isManagingThread()) {
                logger.warn("Thread already managed by Sentry");
            }
            ((AtomicInteger) SENTRY_THREAD.get()).incrementAndGet();
        } catch (Throwable th) {
            ((AtomicInteger) SENTRY_THREAD.get()).incrementAndGet();
        }
    }

    public static void stopManagingThread() {
        try {
            if (!isManagingThread()) {
                startManagingThread();
                logger.warn("Thread not yet managed by Sentry");
            }
            if (((AtomicInteger) SENTRY_THREAD.get()).decrementAndGet() == 0) {
                SENTRY_THREAD.remove();
            }
        } catch (Throwable th) {
            if (((AtomicInteger) SENTRY_THREAD.get()).decrementAndGet() == 0) {
                SENTRY_THREAD.remove();
            }
        }
    }

    public static boolean isManagingThread() {
        return ((AtomicInteger) SENTRY_THREAD.get()).get() > 0;
    }
}
