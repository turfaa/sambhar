package com.twitter.sdk.android.core.internal.network;

import com.twitter.sdk.android.core.GuestSession;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;
import java.io.IOException;
import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.Route;

public class GuestAuthenticator implements Authenticator {
    static final int MAX_RETRIES = 2;
    final GuestSessionProvider guestSessionProvider;

    public GuestAuthenticator(GuestSessionProvider guestSessionProvider) {
        this.guestSessionProvider = guestSessionProvider;
    }

    public Request authenticate(Route route, Response response) throws IOException {
        return reauth(response);
    }

    /* Access modifiers changed, original: 0000 */
    public Request reauth(Response response) {
        if (canRetry(response)) {
            GuestAuthToken guestAuthToken;
            GuestSession refreshCurrentSession = this.guestSessionProvider.refreshCurrentSession(getExpiredSession(response));
            if (refreshCurrentSession == null) {
                guestAuthToken = null;
            } else {
                guestAuthToken = (GuestAuthToken) refreshCurrentSession.getAuthToken();
            }
            if (guestAuthToken != null) {
                return resign(response.request(), guestAuthToken);
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public GuestSession getExpiredSession(Response response) {
        Headers headers = response.request().headers();
        String str = headers.get(OAuthConstants.HEADER_AUTHORIZATION);
        String str2 = headers.get("x-guest-token");
        return (str == null || str2 == null) ? null : new GuestSession(new GuestAuthToken(OAuth2Token.TOKEN_TYPE_BEARER, str.replace("bearer ", ""), str2));
    }

    /* Access modifiers changed, original: 0000 */
    public Request resign(Request request, GuestAuthToken guestAuthToken) {
        Builder newBuilder = request.newBuilder();
        GuestAuthInterceptor.addAuthHeaders(newBuilder, guestAuthToken);
        return newBuilder.build();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean canRetry(Response response) {
        int i = 1;
        while (true) {
            response = response.priorResponse();
            if (response == null) {
                break;
            }
            i++;
        }
        return i < 2;
    }
}
