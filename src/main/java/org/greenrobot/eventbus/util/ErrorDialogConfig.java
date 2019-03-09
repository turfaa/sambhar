package org.greenrobot.eventbus.util;

import android.content.res.Resources;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;

public class ErrorDialogConfig {
    int defaultDialogIconId;
    final int defaultErrorMsgId;
    Class<?> defaultEventTypeOnDialogClosed;
    final int defaultTitleId;
    EventBus eventBus;
    boolean logExceptions = true;
    final ExceptionToResourceMapping mapping;
    final Resources resources;
    String tagForLoggingExceptions;

    public ErrorDialogConfig(Resources resources, int i, int i2) {
        this.resources = resources;
        this.defaultTitleId = i;
        this.defaultErrorMsgId = i2;
        this.mapping = new ExceptionToResourceMapping();
    }

    public ErrorDialogConfig addMapping(Class<? extends Throwable> cls, int i) {
        this.mapping.addMapping(cls, i);
        return this;
    }

    public int getMessageIdForThrowable(Throwable th) {
        Integer mapThrowable = this.mapping.mapThrowable(th);
        if (mapThrowable != null) {
            return mapThrowable.intValue();
        }
        String str = EventBus.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No specific message ressource ID found for ");
        stringBuilder.append(th);
        Log.d(str, stringBuilder.toString());
        return this.defaultErrorMsgId;
    }

    public void setDefaultDialogIconId(int i) {
        this.defaultDialogIconId = i;
    }

    public void setDefaultEventTypeOnDialogClosed(Class<?> cls) {
        this.defaultEventTypeOnDialogClosed = cls;
    }

    public void disableExceptionLogging() {
        this.logExceptions = false;
    }

    public void setTagForLoggingExceptions(String str) {
        this.tagForLoggingExceptions = str;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /* Access modifiers changed, original: 0000 */
    public EventBus getEventBus() {
        return this.eventBus != null ? this.eventBus : EventBus.getDefault();
    }
}
