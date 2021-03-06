package com.twitter.sdk.android.core.internal.oauth;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

abstract class OAuthService {
    private static final String CLIENT_NAME = "TwitterAndroidSDK";
    private final TwitterApi api;
    private final Retrofit retrofit = new Builder().baseUrl(getApi().getBaseHostUrl()).client(new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request().newBuilder().header("User-Agent", OAuthService.this.getUserAgent()).build());
        }
    }).certificatePinner(OkHttpClientHelper.getCertificatePinner()).build()).addConverterFactory(GsonConverterFactory.create()).build();
    private final TwitterCore twitterCore;
    private final String userAgent;

    OAuthService(TwitterCore twitterCore, TwitterApi twitterApi) {
        this.twitterCore = twitterCore;
        this.api = twitterApi;
        this.userAgent = TwitterApi.buildUserAgent(CLIENT_NAME, twitterCore.getVersion());
    }

    /* Access modifiers changed, original: protected */
    public TwitterCore getTwitterCore() {
        return this.twitterCore;
    }

    /* Access modifiers changed, original: protected */
    public TwitterApi getApi() {
        return this.api;
    }

    /* Access modifiers changed, original: protected */
    public String getUserAgent() {
        return this.userAgent;
    }

    /* Access modifiers changed, original: protected */
    public Retrofit getRetrofit() {
        return this.retrofit;
    }
}
