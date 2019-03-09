package io.sentry.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Lookup {
    private static final String CONFIG_FILE_NAME = "sentry.properties";
    private static boolean checkJndi = true;
    private static Properties configProps;
    private static final Logger logger = LoggerFactory.getLogger(Lookup.class);

    static {
        Object configFilePath = getConfigFilePath();
        try {
            InputStream inputStream = getInputStream(configFilePath);
            if (inputStream != null) {
                configProps = new Properties();
                configProps.load(inputStream);
                return;
            }
            logger.debug("Sentry configuration file not found in filesystem or classpath: '{}'.", configFilePath);
        } catch (Exception e) {
            logger.error("Error loading Sentry configuration file '{}': ", configFilePath, e);
        }
    }

    private Lookup() {
    }

    private static String getConfigFilePath() {
        String property = System.getProperty("sentry.properties.file");
        if (property == null) {
            property = System.getenv("SENTRY_PROPERTIES_FILE");
        }
        return property == null ? CONFIG_FILE_NAME : property;
    }

    private static InputStream getInputStream(String str) throws FileNotFoundException {
        File file = new File(str);
        if (file.isFile() && file.canRead()) {
            return new FileInputStream(file);
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(str);
    }

    public static String lookup(String str) {
        return lookup(str, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0067  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d2 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00cd  */
    public static java.lang.String lookup(java.lang.String r7, io.sentry.dsn.Dsn r8) {
        /*
        r0 = checkJndi;
        r1 = 0;
        r2 = 0;
        if (r0 == 0) goto L_0x0040;
    L_0x0006:
        r0 = "javax.naming.InitialContext";
        r3 = io.sentry.dsn.Dsn.class;
        r3 = r3.getClassLoader();	 Catch:{ ClassNotFoundException | NoClassDefFoundError -> 0x0021, ClassNotFoundException | NoClassDefFoundError -> 0x0021 }
        java.lang.Class.forName(r0, r2, r3);	 Catch:{ ClassNotFoundException | NoClassDefFoundError -> 0x0021, ClassNotFoundException | NoClassDefFoundError -> 0x0021 }
        r0 = io.sentry.config.JndiLookup.jndiLookup(r7);	 Catch:{ ClassNotFoundException | NoClassDefFoundError -> 0x0021, ClassNotFoundException | NoClassDefFoundError -> 0x0021 }
        if (r0 == 0) goto L_0x0041;
    L_0x0017:
        r3 = logger;	 Catch:{ ClassNotFoundException | NoClassDefFoundError -> 0x001f, ClassNotFoundException | NoClassDefFoundError -> 0x001f }
        r4 = "Found {}={} in JNDI.";
        r3.debug(r4, r7, r0);	 Catch:{ ClassNotFoundException | NoClassDefFoundError -> 0x001f, ClassNotFoundException | NoClassDefFoundError -> 0x001f }
        goto L_0x0041;
    L_0x001f:
        r3 = move-exception;
        goto L_0x0023;
    L_0x0021:
        r3 = move-exception;
        r0 = r1;
    L_0x0023:
        r4 = logger;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "JNDI is not available: ";
        r5.append(r6);
        r3 = r3.getMessage();
        r5.append(r3);
        r3 = r5.toString();
        r4.trace(r3);
        checkJndi = r2;
        goto L_0x0041;
    L_0x0040:
        r0 = r1;
    L_0x0041:
        if (r0 != 0) goto L_0x0065;
    L_0x0043:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r3 = "sentry.";
        r0.append(r3);
        r3 = r7.toLowerCase();
        r0.append(r3);
        r0 = r0.toString();
        r0 = java.lang.System.getProperty(r0);
        if (r0 == 0) goto L_0x0065;
    L_0x005e:
        r3 = logger;
        r4 = "Found {}={} in Java System Properties.";
        r3.debug(r4, r7, r0);
    L_0x0065:
        if (r0 != 0) goto L_0x0091;
    L_0x0067:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r3 = "SENTRY_";
        r0.append(r3);
        r3 = ".";
        r4 = "_";
        r3 = r7.replace(r3, r4);
        r3 = r3.toUpperCase();
        r0.append(r3);
        r0 = r0.toString();
        r0 = java.lang.System.getenv(r0);
        if (r0 == 0) goto L_0x0091;
    L_0x008a:
        r3 = logger;
        r4 = "Found {}={} in System Environment Variables.";
        r3.debug(r4, r7, r0);
    L_0x0091:
        if (r0 != 0) goto L_0x00a9;
    L_0x0093:
        if (r8 == 0) goto L_0x00a9;
    L_0x0095:
        r8 = r8.getOptions();
        r8 = r8.get(r7);
        r0 = r8;
        r0 = (java.lang.String) r0;
        if (r0 == 0) goto L_0x00a9;
    L_0x00a2:
        r8 = logger;
        r3 = "Found {}={} in DSN.";
        r8.debug(r3, r7, r0);
    L_0x00a9:
        if (r0 != 0) goto L_0x00cb;
    L_0x00ab:
        r8 = configProps;
        if (r8 == 0) goto L_0x00cb;
    L_0x00af:
        r8 = configProps;
        r0 = r8.getProperty(r7);
        if (r0 == 0) goto L_0x00cb;
    L_0x00b7:
        r8 = logger;
        r3 = "Found {}={} in {}.";
        r4 = 3;
        r4 = new java.lang.Object[r4];
        r4[r2] = r7;
        r7 = 1;
        r4[r7] = r0;
        r7 = 2;
        r2 = "sentry.properties";
        r4[r7] = r2;
        r8.debug(r3, r4);
    L_0x00cb:
        if (r0 == 0) goto L_0x00d2;
    L_0x00cd:
        r7 = r0.trim();
        return r7;
    L_0x00d2:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.config.Lookup.lookup(java.lang.String, io.sentry.dsn.Dsn):java.lang.String");
    }
}
