package com.twitter.sdk.android.core.internal.scribe;

import java.util.Collections;
import java.util.List;

public class ScribeEventFactory {
    public static ScribeEvent newScribeEvent(EventNamespace eventNamespace, long j, String str, String str2) {
        return newScribeEvent(eventNamespace, "", j, str, str2, Collections.emptyList());
    }

    public static ScribeEvent newScribeEvent(EventNamespace eventNamespace, String str, long j, String str2, String str3, List<ScribeItem> list) {
        String str4 = eventNamespace.client;
        Object obj = (str4.hashCode() == 114757 && str4.equals(SyndicationClientEvent.CLIENT_NAME)) ? null : -1;
        if (obj != null) {
            return new SyndicatedSdkImpressionEvent(eventNamespace, j, str2, str3, list);
        }
        return new SyndicationClientEvent(eventNamespace, str, j, str2, str3, list);
    }
}
