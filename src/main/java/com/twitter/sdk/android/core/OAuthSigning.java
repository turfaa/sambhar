package com.twitter.sdk.android.core;

import com.twitter.sdk.android.core.internal.oauth.OAuth1aHeaders;
import java.util.Map;

public class OAuthSigning {
    static final String VERIFY_CREDENTIALS_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    final TwitterAuthConfig authConfig;
    final TwitterAuthToken authToken;
    final OAuth1aHeaders oAuth1aHeaders;

    public OAuthSigning(TwitterAuthConfig twitterAuthConfig, TwitterAuthToken twitterAuthToken) {
        this(twitterAuthConfig, twitterAuthToken, new OAuth1aHeaders());
    }

    OAuthSigning(TwitterAuthConfig twitterAuthConfig, TwitterAuthToken twitterAuthToken, OAuth1aHeaders oAuth1aHeaders) {
        if (twitterAuthConfig == null) {
            throw new IllegalArgumentException("authConfig must not be null");
        } else if (twitterAuthToken != null) {
            this.authConfig = twitterAuthConfig;
            this.authToken = twitterAuthToken;
            this.oAuth1aHeaders = oAuth1aHeaders;
        } else {
            throw new IllegalArgumentException("authToken must not be null");
        }
    }

    public String getAuthorizationHeader(String str, String str2, Map<String, String> map) {
        return this.oAuth1aHeaders.getAuthorizationHeader(this.authConfig, this.authToken, null, str, str2, map);
    }

    public Map<String, String> getOAuthEchoHeaders(String str, String str2, Map<String, String> map) {
        return this.oAuth1aHeaders.getOAuthEchoHeaders(this.authConfig, this.authToken, null, str, str2, map);
    }

    public Map<String, String> getOAuthEchoHeadersForVerifyCredentials() {
        return this.oAuth1aHeaders.getOAuthEchoHeaders(this.authConfig, this.authToken, null, "GET", VERIFY_CREDENTIALS_URL, null);
    }
}
