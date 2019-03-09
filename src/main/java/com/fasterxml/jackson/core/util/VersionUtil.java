package com.fasterxml.jackson.core.util;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Version;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class VersionUtil {
    private static final Pattern V_SEP = Pattern.compile("[-_./;:]");
    private final Version _v;

    protected VersionUtil() {
        Version versionFor;
        try {
            versionFor = versionFor(getClass());
        } catch (Exception unused) {
            PrintStream printStream = System.err;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ERROR: Failed to load Version information from ");
            stringBuilder.append(getClass());
            printStream.println(stringBuilder.toString());
            versionFor = null;
        }
        if (versionFor == null) {
            versionFor = Version.unknownVersion();
        }
        this._v = versionFor;
    }

    public Version version() {
        return this._v;
    }

    public static Version versionFor(Class<?> cls) {
        Version packageVersionFor = packageVersionFor(cls);
        return packageVersionFor == null ? Version.unknownVersion() : packageVersionFor;
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x002d */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public static com.fasterxml.jackson.core.Version packageVersionFor(java.lang.Class<?> r3) {
        /*
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0044 }
        r0.<init>();	 Catch:{ Exception -> 0x0044 }
        r1 = r3.getPackage();	 Catch:{ Exception -> 0x0044 }
        r1 = r1.getName();	 Catch:{ Exception -> 0x0044 }
        r0.append(r1);	 Catch:{ Exception -> 0x0044 }
        r1 = ".PackageVersion";
        r0.append(r1);	 Catch:{ Exception -> 0x0044 }
        r0 = r0.toString();	 Catch:{ Exception -> 0x0044 }
        r1 = 1;
        r3 = r3.getClassLoader();	 Catch:{ Exception -> 0x0044 }
        r3 = java.lang.Class.forName(r0, r1, r3);	 Catch:{ Exception -> 0x0044 }
        r0 = r3.newInstance();	 Catch:{ Exception -> 0x002d }
        r0 = (com.fasterxml.jackson.core.Versioned) r0;	 Catch:{ Exception -> 0x002d }
        r0 = r0.version();	 Catch:{ Exception -> 0x002d }
        goto L_0x0045;
    L_0x002d:
        r0 = new java.lang.IllegalArgumentException;	 Catch:{ Exception -> 0x0044 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0044 }
        r1.<init>();	 Catch:{ Exception -> 0x0044 }
        r2 = "Failed to get Versioned out of ";
        r1.append(r2);	 Catch:{ Exception -> 0x0044 }
        r1.append(r3);	 Catch:{ Exception -> 0x0044 }
        r3 = r1.toString();	 Catch:{ Exception -> 0x0044 }
        r0.<init>(r3);	 Catch:{ Exception -> 0x0044 }
        throw r0;	 Catch:{ Exception -> 0x0044 }
    L_0x0044:
        r0 = 0;
    L_0x0045:
        if (r0 != 0) goto L_0x004b;
    L_0x0047:
        r0 = com.fasterxml.jackson.core.Version.unknownVersion();
    L_0x004b:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.util.VersionUtil.packageVersionFor(java.lang.Class):com.fasterxml.jackson.core.Version");
    }

    @Deprecated
    public static Version mavenVersionFor(ClassLoader classLoader, String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("META-INF/maven/");
        stringBuilder.append(str.replaceAll("\\.", "/"));
        stringBuilder.append("/");
        stringBuilder.append(str2);
        stringBuilder.append("/pom.properties");
        Version stringBuilder2 = stringBuilder.toString();
        InputStream resourceAsStream = classLoader.getResourceAsStream(stringBuilder2);
        if (resourceAsStream != null) {
            try {
                Properties properties = new Properties();
                properties.load(resourceAsStream);
                stringBuilder2 = parseVersion(properties.getProperty(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION), properties.getProperty("groupId"), properties.getProperty("artifactId"));
                return stringBuilder2;
            } catch (IOException unused) {
            } finally {
                _close(resourceAsStream);
            }
        }
        return Version.unknownVersion();
    }

    public static Version parseVersion(String str, String str2, String str3) {
        if (str != null) {
            str = str.trim();
            if (str.length() > 0) {
                String[] split = V_SEP.split(str);
                return new Version(parseVersionPart(split[0]), split.length > 1 ? parseVersionPart(split[1]) : 0, split.length > 2 ? parseVersionPart(split[2]) : 0, split.length > 3 ? split[3] : null, str2, str3);
            }
        }
        return Version.unknownVersion();
    }

    protected static int parseVersionPart(String str) {
        int length = str.length();
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (charAt > '9' || charAt < '0') {
                break;
            }
            i = (i * 10) + (charAt - 48);
        }
        return i;
    }

    private static final void _close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException unused) {
        }
    }

    public static final void throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}
