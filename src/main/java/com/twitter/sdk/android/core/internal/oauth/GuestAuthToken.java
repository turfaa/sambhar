package com.twitter.sdk.android.core.internal.oauth;

import com.google.gson.annotations.SerializedName;

public class GuestAuthToken extends OAuth2Token {
    private static final long EXPIRES_IN_MS = 10800000;
    public static final String HEADER_GUEST_TOKEN = "x-guest-token";
    @SerializedName("guest_token")
    private final String guestToken;

    public GuestAuthToken(String str, String str2, String str3) {
        super(str, str2);
        this.guestToken = str3;
    }

    public GuestAuthToken(String str, String str2, String str3, long j) {
        super(str, str2, j);
        this.guestToken = str3;
    }

    public String getGuestToken() {
        return this.guestToken;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.createdAt + EXPIRES_IN_MS;
    }

    /* JADX WARNING: Missing block: B:18:0x0030, code skipped:
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
        if (r5 == 0) goto L_0x0030;
    L_0x0007:
        r2 = r4.getClass();
        r3 = r5.getClass();
        if (r2 == r3) goto L_0x0012;
    L_0x0011:
        goto L_0x0030;
    L_0x0012:
        r2 = super.equals(r5);
        if (r2 != 0) goto L_0x0019;
    L_0x0018:
        return r1;
    L_0x0019:
        r5 = (com.twitter.sdk.android.core.internal.oauth.GuestAuthToken) r5;
        r2 = r4.guestToken;
        if (r2 == 0) goto L_0x002a;
    L_0x001f:
        r2 = r4.guestToken;
        r5 = r5.guestToken;
        r5 = r2.equals(r5);
        if (r5 != 0) goto L_0x002f;
    L_0x0029:
        goto L_0x002e;
    L_0x002a:
        r5 = r5.guestToken;
        if (r5 == 0) goto L_0x002f;
    L_0x002e:
        return r1;
    L_0x002f:
        return r0;
    L_0x0030:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.internal.oauth.GuestAuthToken.equals(java.lang.Object):boolean");
    }

    public int hashCode() {
        return (super.hashCode() * 31) + (this.guestToken != null ? this.guestToken.hashCode() : 0);
    }
}
