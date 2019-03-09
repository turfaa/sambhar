package com.twitter.sdk.android.core.internal.scribe;

public interface EventsStrategy<T> extends FileRollOverManager, EventsManager<T> {
    FilesSender getFilesSender();
}
