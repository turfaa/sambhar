package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace.Builder;
import com.twitter.sdk.android.core.internal.scribe.SyndicationClientEvent;

final class ScribeConstants {
    static final String SCRIBE_FILTER_ACTION = "filter";
    static final String SCRIBE_IMPRESSION_ACTION = "impression";
    static final String SCRIBE_INITIAL_COMPONENT = "initial";
    static final String SCRIBE_INITIAL_ELEMENT = "initial";
    static final String SCRIBE_TIMELINE_PAGE = "timeline";
    static final String SCRIBE_TIMELINE_SECTION = "timeline";
    static final String SYNDICATED_SDK_IMPRESSION_ELEMENT = "";
    static final String TFW_CLIENT_EVENT_PAGE = "android";

    private ScribeConstants() {
    }

    static EventNamespace getSyndicatedSdkTimelineNamespace(String str) {
        return new Builder().setClient("android").setPage("timeline").setSection(str).setComponent("initial").setElement("").setAction(SCRIBE_IMPRESSION_ACTION).builder();
    }

    static EventNamespace getTfwClientTimelineNamespace(String str) {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("timeline").setComponent(str).setElement("initial").setAction(SCRIBE_IMPRESSION_ACTION).builder();
    }

    static EventNamespace getTfwClientFilterTimelineNamespace(String str) {
        return new Builder().setClient(SyndicationClientEvent.CLIENT_NAME).setPage("android").setSection("timeline").setComponent(str).setElement("initial").setAction(SCRIBE_FILTER_ACTION).builder();
    }
}
