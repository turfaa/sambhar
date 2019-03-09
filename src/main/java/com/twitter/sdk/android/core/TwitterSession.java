package com.twitter.sdk.android.core;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.internal.persistence.SerializationStrategy;

public class TwitterSession extends Session<TwitterAuthToken> {
    public static final long UNKNOWN_USER_ID = -1;
    public static final String UNKNOWN_USER_NAME = "";
    @SerializedName("user_name")
    private final String userName;

    static class Serializer implements SerializationStrategy<TwitterSession> {
        private final Gson gson = new Gson();

        public String serialize(TwitterSession twitterSession) {
            if (!(twitterSession == null || twitterSession.getAuthToken() == null)) {
                try {
                    return this.gson.toJson((Object) twitterSession);
                } catch (Exception e) {
                    Twitter.getLogger().d("Twitter", e.getMessage());
                }
            }
            return "";
        }

        public TwitterSession deserialize(String str) {
            if (!TextUtils.isEmpty(str)) {
                try {
                    return (TwitterSession) this.gson.fromJson(str, TwitterSession.class);
                } catch (Exception e) {
                    Twitter.getLogger().d("Twitter", e.getMessage());
                }
            }
            return null;
        }
    }

    public TwitterSession(TwitterAuthToken twitterAuthToken, long j, String str) {
        super(twitterAuthToken, j);
        this.userName = str;
    }

    public long getUserId() {
        return getId();
    }

    public String getUserName() {
        return this.userName;
    }

    /* JADX WARNING: Missing block: B:17:0x002f, code skipped:
            return false;
     */
    public boolean equals(java.lang.Object r5) {
        /*
        r4 = this;
        r0 = 1;
        if (r4 != r5) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = 0;
        if (r5 == 0) goto L_0x002f;
    L_0x0007:
        r2 = r4.getClass();
        r3 = r5.getClass();
        if (r2 == r3) goto L_0x0012;
    L_0x0011:
        goto L_0x002f;
    L_0x0012:
        r2 = super.equals(r5);
        if (r2 != 0) goto L_0x0019;
    L_0x0018:
        return r1;
    L_0x0019:
        r5 = (com.twitter.sdk.android.core.TwitterSession) r5;
        r2 = r4.userName;
        if (r2 == 0) goto L_0x0028;
    L_0x001f:
        r0 = r4.userName;
        r5 = r5.userName;
        r0 = r0.equals(r5);
        goto L_0x002e;
    L_0x0028:
        r5 = r5.userName;
        if (r5 != 0) goto L_0x002d;
    L_0x002c:
        goto L_0x002e;
    L_0x002d:
        r0 = 0;
    L_0x002e:
        return r0;
    L_0x002f:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.TwitterSession.equals(java.lang.Object):boolean");
    }

    public int hashCode() {
        return (super.hashCode() * 31) + (this.userName != null ? this.userName.hashCode() : 0);
    }
}
