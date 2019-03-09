package io.sentry.event;

import com.bumptech.glide.load.Key;
import io.sentry.environment.SentryEnvironment;
import io.sentry.event.Event.Level;
import io.sentry.event.interfaces.SentryInterface;
import io.sentry.event.interfaces.SentryStackTraceElement;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBuilder {
    public static final String DEFAULT_HOSTNAME = "unavailable";
    public static final String DEFAULT_PLATFORM = "java";
    private static final HostnameCache HOSTNAME_CACHE = new HostnameCache(HOSTNAME_CACHE_DURATION);
    public static final long HOSTNAME_CACHE_DURATION = TimeUnit.HOURS.toMillis(5);
    private static final Charset UTF_8 = Charset.forName(Key.STRING_CHARSET_NAME);
    private boolean alreadyBuilt;
    private final Event event;
    private Set<String> sdkIntegrations;

    private static final class HostnameCache {
        public static final long GET_HOSTNAME_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
        private static final Logger logger = LoggerFactory.getLogger(HostnameCache.class);
        private final long cacheDuration;
        private volatile long expirationTimestamp;
        private volatile String hostname;
        private AtomicBoolean updateRunning;

        private HostnameCache(long j) {
            this.hostname = EventBuilder.DEFAULT_HOSTNAME;
            this.updateRunning = new AtomicBoolean(false);
            this.cacheDuration = j;
        }

        public String getHostname() {
            if (this.expirationTimestamp < System.currentTimeMillis() && this.updateRunning.compareAndSet(false, true)) {
                updateCache();
            }
            return this.hostname;
        }

        public void updateCache() {
            AnonymousClass1 anonymousClass1 = new Callable<Void>() {
                public Void call() throws Exception {
                    try {
                        HostnameCache.this.hostname = InetAddress.getLocalHost().getCanonicalHostName();
                        HostnameCache.this.expirationTimestamp = System.currentTimeMillis() + HostnameCache.this.cacheDuration;
                        return null;
                    } finally {
                        HostnameCache.this.updateRunning.set(false);
                    }
                }
            };
            try {
                logger.debug("Updating the hostname cache");
                FutureTask futureTask = new FutureTask(anonymousClass1);
                new Thread(futureTask).start();
                futureTask.get(GET_HOSTNAME_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                this.expirationTimestamp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1);
                logger.debug("Localhost hostname lookup failed, keeping the value '{}'. If this persists it may mean your DNS is incorrectly configured and you may want to hardcode your server name: https://docs.sentry.io/clients/java/config/", this.hostname, e);
            }
        }
    }

    public EventBuilder() {
        this(UUID.randomUUID());
    }

    public EventBuilder(UUID uuid) {
        this.alreadyBuilt = false;
        this.sdkIntegrations = new HashSet();
        this.event = new Event(uuid);
    }

    private static String calculateChecksum(String str) {
        byte[] bytes = str.getBytes(UTF_8);
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return Long.toHexString(crc32.getValue()).toUpperCase();
    }

    private void autoSetMissingValues() {
        if (this.event.getTimestamp() == null) {
            this.event.setTimestamp(new Date());
        }
        if (this.event.getPlatform() == null) {
            this.event.setPlatform(DEFAULT_PLATFORM);
        }
        if (this.event.getSdk() == null) {
            this.event.setSdk(new Sdk(SentryEnvironment.SDK_NAME, "1.7.16-9b60b", this.sdkIntegrations));
        }
        if (this.event.getServerName() == null) {
            this.event.setServerName(HOSTNAME_CACHE.getHostname());
        }
    }

    private void makeImmutable() {
        this.event.setTags(Collections.unmodifiableMap(this.event.getTags()));
        this.event.setBreadcrumbs(Collections.unmodifiableList(this.event.getBreadcrumbs()));
        HashMap hashMap = new HashMap();
        for (Entry entry : this.event.getContexts().entrySet()) {
            hashMap.put(entry.getKey(), Collections.unmodifiableMap((Map) entry.getValue()));
        }
        this.event.setContexts(Collections.unmodifiableMap(hashMap));
        this.event.setExtra(Collections.unmodifiableMap(this.event.getExtra()));
        this.event.setSentryInterfaces(Collections.unmodifiableMap(this.event.getSentryInterfaces()));
    }

    public EventBuilder withMessage(String str) {
        this.event.setMessage(str);
        return this;
    }

    public EventBuilder withTimestamp(Date date) {
        this.event.setTimestamp(date);
        return this;
    }

    public EventBuilder withLevel(Level level) {
        this.event.setLevel(level);
        return this;
    }

    public EventBuilder withRelease(String str) {
        this.event.setRelease(str);
        return this;
    }

    public EventBuilder withDist(String str) {
        this.event.setDist(str);
        return this;
    }

    public EventBuilder withEnvironment(String str) {
        this.event.setEnvironment(str);
        return this;
    }

    public EventBuilder withLogger(String str) {
        this.event.setLogger(str);
        return this;
    }

    public EventBuilder withPlatform(String str) {
        this.event.setPlatform(str);
        return this;
    }

    public EventBuilder withSdkIntegration(String str) {
        this.sdkIntegrations.add(str);
        return this;
    }

    @Deprecated
    public EventBuilder withCulprit(SentryStackTraceElement sentryStackTraceElement) {
        return withCulprit(buildCulpritString(sentryStackTraceElement.getModule(), sentryStackTraceElement.getFunction(), sentryStackTraceElement.getFileName(), sentryStackTraceElement.getLineno()));
    }

    @Deprecated
    public EventBuilder withCulprit(StackTraceElement stackTraceElement) {
        return withCulprit(buildCulpritString(stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber()));
    }

    private String buildCulpritString(String str, String str2, String str3, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(".");
        stringBuilder.append(str2);
        if (!(str3 == null || str3.isEmpty())) {
            stringBuilder.append("(");
            stringBuilder.append(str3);
            if (i >= 0) {
                stringBuilder.append(":");
                stringBuilder.append(i);
            }
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    @Deprecated
    public EventBuilder withCulprit(String str) {
        this.event.setCulprit(str);
        return this;
    }

    public EventBuilder withTransaction(String str) {
        this.event.setTransaction(str);
        return this;
    }

    public EventBuilder withTag(String str, String str2) {
        this.event.getTags().put(str, str2);
        return this;
    }

    public EventBuilder withBreadcrumbs(List<Breadcrumb> list) {
        this.event.setBreadcrumbs(list);
        return this;
    }

    public EventBuilder withContexts(Map<String, Map<String, Object>> map) {
        this.event.setContexts(map);
        return this;
    }

    public EventBuilder withServerName(String str) {
        this.event.setServerName(str);
        return this;
    }

    public EventBuilder withExtra(String str, Object obj) {
        this.event.getExtra().put(str, obj);
        return this;
    }

    public EventBuilder withFingerprint(String... strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        Collections.addAll(arrayList, strArr);
        this.event.setFingerprint(arrayList);
        return this;
    }

    public EventBuilder withFingerprint(List<String> list) {
        this.event.setFingerprint(list);
        return this;
    }

    public EventBuilder withChecksumFor(String str) {
        return withChecksum(calculateChecksum(str));
    }

    public EventBuilder withChecksum(String str) {
        this.event.setChecksum(str);
        return this;
    }

    public EventBuilder withSentryInterface(SentryInterface sentryInterface) {
        return withSentryInterface(sentryInterface, true);
    }

    public EventBuilder withSentryInterface(SentryInterface sentryInterface, boolean z) {
        if (z || !this.event.getSentryInterfaces().containsKey(sentryInterface.getInterfaceName())) {
            this.event.getSentryInterfaces().put(sentryInterface.getInterfaceName(), sentryInterface);
        }
        return this;
    }

    public synchronized Event build() {
        if (this.alreadyBuilt) {
            throw new IllegalStateException("A message can't be built twice");
        }
        autoSetMissingValues();
        makeImmutable();
        this.alreadyBuilt = true;
        return this.event;
    }

    public Event getEvent() {
        return this.event;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EventBuilder{event=");
        stringBuilder.append(this.event);
        stringBuilder.append(", alreadyBuilt=");
        stringBuilder.append(this.alreadyBuilt);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
