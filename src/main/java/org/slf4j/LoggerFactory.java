package org.slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.helpers.SubstituteLoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticLoggerBinder;

public final class LoggerFactory {
    private static final String[] API_COMPATIBILITY_LIST = new String[]{"1.6", "1.7"};
    static final String CODES_PREFIX = "http://www.slf4j.org/codes.html";
    static boolean DETECT_LOGGER_NAME_MISMATCH = Util.safeGetBooleanSystemProperty(DETECT_LOGGER_NAME_MISMATCH_PROPERTY);
    static final String DETECT_LOGGER_NAME_MISMATCH_PROPERTY = "slf4j.detectLoggerNameMismatch";
    static final int FAILED_INITIALIZATION = 2;
    static volatile int INITIALIZATION_STATE = 0;
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";
    static final String LOGGER_NAME_MISMATCH_URL = "http://www.slf4j.org/codes.html#loggerNameMismatch";
    static final String MULTIPLE_BINDINGS_URL = "http://www.slf4j.org/codes.html#multiple_bindings";
    static final NOPLoggerFactory NOP_FALLBACK_FACTORY = new NOPLoggerFactory();
    static final int NOP_FALLBACK_INITIALIZATION = 4;
    static final String NO_STATICLOGGERBINDER_URL = "http://www.slf4j.org/codes.html#StaticLoggerBinder";
    static final String NULL_LF_URL = "http://www.slf4j.org/codes.html#null_LF";
    static final int ONGOING_INITIALIZATION = 1;
    static final String REPLAY_URL = "http://www.slf4j.org/codes.html#replay";
    private static String STATIC_LOGGER_BINDER_PATH = "org/slf4j/impl/StaticLoggerBinder.class";
    static final String SUBSTITUTE_LOGGER_URL = "http://www.slf4j.org/codes.html#substituteLogger";
    static final SubstituteLoggerFactory SUBST_FACTORY = new SubstituteLoggerFactory();
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int UNINITIALIZED = 0;
    static final String UNSUCCESSFUL_INIT_MSG = "org.slf4j.LoggerFactory in failed state. Original exception was thrown EARLIER. See also http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final String UNSUCCESSFUL_INIT_URL = "http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final String VERSION_MISMATCH = "http://www.slf4j.org/codes.html#version_mismatch";

    private LoggerFactory() {
    }

    static void reset() {
        INITIALIZATION_STATE = 0;
    }

    private static final void performInitialization() {
        bind();
        if (INITIALIZATION_STATE == 3) {
            versionSanityCheck();
        }
    }

    private static boolean messageContainsOrgSlf4jImplStaticLoggerBinder(String str) {
        if (str == null) {
            return false;
        }
        return str.contains("org/slf4j/impl/StaticLoggerBinder") || str.contains("org.slf4j.impl.StaticLoggerBinder");
    }

    private static final void bind() {
        Set set = null;
        try {
            if (!isAndroid()) {
                set = findPossibleStaticLoggerBinderPathSet();
                reportMultipleBindingAmbiguity(set);
            }
            StaticLoggerBinder.getSingleton();
            INITIALIZATION_STATE = 3;
            reportActualBinding(set);
            fixSubstituteLoggers();
            replayEvents();
            SUBST_FACTORY.clear();
        } catch (NoClassDefFoundError e) {
            if (messageContainsOrgSlf4jImplStaticLoggerBinder(e.getMessage())) {
                INITIALIZATION_STATE = 4;
                Util.report("Failed to load class \"org.slf4j.impl.StaticLoggerBinder\".");
                Util.report("Defaulting to no-operation (NOP) logger implementation");
                Util.report("See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.");
                return;
            }
            failedBinding(e);
            throw e;
        } catch (NoSuchMethodError e2) {
            String message = e2.getMessage();
            if (message != null && message.contains("org.slf4j.impl.StaticLoggerBinder.getSingleton()")) {
                INITIALIZATION_STATE = 2;
                Util.report("slf4j-api 1.6.x (or later) is incompatible with this binding.");
                Util.report("Your binding is version 1.5.5 or earlier.");
                Util.report("Upgrade your binding to version 1.6.x.");
            }
            throw e2;
        } catch (Exception e3) {
            failedBinding(e3);
            throw new IllegalStateException("Unexpected initialization failure", e3);
        }
    }

    private static void fixSubstituteLoggers() {
        synchronized (SUBST_FACTORY) {
            SUBST_FACTORY.postInitialization();
            for (SubstituteLogger substituteLogger : SUBST_FACTORY.getLoggers()) {
                substituteLogger.setDelegate(getLogger(substituteLogger.getName()));
            }
        }
    }

    static void failedBinding(Throwable th) {
        INITIALIZATION_STATE = 2;
        Util.report("Failed to instantiate SLF4J LoggerFactory", th);
    }

    private static void replayEvents() {
        LinkedBlockingQueue eventQueue = SUBST_FACTORY.getEventQueue();
        int size = eventQueue.size();
        ArrayList<SubstituteLoggingEvent> arrayList = new ArrayList(128);
        int i = 0;
        while (eventQueue.drainTo(arrayList, 128) != 0) {
            for (SubstituteLoggingEvent substituteLoggingEvent : arrayList) {
                replaySingleEvent(substituteLoggingEvent);
                int i2 = i + 1;
                if (i == 0) {
                    emitReplayOrSubstituionWarning(substituteLoggingEvent, size);
                }
                i = i2;
            }
            arrayList.clear();
        }
    }

    private static void emitReplayOrSubstituionWarning(SubstituteLoggingEvent substituteLoggingEvent, int i) {
        if (substituteLoggingEvent.getLogger().isDelegateEventAware()) {
            emitReplayWarning(i);
        } else if (!substituteLoggingEvent.getLogger().isDelegateNOP()) {
            emitSubstitutionWarning();
        }
    }

