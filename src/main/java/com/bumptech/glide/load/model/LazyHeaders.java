package com.bumptech.glide.load.model;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class LazyHeaders implements Headers {
    private volatile Map<String, String> combinedHeaders;
    private final Map<String, List<LazyHeaderFactory>> headers;

    public static final class Builder {
        private static final Map<String, List<LazyHeaderFactory>> DEFAULT_HEADERS;
        private static final String DEFAULT_USER_AGENT = getSanitizedUserAgent();
        private static final String USER_AGENT_HEADER = "User-Agent";
        private boolean copyOnModify = true;
        private Map<String, List<LazyHeaderFactory>> headers = DEFAULT_HEADERS;
        private boolean isUserAgentDefault = true;

        static {
            HashMap hashMap = new HashMap(2);
            if (!TextUtils.isEmpty(DEFAULT_USER_AGENT)) {
                hashMap.put(USER_AGENT_HEADER, Collections.singletonList(new StringHeaderFactory(DEFAULT_USER_AGENT)));
            }
            DEFAULT_HEADERS = Collections.unmodifiableMap(hashMap);
        }

        public Builder addHeader(String str, String str2) {
            return addHeader(str, new StringHeaderFactory(str2));
        }

        public Builder addHeader(String str, LazyHeaderFactory lazyHeaderFactory) {
            if (this.isUserAgentDefault && USER_AGENT_HEADER.equalsIgnoreCase(str)) {
                return setHeader(str, lazyHeaderFactory);
            }
            copyIfNecessary();
            getFactories(str).add(lazyHeaderFactory);
            return this;
        }

        public Builder setHeader(String str, String str2) {
            return setHeader(str, str2 == null ? null : new StringHeaderFactory(str2));
        }

        public Builder setHeader(String str, LazyHeaderFactory lazyHeaderFactory) {
            copyIfNecessary();
            if (lazyHeaderFactory == null) {
                this.headers.remove(str);
            } else {
                List factories = getFactories(str);
                factories.clear();
                factories.add(lazyHeaderFactory);
            }
            if (this.isUserAgentDefault && USER_AGENT_HEADER.equalsIgnoreCase(str)) {
                this.isUserAgentDefault = false;
            }
            return this;
        }

        private List<LazyHeaderFactory> getFactories(String str) {
            List<LazyHeaderFactory> list = (List) this.headers.get(str);
            if (list != null) {
                return list;
            }
            ArrayList arrayList = new ArrayList();
            this.headers.put(str, arrayList);
            return arrayList;
        }

        private void copyIfNecessary() {
            if (this.copyOnModify) {
                this.copyOnModify = false;
                this.headers = copyHeaders();
            }
        }

        public LazyHeaders build() {
            this.copyOnModify = true;
            return new LazyHeaders(this.headers);
        }

        private Map<String, List<LazyHeaderFactory>> copyHeaders() {
            HashMap hashMap = new HashMap(this.headers.size());
            for (Entry entry : this.headers.entrySet()) {
                hashMap.put(entry.getKey(), new ArrayList((Collection) entry.getValue()));
            }
            return hashMap;
        }

        @VisibleForTesting
        static String getSanitizedUserAgent() {
            String property = System.getProperty("http.agent");
            if (TextUtils.isEmpty(property)) {
                return property;
            }
            int length = property.length();
            StringBuilder stringBuilder = new StringBuilder(property.length());
            for (int i = 0; i < length; i++) {
                char charAt = property.charAt(i);
                if ((charAt > 31 || charAt == 9) && charAt < 127) {
                    stringBuilder.append(charAt);
                } else {
                    stringBuilder.append('?');
                }
            }
            return stringBuilder.toString();
        }
    }

    static final class StringHeaderFactory implements LazyHeaderFactory {
        private final String value;

        StringHeaderFactory(String str) {
            this.value = str;
        }

        public String buildHeader() {
            return this.value;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("StringHeaderFactory{value='");
            stringBuilder.append(this.value);
            stringBuilder.append('\'');
            stringBuilder.append('}');
            return stringBuilder.toString();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof StringHeaderFactory)) {
                return false;
            }
            return this.value.equals(((StringHeaderFactory) obj).value);
        }

        public int hashCode() {
            return this.value.hashCode();
        }
    }

    LazyHeaders(Map<String, List<LazyHeaderFactory>> map) {
        this.headers = Collections.unmodifiableMap(map);
    }

    public Map<String, String> getHeaders() {
        if (this.combinedHeaders == null) {
            synchronized (this) {
                if (this.combinedHeaders == null) {
                    this.combinedHeaders = Collections.unmodifiableMap(generateHeaders());
                }
            }
        }
        return this.combinedHeaders;
    }

    private Map<String, String> generateHeaders() {
        HashMap hashMap = new HashMap();
        for (Entry entry : this.headers.entrySet()) {
            String buildHeaderValue = buildHeaderValue((List) entry.getValue());
            if (!TextUtils.isEmpty(buildHeaderValue)) {
                hashMap.put(entry.getKey(), buildHeaderValue);
            }
        }
        return hashMap;
    }

    @NonNull
    private String buildHeaderValue(@NonNull List<LazyHeaderFactory> list) {
        StringBuilder stringBuilder = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String buildHeader = ((LazyHeaderFactory) list.get(i)).buildHeader();
            if (!TextUtils.isEmpty(buildHeader)) {
                stringBuilder.append(buildHeader);
                if (i != list.size() - 1) {
                    stringBuilder.append(',');
                }
            }
        }
        return stringBuilder.toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LazyHeaders{headers=");
        stringBuilder.append(this.headers);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LazyHeaders)) {
            return false;
        }
        return this.headers.equals(((LazyHeaders) obj).headers);
    }

    public int hashCode() {
        return this.headers.hashCode();
    }
}
