package com.twitter.sdk.android.core.internal.oauth;

import android.net.Uri;
import com.facebook.internal.ServerProtocol;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.network.UrlUtils;
import java.util.TreeMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class OAuth1aService extends OAuthService {
    private static final String CALLBACK_URL = "twittersdk://callback";
    private static final String PARAM_SCREEN_NAME = "screen_name";
    private static final String PARAM_USER_ID = "user_id";
    private static final String RESOURCE_OAUTH = "oauth";
    OAuthApi api = ((OAuthApi) getRetrofit().create(OAuthApi.class));

    interface OAuthApi {
        @POST("/oauth/access_token")
        Call<ResponseBody> getAccessToken(@Header("Authorization") String str, @Query("oauth_verifier") String str2);

        @POST("/oauth/request_token")
        Call<ResponseBody> getTempToken(@Header("Authorization") String str);
    }

    public OAuth1aService(TwitterCore twitterCore, TwitterApi twitterApi) {
        super(twitterCore, twitterApi);
    }

    public void requestTempToken(Callback<OAuthResponse> callback) {
        TwitterAuthConfig authConfig = getTwitterCore().getAuthConfig();
        this.api.getTempToken(new OAuth1aHeaders().getAuthorizationHeader(authConfig, null, buildCallbackUrl(authConfig), "POST", getTempTokenUrl(), null)).enqueue(getCallbackWrapper(callback));
    }

    /* Access modifiers changed, original: 0000 */
    public String getTempTokenUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getApi().getBaseHostUrl());
        stringBuilder.append("/oauth/request_token");
        return stringBuilder.toString();
    }

    public String buildCallbackUrl(TwitterAuthConfig twitterAuthConfig) {
        return Uri.parse(CALLBACK_URL).buildUpon().appendQueryParameter(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION, getTwitterCore().getVersion()).appendQueryParameter("app", twitterAuthConfig.getConsumerKey()).build().toString();
    }

    public void requestAccessToken(Callback<OAuthResponse> callback, TwitterAuthToken twitterAuthToken, String str) {
        TwitterAuthToken twitterAuthToken2 = twitterAuthToken;
        this.api.getAccessToken(new OAuth1aHeaders().getAuthorizationHeader(getTwitterCore().getAuthConfig(), twitterAuthToken2, null, "POST", getAccessTokenUrl(), null), str).enqueue(getCallbackWrapper(callback));
    }

    /* Access modifiers changed, original: 0000 */
    public String getAccessTokenUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getApi().getBaseHostUrl());
        stringBuilder.append("/oauth/access_token");
        return stringBuilder.toString();
    }

    public String getAuthorizeUrl(TwitterAuthToken twitterAuthToken) {
        return getApi().buildUponBaseHostUrl(RESOURCE_OAUTH, "authorize").appendQueryParameter(OAuthConstants.PARAM_TOKEN, twitterAuthToken.token).build().toString();
    }

    public static OAuthResponse parseAuthResponse(String str) {
        TreeMap queryParams = UrlUtils.getQueryParams(str, false);
        String str2 = (String) queryParams.get(OAuthConstants.PARAM_TOKEN);
        String str3 = (String) queryParams.get(OAuthConstants.PARAM_TOKEN_SECRET);
        return (str2 == null || str3 == null) ? null : new OAuthResponse(new TwitterAuthToken(str2, str3), (String) queryParams.get(PARAM_SCREEN_NAME), queryParams.containsKey("user_id") ? Long.parseLong((String) queryParams.get("user_id")) : 0);
    }

    /* Access modifiers changed, original: 0000 */
    public Callback<ResponseBody> getCallbackWrapper(final Callback<OAuthResponse> callback) {
        return new Callback<ResponseBody>() {
            /* JADX WARNING: Removed duplicated region for block: B:17:0x005c A:{Catch:{ IOException -> 0x0060 }} */
            public void success(com.twitter.sdk.android.core.Result<okhttp3.ResponseBody> r5) {
                /*
                r4 = this;
                r0 = new java.lang.StringBuilder;
                r0.<init>();
                r1 = 0;
                r2 = new java.io.BufferedReader;	 Catch:{ all -> 0x0059 }
                r3 = new java.io.InputStreamReader;	 Catch:{ all -> 0x0059 }
                r5 = r5.data;	 Catch:{ all -> 0x0059 }
                r5 = (okhttp3.ResponseBody) r5;	 Catch:{ all -> 0x0059 }
                r5 = r5.byteStream();	 Catch:{ all -> 0x0059 }
                r3.<init>(r5);	 Catch:{ all -> 0x0059 }
                r2.<init>(r3);	 Catch:{ all -> 0x0059 }
            L_0x0018:
                r5 = r2.readLine();	 Catch:{ all -> 0x0056 }
                if (r5 == 0) goto L_0x0022;
            L_0x001e:
                r0.append(r5);	 Catch:{ all -> 0x0056 }
                goto L_0x0018;
            L_0x0022:
                r2.close();	 Catch:{ IOException -> 0x0060 }
                r5 = r0.toString();	 Catch:{ IOException -> 0x0060 }
                r0 = com.twitter.sdk.android.core.internal.oauth.OAuth1aService.parseAuthResponse(r5);	 Catch:{ IOException -> 0x0060 }
                if (r0 != 0) goto L_0x004b;
            L_0x002f:
                r0 = r2;	 Catch:{ IOException -> 0x0060 }
                r1 = new com.twitter.sdk.android.core.TwitterAuthException;	 Catch:{ IOException -> 0x0060 }
                r2 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0060 }
                r2.<init>();	 Catch:{ IOException -> 0x0060 }
                r3 = "Failed to parse auth response: ";
                r2.append(r3);	 Catch:{ IOException -> 0x0060 }
                r2.append(r5);	 Catch:{ IOException -> 0x0060 }
                r5 = r2.toString();	 Catch:{ IOException -> 0x0060 }
                r1.<init>(r5);	 Catch:{ IOException -> 0x0060 }
                r0.failure(r1);	 Catch:{ IOException -> 0x0060 }
                goto L_0x0071;
            L_0x004b:
                r5 = r2;	 Catch:{ IOException -> 0x0060 }
                r2 = new com.twitter.sdk.android.core.Result;	 Catch:{ IOException -> 0x0060 }
                r2.<init>(r0, r1);	 Catch:{ IOException -> 0x0060 }
                r5.success(r2);	 Catch:{ IOException -> 0x0060 }
                goto L_0x0071;
            L_0x0056:
                r5 = move-exception;
                r1 = r2;
                goto L_0x005a;
            L_0x0059:
                r5 = move-exception;
            L_0x005a:
                if (r1 == 0) goto L_0x0062;
            L_0x005c:
                r1.close();	 Catch:{ IOException -> 0x0060 }
                goto L_0x0062;
            L_0x0060:
                r5 = move-exception;
                goto L_0x0063;
            L_0x0062:
                throw r5;	 Catch:{ IOException -> 0x0060 }
            L_0x0063:
                r0 = r2;
                r1 = new com.twitter.sdk.android.core.TwitterAuthException;
                r2 = r5.getMessage();
                r1.<init>(r2, r5);
                r0.failure(r1);
            L_0x0071:
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.internal.oauth.OAuth1aService$AnonymousClass1.success(com.twitter.sdk.android.core.Result):void");
            }

            public void failure(TwitterException twitterException) {
                callback.failure(twitterException);
            }
        };
    }
}