    private static void replaySingleEvent(SubstituteLoggingEvent substituteLoggingEvent) {
        if (substituteLoggingEvent != null) {
            SubstituteLogger logger = substituteLoggingEvent.getLogger();
            String name = logger.getName();
            if (logger.isDelegateNull()) {
                throw new IllegalStateException("Delegate logger cannot be null at this state.");
            }
            if (!logger.isDelegateNOP()) {
                if (logger.isDelegateEventAware()) {
                    logger.log(substituteLoggingEvent);
                } else {
                    Util.report(name);
                }
            }
        }
    }

    private static void emitSubstitutionWarning() {
        Util.report("The following set of substitute loggers may have been accessed");
        Util.report("during the initialization phase. Logging calls during this");
        Util.report("phase were not honored. However, subsequent logging calls to these");
        Util.report("loggers will work as normally expected.");
        Util.report("See also http://www.slf4j.org/codes.html#substituteLogger");
    }

    private static void emitReplayWarning(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("A number (");
        stringBuilder.append(i);
        stringBuilder.append(") of logging calls during the initialization phase have been intercepted and are");
        Util.report(stringBuilder.toString());
        Util.report("now being replayed. These are subject to the filtering rules of the underlying logging system.");
        Util.report("See also http://www.slf4j.org/codes.html#replay");
    }

    private static final void versionSanityCheck() {
        try {
            String str = StaticLoggerBinder.REQUESTED_API_VERSION;
            Object obj = null;
            for (String startsWith : API_COMPATIBILITY_LIST) {
                if (str.startsWith(startsWith)) {
                    obj = 1;
                }
            }
            if (obj == null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("The requested version ");
                stringBuilder.append(str);
                stringBuilder.append(" by your slf4j binding is not compatible with ");
                stringBuilder.append(Arrays.asList(API_COMPATIBILITY_LIST).toString());
                Util.report(stringBuilder.toString());
                Util.report("See http://www.slf4j.org/codes.html#version_mismatch for further details.");
            }
        } catch (NoSuchFieldError unused) {
        } catch (Throwable th) {
            Util.report("Unexpected problem occured during version sanity check", th);
        }
    }

    static Set<URL> findPossibleStaticLoggerBinderPathSet() {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        try {
            Enumeration systemResources;
            ClassLoader classLoader = LoggerFactory.class.getClassLoader();
            if (classLoader == null) {
                systemResources = ClassLoader.getSystemResources(STATIC_LOGGER_BINDER_PATH);
            } else {
                systemResources = classLoader.getResources(STATIC_LOGGER_BINDER_PATH);
            }
            while (systemResources.hasMoreElements()) {
                linkedHashSet.add((URL) systemResources.nextElement());
            }
        } catch (IOException e) {
            Util.report("Error getting resources from path", e);
        }
        return linkedHashSet;
    }

    private static boolean isAmbiguousStaticLoggerBinderPathSet(Set<URL> set) {
        return set.size() > 1;
    }

    private static void reportMultipleBindingAmbiguity(Set<URL> set) {
        if (isAmbiguousStaticLoggerBinderPathSet(set)) {
            Util.report("Class path contains multiple SLF4J bindings.");
            for (URL url : set) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Found binding in [");
                stringBuilder.append(url);
                stringBuilder.append("]");
                Util.report(stringBuilder.toString());
            }
            Util.report("See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.");
        }
    }

    private static boolean isAndroid() {
        String safeGetSystemProperty = Util.safeGetSystemProperty(JAVA_VENDOR_PROPERTY);
        if (safeGetSystemProperty == null) {
            return false;
        }
        return safeGetSystemProperty.toLowerCase().contains("android");
    }

    private static void reportActualBinding(Set<URL> set) {
        if (set != null && isAmbiguousStaticLoggerBinderPathSet(set)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Actual binding is of type [");
            stringBuilder.append(StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr());
            stringBuilder.append("]");
            Util.report(stringBuilder.toString());
        }
    }

    public static Logger getLogger(String str) {
        return getILoggerFactory().getLogger(str);
    }

    public static Logger getLogger(Class<?> cls) {
        Logger logger = getLogger(cls.getName());
        if (DETECT_LOGGER_NAME_MISMATCH) {
            Class callingClass = Util.getCallingClass();
            if (callingClass != null && nonMatchingClasses(cls, callingClass)) {
                Util.report(String.format("Detected logger name mismatch. Given name: \"%s\"; computed name: \"%s\".", new Object[]{logger.getName(), callingClass.getName()}));
                Util.report("See http://www.slf4j.org/codes.html#loggerNameMismatch for an explanation");
            }
        }
        return logger;
    }

    private static boolean nonMatchingClasses(Class<?> cls, Class<?> cls2) {
        return cls2.isAssignableFrom(cls) ^ 1;
    }

    public static ILoggerFactory getILoggerFactory() {
        if (INITIALIZATION_STATE == 0) {
            synchronized (LoggerFactory.class) {
                if (INITIALIZATION_STATE == 0) {
                    INITIALIZATION_STATE = 1;
                    performInitialization();
                }
            }
        }
        switch (INITIALIZATION_STATE) {
            case 1:
                return SUBST_FACTORY;
            case 2:
                throw new IllegalStateException(UNSUCCESSFUL_INIT_MSG);
            case 3:
                return StaticLoggerBinder.getSingleton().getLoggerFactory();
            case 4:
                return NOP_FALLBACK_FACTORY;
            default:
                throw new IllegalStateException("Unreachable code");
        }
    }
}
