package com.twitter.sdk.android.core.internal.network;

import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.internal.oauth.OAuth1aHeaders;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OAuth1aInterceptor implements Interceptor {
    final TwitterAuthConfig authConfig;
    final Session<? extends TwitterAuthToken> session;

    public OAuth1aInterceptor(Session<? extends TwitterAuthToken> session, TwitterAuthConfig twitterAuthConfig) {
        this.session = session;
        this.authConfig = twitterAuthConfig;
    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request = request.newBuilder().url(urlWorkaround(request.url())).build();
        return chain.proceed(request.newBuilder().header(OAuthConstants.HEADER_AUTHORIZATION, getAuthorizationHeader(request)).build());
    }

    /* Access modifiers changed, original: 0000 */
    public HttpUrl urlWorkaround(HttpUrl httpUrl) {
        Builder query = httpUrl.newBuilder().query(null);
        int querySize = httpUrl.querySize();
        for (int i = 0; i < querySize; i++) {
            query.addEncodedQueryParameter(UrlUtils.percentEncode(httpUrl.queryParameterName(i)), UrlUtils.percentEncode(httpUrl.queryParameterValue(i)));
        }
        return query.build();
    }

    /* Access modifiers changed, original: 0000 */
    public String getAuthorizationHeader(Request request) throws IOException {
        return new OAuth1aHeaders().getAuthorizationHeader(this.authConfig, (TwitterAuthToken) this.session.getAuthToken(), null, request.method(), request.url().toString(), getPostParams(request));
    }

    /* Access modifiers changed, original: 0000 */
    public Map<String, String> getPostParams(Request request) throws IOException {
        HashMap hashMap = new HashMap();
        if ("POST".equals(request.method().toUpperCase(Locale.US))) {
            RequestBody body = request.body();
            if (body instanceof FormBody) {
                FormBody formBody = (FormBody) body;
                for (int i = 0; i < formBody.size(); i++) {
                    hashMap.put(formBody.encodedName(i), formBody.value(i));
                }
            }
        }
        return hashMap;
    }
}
