package io.sentry;

import io.sentry.event.Event.Level;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentryUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SentryClientFactory.class);
    private UncaughtExceptionHandler defaultExceptionHandler;
    private volatile Boolean enabled = Boolean.valueOf(true);

    public SentryUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.defaultExceptionHandler = uncaughtExceptionHandler;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        if (this.enabled.booleanValue()) {
            logger.trace("Uncaught exception received.");
            try {
                Sentry.capture(new EventBuilder().withMessage(th.getMessage()).withLevel(Level.FATAL).withSentryInterface(new ExceptionInterface(th)));
            } catch (Exception e) {
                logger.error("Error sending uncaught exception to Sentry.", e);
            }
        }
        if (this.defaultExceptionHandler != null) {
            this.defaultExceptionHandler.uncaughtException(thread, th);
        } else if (!(th instanceof ThreadDeath)) {
            PrintStream printStream = System.err;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception in thread \"");
            stringBuilder.append(thread.getName());
            stringBuilder.append("\" ");
            printStream.print(stringBuilder.toString());
            th.printStackTrace(System.err);
        }
    }

    public static SentryUncaughtExceptionHandler setup() {
        logger.debug("Configuring uncaught exception handler.");
        UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null) {
            Logger logger = logger;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("default UncaughtExceptionHandler class='");
            stringBuilder.append(defaultUncaughtExceptionHandler.getClass().getName());
            stringBuilder.append("'");
            logger.debug(stringBuilder.toString());
        }
        SentryUncaughtExceptionHandler sentryUncaughtExceptionHandler = new SentryUncaughtExceptionHandler(defaultUncaughtExceptionHandler);
        Thread.setDefaultUncaughtExceptionHandler(sentryUncaughtExceptionHandler);
        return sentryUncaughtExceptionHandler;
    }

    public void disable() {
        this.enabled = Boolean.valueOf(false);
        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            Thread.setDefaultUncaughtExceptionHandler(this.defaultExceptionHandler);
        }
    }

    public Boolean isEnabled() {
        return this.enabled;
    }
}
