package io.sentry.event.interfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MessageInterface implements SentryInterface {
    public static final String MESSAGE_INTERFACE = "sentry.interfaces.Message";
    private final String formatted;
    private final String message;
    private final List<String> parameters;

    public String getInterfaceName() {
        return MESSAGE_INTERFACE;
    }

    public MessageInterface(String str) {
        this(str, Collections.emptyList());
    }

    public MessageInterface(String str, String... strArr) {
        this(str, Arrays.asList(strArr));
    }

    public MessageInterface(String str, List<String> list) {
        this(str, list, null);
    }

    public MessageInterface(String str, List<String> list, String str2) {
        this.message = str;
        this.parameters = Collections.unmodifiableList(new ArrayList(list));
        this.formatted = str2;
    }

    public String getMessage() {
        return this.message;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public String getFormatted() {
        return this.formatted;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MessageInterface{message='");
        stringBuilder.append(this.message);
        stringBuilder.append('\'');
        stringBuilder.append(", parameters=");
        stringBuilder.append(this.parameters);
        stringBuilder.append(", formatted=");
        stringBuilder.append(this.formatted);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageInterface messageInterface = (MessageInterface) obj;
        if (!(Objects.equals(this.message, messageInterface.message) && Objects.equals(this.parameters, messageInterface.parameters) && Objects.equals(this.formatted, messageInterface.formatted))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.message, this.parameters, this.formatted});
    }
}
