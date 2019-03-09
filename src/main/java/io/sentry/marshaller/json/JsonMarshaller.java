package io.sentry.marshaller.json;

import com.facebook.internal.ServerProtocol;
import com.facebook.share.internal.ShareConstants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.sentry.event.Breadcrumb;
import io.sentry.event.Event;
import io.sentry.event.Event.Level;
import io.sentry.event.Sdk;
import io.sentry.event.interfaces.SentryInterface;
import io.sentry.marshaller.Marshaller;
import io.sentry.marshaller.Marshaller.UncloseableOutputStream;
import io.sentry.util.Util;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonMarshaller implements Marshaller {
    public static final String BREADCRUMBS = "breadcrumbs";
    public static final String CHECKSUM = "checksum";
    public static final String CONTEXTS = "contexts";
    public static final String CULPRIT = "culprit";
    public static final int DEFAULT_MAX_MESSAGE_LENGTH = 1000;
    public static final String DIST = "dist";
    public static final String ENVIRONMENT = "environment";
    public static final String EVENT_ID = "event_id";
    public static final String EXTRA = "extra";
    public static final String FINGERPRINT = "fingerprint";
    private static final ThreadLocal<DateFormat> ISO_FORMAT = new ThreadLocal<DateFormat>() {
        /* Access modifiers changed, original: protected */
        public DateFormat initialValue() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat;
        }
    };
    public static final String LEVEL = "level";
    public static final String LOGGER = "logger";
    public static final String MESSAGE = "message";
    public static final String MODULES = "modules";
    public static final String PLATFORM = "platform";
    public static final String RELEASE = "release";
    public static final String SDK = "sdk";
    public static final String SERVER_NAME = "server_name";
    public static final String TAGS = "tags";
    public static final String TIMESTAMP = "timestamp";
    public static final String TRANSACTION = "transaction";
    private static final Logger logger = LoggerFactory.getLogger(JsonMarshaller.class);
    private boolean compression;
    private final Map<Class<? extends SentryInterface>, InterfaceBinding<?>> interfaceBindings;
    private final JsonFactory jsonFactory;
    private final int maxMessageLength;

    public String getContentType() {
        return "application/json";
    }

    public JsonMarshaller() {
        this(1000);
    }

    public JsonMarshaller(int i) {
        this.jsonFactory = new JsonFactory();
        this.interfaceBindings = new HashMap();
        this.compression = true;
        this.maxMessageLength = i;
    }

    public void marshall(Event event, OutputStream outputStream) throws IOException {
        OutputStream uncloseableOutputStream = new UncloseableOutputStream(outputStream);
        outputStream = this.compression ? new GZIPOutputStream(uncloseableOutputStream) : uncloseableOutputStream;
        JsonGenerator createJsonGenerator;
        try {
            createJsonGenerator = createJsonGenerator(outputStream);
            writeContent(createJsonGenerator, event);
            if (createJsonGenerator != null) {
                createJsonGenerator.close();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("An exception occurred while serialising the event.", e);
            }
        } catch (IOException e2) {
            try {
                logger.error("An exception occurred while serialising the event.", e2);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e3) {
                    logger.error("An exception occurred while serialising the event.", e3);
                }
            }
        } catch (Throwable unused) {
        }
    }

    /* Access modifiers changed, original: protected */
    public JsonGenerator createJsonGenerator(OutputStream outputStream) throws IOException {
        return new SentryJsonGenerator(this.jsonFactory.createGenerator(outputStream));
    }

    public String getContentEncoding() {
        return isCompressed() ? "gzip" : null;
    }

    private void writeContent(JsonGenerator jsonGenerator, Event event) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(EVENT_ID, formatId(event.getId()));
        jsonGenerator.writeStringField("message", Util.trimString(event.getMessage(), this.maxMessageLength));
        jsonGenerator.writeStringField(TIMESTAMP, ((DateFormat) ISO_FORMAT.get()).format(event.getTimestamp()));
        jsonGenerator.writeStringField(LEVEL, formatLevel(event.getLevel()));
        jsonGenerator.writeStringField(LOGGER, event.getLogger());
        jsonGenerator.writeStringField(PLATFORM, event.getPlatform());
        jsonGenerator.writeStringField(CULPRIT, event.getCulprit());
        jsonGenerator.writeStringField(TRANSACTION, event.getTransaction());
        writeSdk(jsonGenerator, event.getSdk());
        writeTags(jsonGenerator, event.getTags());
        writeBreadcumbs(jsonGenerator, event.getBreadcrumbs());
        writeContexts(jsonGenerator, event.getContexts());
        jsonGenerator.writeStringField(SERVER_NAME, event.getServerName());
        jsonGenerator.writeStringField("release", event.getRelease());
        jsonGenerator.writeStringField("dist", event.getDist());
        jsonGenerator.writeStringField("environment", event.getEnvironment());
        writeExtras(jsonGenerator, event.getExtra());
        writeCollection(jsonGenerator, FINGERPRINT, event.getFingerprint());
        jsonGenerator.writeStringField(CHECKSUM, event.getChecksum());
        writeInterfaces(jsonGenerator, event.getSentryInterfaces());
        jsonGenerator.writeEndObject();
    }

    private void writeInterfaces(JsonGenerator jsonGenerator, Map<String, SentryInterface> map) throws IOException {
        for (Entry entry : map.entrySet()) {
            Object obj = (SentryInterface) entry.getValue();
            if (this.interfaceBindings.containsKey(obj.getClass())) {
                jsonGenerator.writeFieldName((String) entry.getKey());
                getInterfaceBinding(obj).writeInterface(jsonGenerator, (SentryInterface) entry.getValue());
            } else {
                logger.error("Couldn't parse the content of '{}' provided in {}.", entry.getKey(), obj);
            }
        }
    }

    private <T extends SentryInterface> InterfaceBinding<? super T> getInterfaceBinding(T t) {
        return (InterfaceBinding) this.interfaceBindings.get(t.getClass());
    }

    private void writeExtras(JsonGenerator jsonGenerator, Map<String, Object> map) throws IOException {
        jsonGenerator.writeObjectFieldStart("extra");
        for (Entry entry : map.entrySet()) {
            jsonGenerator.writeFieldName((String) entry.getKey());
            jsonGenerator.writeObject(entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }

    private void writeCollection(JsonGenerator jsonGenerator, String str, Collection<String> collection) throws IOException {
        if (collection != null && !collection.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(str);
            for (String writeString : collection) {
                jsonGenerator.writeString(writeString);
            }
            jsonGenerator.writeEndArray();
        }
    }

    private void writeSdk(JsonGenerator jsonGenerator, Sdk sdk) throws IOException {
        jsonGenerator.writeObjectFieldStart("sdk");
        jsonGenerator.writeStringField("name", sdk.getName());
        jsonGenerator.writeStringField(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION, sdk.getVersion());
        if (!(sdk.getIntegrations() == null || sdk.getIntegrations().isEmpty())) {
            jsonGenerator.writeArrayFieldStart("integrations");
            for (String writeString : sdk.getIntegrations()) {
                jsonGenerator.writeString(writeString);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }

    private void writeTags(JsonGenerator jsonGenerator, Map<String, String> map) throws IOException {
        jsonGenerator.writeObjectFieldStart("tags");
        for (Entry entry : map.entrySet()) {
            jsonGenerator.writeStringField((String) entry.getKey(), (String) entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }

    private void writeBreadcumbs(JsonGenerator jsonGenerator, List<Breadcrumb> list) throws IOException {
        if (!list.isEmpty()) {
            jsonGenerator.writeObjectFieldStart(BREADCRUMBS);
            jsonGenerator.writeArrayFieldStart("values");
            for (Breadcrumb breadcrumb : list) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField(TIMESTAMP, breadcrumb.getTimestamp().getTime() / 1000);
                if (breadcrumb.getType() != null) {
                    jsonGenerator.writeStringField("type", breadcrumb.getType().getValue());
                }
                if (breadcrumb.getLevel() != null) {
                    jsonGenerator.writeStringField(LEVEL, breadcrumb.getLevel().getValue());
                }
                if (breadcrumb.getMessage() != null) {
                    jsonGenerator.writeStringField("message", breadcrumb.getMessage());
                }
                if (breadcrumb.getCategory() != null) {
                    jsonGenerator.writeStringField("category", breadcrumb.getCategory());
                }
                if (!(breadcrumb.getData() == null || breadcrumb.getData().isEmpty())) {
                    jsonGenerator.writeObjectFieldStart(ShareConstants.WEB_DIALOG_PARAM_DATA);
                    for (Entry entry : breadcrumb.getData().entrySet()) {
                        jsonGenerator.writeStringField((String) entry.getKey(), (String) entry.getValue());
                    }
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }

    private void writeContexts(JsonGenerator jsonGenerator, Map<String, Map<String, Object>> map) throws IOException {
        if (!map.isEmpty()) {
            jsonGenerator.writeObjectFieldStart(CONTEXTS);
            for (Entry entry : map.entrySet()) {
                jsonGenerator.writeObjectFieldStart((String) entry.getKey());
                for (Entry entry2 : ((Map) entry.getValue()).entrySet()) {
                    jsonGenerator.writeObjectField((String) entry2.getKey(), entry2.getValue());
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndObject();
        }
    }

    private String formatId(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    private String formatLevel(Level level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case DEBUG:
                return "debug";
            case FATAL:
                return "fatal";
            case WARNING:
                return "warning";
            case INFO:
                return "info";
            case ERROR:
                return "error";
            default:
                logger.error("The level '{}' isn't supported, this should NEVER happen, contact Sentry developers", level.name());
                return null;
        }
    }

    public <T extends SentryInterface, F extends T> void addInterfaceBinding(Class<F> cls, InterfaceBinding<T> interfaceBinding) {
        this.interfaceBindings.put(cls, interfaceBinding);
    }

    public void setCompression(boolean z) {
        this.compression = z;
    }

    public boolean isCompressed() {
        return this.compression;
    }
}
