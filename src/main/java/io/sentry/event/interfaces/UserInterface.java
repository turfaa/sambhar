package io.sentry.event.interfaces;

import java.util.Map;
import java.util.Objects;

public class UserInterface implements SentryInterface {
    public static final String USER_INTERFACE = "sentry.interfaces.User";
    private final Map<String, Object> data;
    private final String email;
    private final String id;
    private final String ipAddress;
    private final String username;

    public String getInterfaceName() {
        return USER_INTERFACE;
    }

    public UserInterface(String str, String str2, String str3, String str4, Map<String, Object> map) {
        this.id = str;
        this.username = str2;
        this.ipAddress = str3;
        this.email = str4;
        this.data = map;
    }

    public UserInterface(String str, String str2, String str3, String str4) {
        this(str, str2, str3, str4, null);
    }

    public String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getEmail() {
        return this.email;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserInterface userInterface = (UserInterface) obj;
        if (!(Objects.equals(this.id, userInterface.id) && Objects.equals(this.username, userInterface.username) && Objects.equals(this.ipAddress, userInterface.ipAddress) && Objects.equals(this.email, userInterface.email) && Objects.equals(this.data, userInterface.data))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.username, this.ipAddress, this.email, this.data});
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UserInterface{id='");
        stringBuilder.append(this.id);
        stringBuilder.append('\'');
        stringBuilder.append(", username='");
        stringBuilder.append(this.username);
        stringBuilder.append('\'');
        stringBuilder.append(", ipAddress='");
        stringBuilder.append(this.ipAddress);
        stringBuilder.append('\'');
        stringBuilder.append(", email='");
        stringBuilder.append(this.email);
        stringBuilder.append('\'');
        stringBuilder.append(", data=");
        stringBuilder.append(this.data);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
