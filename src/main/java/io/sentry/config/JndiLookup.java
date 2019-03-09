package io.sentry.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JndiLookup {
    private static final String JNDI_PREFIX = "java:comp/env/sentry/";
    private static final Logger logger = LoggerFactory.getLogger(JndiLookup.class);

    private JndiLookup() {
    }

    public static String jndiLookup(String str) {
        StringBuilder stringBuilder;
        try {
            InitialContext initialContext = new InitialContext();
            stringBuilder = new StringBuilder();
            stringBuilder.append(JNDI_PREFIX);
            stringBuilder.append(str);
            return (String) initialContext.lookup(stringBuilder.toString());
        } catch (NoInitialContextException unused) {
            logger.trace("JNDI not configured for Sentry (NoInitialContextEx)");
        } catch (NamingException unused2) {
            Logger logger = logger;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No /sentry/");
            stringBuilder.append(str);
            stringBuilder.append(" in JNDI");
            logger.trace(stringBuilder.toString());
        } catch (RuntimeException e) {
            logger.warn("Odd RuntimeException while testing for JNDI", e);
        }
        return null;
    }
}
