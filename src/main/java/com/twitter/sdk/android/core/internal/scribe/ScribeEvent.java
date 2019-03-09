package com.twitter.sdk.android.core.internal.scribe;

import android.text.TextUtils;
import com.bumptech.glide.load.Key;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ScribeEvent {
    private static final String CURRENT_FORMAT_VERSION = "2";
    @SerializedName("_category_")
    final String category;
    @SerializedName("event_namespace")
    final EventNamespace eventNamespace;
    @SerializedName("format_version")
    final String formatVersion;
    @SerializedName("items")
    final List<ScribeItem> items;
    @SerializedName("ts")
    final String timestamp;

    public static class Transform implements EventTransform<ScribeEvent> {
        private final Gson gson;

        public Transform(Gson gson) {
            this.gson = gson;
        }

        public byte[] toBytes(ScribeEvent scribeEvent) throws IOException {
            return this.gson.toJson((Object) scribeEvent).getBytes(Key.STRING_CHARSET_NAME);
        }
    }

    public ScribeEvent(String str, EventNamespace eventNamespace, long j) {
        this(str, eventNamespace, j, Collections.emptyList());
    }

    public ScribeEvent(String str, EventNamespace eventNamespace, long j, List<ScribeItem> list) {
        this.category = str;
        this.eventNamespace = eventNamespace;
        this.timestamp = String.valueOf(j);
        this.formatVersion = CURRENT_FORMAT_VERSION;
        this.items = Collections.unmodifiableList(list);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("event_namespace=");
        stringBuilder.append(this.eventNamespace);
        stringBuilder.append(", ts=");
        stringBuilder.append(this.timestamp);
        stringBuilder.append(", format_version=");
        stringBuilder.append(this.formatVersion);
        stringBuilder.append(", _category_=");
        stringBuilder.append(this.category);
        stringBuilder.append(", items=");
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("[");
        stringBuilder2.append(TextUtils.join(", ", this.items));
        stringBuilder2.append("]");
        stringBuilder.append(stringBuilder2.toString());
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScribeEvent scribeEvent = (ScribeEvent) obj;
        if (!this.category == null ? this.category.equals(scribeEvent.category) : scribeEvent.category == null) {
            return false;
        }
        if (!this.eventNamespace == null ? this.eventNamespace.equals(scribeEvent.eventNamespace) : scribeEvent.eventNamespace == null) {
            return false;
        }
        if (!this.formatVersion == null ? this.formatVersion.equals(scribeEvent.formatVersion) : scribeEvent.formatVersion == null) {
            return false;
        }
        if (this.timestamp == null ? scribeEvent.timestamp == null : this.timestamp.equals(scribeEvent.timestamp)) {
            return this.items == null ? scribeEvent.items == null : this.items.equals(scribeEvent.items);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((this.eventNamespace != null ? this.eventNamespace.hashCode() : 0) * 31) + (this.timestamp != null ? this.timestamp.hashCode() : 0)) * 31) + (this.formatVersion != null ? this.formatVersion.hashCode() : 0)) * 31) + (this.category != null ? this.category.hashCode() : 0)) * 31;
        if (this.items != null) {
            i = this.items.hashCode();
        }
        return hashCode + i;
    }
}
