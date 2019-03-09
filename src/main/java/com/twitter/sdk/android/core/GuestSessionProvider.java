package com.twitter.sdk.android.core;

import com.twitter.sdk.android.core.internal.oauth.GuestAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Service;
import java.util.concurrent.CountDownLatch;

public class GuestSessionProvider {
    private final OAuth2Service oAuth2Service;
    private final SessionManager<GuestSession> sessionManager;

    public GuestSessionProvider(OAuth2Service oAuth2Service, SessionManager<GuestSession> sessionManager) {
        this.oAuth2Service = oAuth2Service;
        this.sessionManager = sessionManager;
    }

    public synchronized GuestSession getCurrentSession() {
        GuestSession guestSession = (GuestSession) this.sessionManager.getActiveSession();
        if (isSessionValid(guestSession)) {
            return guestSession;
        }
        refreshToken();
        return (GuestSession) this.sessionManager.getActiveSession();
    }

    public synchronized GuestSession refreshCurrentSession(GuestSession guestSession) {
        GuestSession guestSession2 = (GuestSession) this.sessionManager.getActiveSession();
        if (guestSession != null && guestSession.equals(guestSession2)) {
            refreshToken();
        }
        return (GuestSession) this.sessionManager.getActiveSession();
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshToken() {
        Twitter.getLogger().d("GuestSessionProvider", "Refreshing expired guest session.");
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        this.oAuth2Service.requestGuestAuthToken(new Callback<GuestAuthToken>() {
            public void success(Result<GuestAuthToken> result) {
                GuestSessionProvider.this.sessionManager.setActiveSession(new GuestSession((GuestAuthToken) result.data));
                countDownLatch.countDown();
            }

            public void failure(TwitterException twitterException) {
                GuestSessionProvider.this.sessionManager.clearSession(0);
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException unused) {
            this.sessionManager.clearSession(0);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isSessionValid(GuestSession guestSession) {
        return (guestSession == null || guestSession.getAuthToken() == null || ((GuestAuthToken) guestSession.getAuthToken()).isExpired()) ? false : true;
    }
}
