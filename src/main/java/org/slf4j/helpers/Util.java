package org.slf4j.helpers;

import com.facebook.internal.ServerProtocol;
import java.io.PrintStream;

public final class Util {
    private static ClassContextSecurityManager SECURITY_MANAGER = null;
    private static boolean SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = false;

    private static final class ClassContextSecurityManager extends SecurityManager {
        private ClassContextSecurityManager() {
        }

        /* Access modifiers changed, original: protected */
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

    private Util() {
    }

    public static String safeGetSystemProperty(String str) {
        if (str != null) {
            try {
                return System.getProperty(str);
            } catch (SecurityException unused) {
                return null;
            }
        }
        throw new IllegalArgumentException("null input");
    }

    public static boolean safeGetBooleanSystemProperty(String str) {
        str = safeGetSystemProperty(str);
        if (str == null) {
            return false;
        }
        return str.equalsIgnoreCase(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
    }

    private static ClassContextSecurityManager getSecurityManager() {
        if (SECURITY_MANAGER != null) {
            return SECURITY_MANAGER;
        }
        if (SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED) {
            return null;
        }
        SECURITY_MANAGER = safeCreateSecurityManager();
        SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = true;
        return SECURITY_MANAGER;
    }

    private static ClassContextSecurityManager safeCreateSecurityManager() {
        try {
            return new ClassContextSecurityManager();
        } catch (SecurityException unused) {
            return null;
        }
    }

    public static Class<?> getCallingClass() {
        ClassContextSecurityManager securityManager = getSecurityManager();
        if (securityManager == null) {
            return null;
        }
        Class[] classContext = securityManager.getClassContext();
        String name = Util.class.getName();
        int i = 0;
        while (i < classContext.length && !name.equals(classContext[i].getName())) {
            i++;
        }
        if (i < classContext.length) {
            i += 2;
            if (i < classContext.length) {
                return classContext[i];
            }
        }
        throw new IllegalStateException("Failed to find org.slf4j.helpers.Util or its caller in the stack; this should not happen");
    }

    public static final void report(String str, Throwable th) {
        System.err.println(str);
        System.err.println("Reported exception:");
        th.printStackTrace();
    }

    public static final void report(String str) {
        PrintStream printStream = System.err;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SLF4J: ");
        stringBuilder.append(str);
        printStream.println(stringBuilder.toString());
    }
}
