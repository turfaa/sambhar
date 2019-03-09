package io.sentry.event.interfaces;

import io.sentry.jvmti.FrameCache;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

public final class SentryException implements Serializable {
    public static final String DEFAULT_PACKAGE_NAME = "(default)";
    private final String exceptionClassName;
    private final String exceptionMessage;
    private final String exceptionPackageName;
    private final StackTraceInterface stackTraceInterface;

    public SentryException(Throwable th, StackTraceElement[] stackTraceElementArr) {
        this.exceptionMessage = th.getMessage();
        this.exceptionClassName = th.getClass().getSimpleName();
        Package packageR = th.getClass().getPackage();
        this.exceptionPackageName = packageR != null ? packageR.getName() : null;
        this.stackTraceInterface = new StackTraceInterface(th.getStackTrace(), stackTraceElementArr, FrameCache.get(th));
    }

    public SentryException(String str, String str2, String str3, StackTraceInterface stackTraceInterface) {
        this.exceptionMessage = str;
        this.exceptionClassName = str2;
        this.exceptionPackageName = str3;
        this.stackTraceInterface = stackTraceInterface;
    }

    public static Deque<SentryException> extractExceptionQueue(Throwable th) {
        ArrayDeque arrayDeque = new ArrayDeque();
        HashSet hashSet = new HashSet();
        StackTraceElement[] stackTraceElementArr = new StackTraceElement[0];
        Object th2;
        while (th2 != null && hashSet.add(th2)) {
            arrayDeque.add(new SentryException(th2, stackTraceElementArr));
            stackTraceElementArr = th2.getStackTrace();
            th2 = th2.getCause();
        }
        return arrayDeque;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public String getExceptionClassName() {
        return this.exceptionClassName;
    }

    public String getExceptionPackageName() {
        return this.exceptionPackageName != null ? this.exceptionPackageName : DEFAULT_PACKAGE_NAME;
    }

    public StackTraceInterface getStackTraceInterface() {
        return this.stackTraceInterface;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SentryException{exceptionMessage='");
        stringBuilder.append(this.exceptionMessage);
        stringBuilder.append('\'');
        stringBuilder.append(", exceptionClassName='");
        stringBuilder.append(this.exceptionClassName);
        stringBuilder.append('\'');
        stringBuilder.append(", exceptionPackageName='");
        stringBuilder.append(this.exceptionPackageName);
        stringBuilder.append('\'');
        stringBuilder.append(", stackTraceInterface=");
        stringBuilder.append(this.stackTraceInterface);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SentryException sentryException = (SentryException) obj;
        if (!this.exceptionClassName.equals(sentryException.exceptionClassName)) {
            return false;
        }
        if (!this.exceptionMessage == null ? this.exceptionMessage.equals(sentryException.exceptionMessage) : sentryException.exceptionMessage == null) {
            return false;
        }
        if (this.exceptionPackageName == null ? sentryException.exceptionPackageName == null : this.exceptionPackageName.equals(sentryException.exceptionPackageName)) {
            return this.stackTraceInterface.equals(sentryException.stackTraceInterface);
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((this.exceptionMessage != null ? this.exceptionMessage.hashCode() : 0) * 31) + this.exceptionClassName.hashCode()) * 31;
        if (this.exceptionPackageName != null) {
            i = this.exceptionPackageName.hashCode();
        }
        return hashCode + i;
    }
}
