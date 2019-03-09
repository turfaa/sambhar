package com.readystatesoftware.chuck;

import android.content.Context;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Response;

public final class ChuckInterceptor implements Interceptor {

    public enum Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }

    public ChuckInterceptor maxContentLength(long j) {
        return this;
    }

    public ChuckInterceptor retainDataFor(Period period) {
        return this;
    }

    public ChuckInterceptor showNotification(boolean z) {
        return this;
    }

    public ChuckInterceptor(Context context) {
    }

    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
