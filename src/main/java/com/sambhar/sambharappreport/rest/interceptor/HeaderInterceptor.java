package com.sambhar.sambharappreport.rest.interceptor;

import com.facebook.internal.ServerProtocol;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;
import java.io.IOException;
import javax.inject.Inject;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    UserSharedPref mUserUtils;

    @Inject
    public HeaderInterceptor(UserSharedPref userSharedPref) {
        this.mUserUtils = userSharedPref;
    }

    public Response intercept(Chain chain) throws IOException {
        Builder newBuilder = chain.request().newBuilder();
        String str = OAuthConstants.HEADER_AUTHORIZATION;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Bearer ");
        stringBuilder.append(this.mUserUtils.getUserToken());
        return chain.proceed(newBuilder.addHeader(str, stringBuilder.toString()).addHeader(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION, "5").build());
    }
}
