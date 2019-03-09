package com.twitter.sdk.android.core;

import com.google.gson.annotations.SerializedName;

public class Session<T extends AuthToken> {
    @SerializedName("auth_token")
    private final T authToken;
    @SerializedName("id")
    private final long id;

    public Session(T t, long j) {
        if (t != null) {
            this.authToken = t;
            this.id = j;
            return;
        }
        throw new IllegalArgumentException("AuthToken must not be null.");
    }

    public T getAuthToken() {
        return this.authToken;
    }

    public long getId() {
        return this.id;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Session session = (Session) obj;
        if (this.id != session.id) {
            return false;
        }
        if (this.authToken != null) {
            z = this.authToken.equals(session.authToken);
        } else if (session.authToken != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((this.authToken != null ? this.authToken.hashCode() : 0) * 31) + ((int) (this.id ^ (this.id >>> 32)));
    }
}
