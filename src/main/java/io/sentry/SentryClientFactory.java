package io.sentry;

import io.sentry.config.Lookup;
import io.sentry.dsn.Dsn;
import io.sentry.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SentryClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(SentryClientFactory.class);

    public abstract SentryClient createSentryClient(Dsn dsn);

    public static SentryClient sentryClient() {
        return sentryClient(null, null);
    }

    public static SentryClient sentryClient(String str) {
        return sentryClient(str, null);
    }

    public static SentryClient sentryClient(String str, SentryClientFactory sentryClientFactory) {
        Dsn resolveDsn = resolveDsn(str);
        if (sentryClientFactory == null) {
            String lookup = Lookup.lookup("factory", resolveDsn);
            if (Util.isNullOrEmpty(lookup)) {
                sentryClientFactory = new DefaultSentryClientFactory();
            } else {
                try {
                    sentryClientFactory = (SentryClientFactory) Class.forName(lookup).newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    Logger logger = logger;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error creating SentryClient using factory class: '");
                    stringBuilder.append(lookup);
                    stringBuilder.append("'.");
                    logger.error(stringBuilder.toString(), e);
                    return null;
                }
            }
        }
        return sentryClientFactory.createSentryClient(resolveDsn);
    }

    private static Dsn resolveDsn(String str) {
        try {
            if (Util.isNullOrEmpty(str)) {
                str = Dsn.dsnLookup();
            }
            return new Dsn(str);
        } catch (Exception e) {
            logger.error("Error creating valid DSN from: '{}'.", (Object) str, e);
            throw e;
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SentryClientFactory{name='");
        stringBuilder.append(getClass().getName());
        stringBuilder.append('\'');
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
