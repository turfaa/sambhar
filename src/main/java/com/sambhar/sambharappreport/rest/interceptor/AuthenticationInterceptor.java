package com.sambhar.sambharappreport.rest.interceptor;

import com.sambhar.sambharappreport.data.SambharConstant;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.event.LogoutEvent;
import java.io.IOException;
import javax.inject.Inject;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;

public class AuthenticationInterceptor implements Interceptor {
    UserSharedPref mUserSharedPref;

    @Inject
    public AuthenticationInterceptor(UserSharedPref userSharedPref) {
        this.mUserSharedPref = userSharedPref;
    }

    public Response intercept(Chain chain) throws IOException {
        Response proceed = chain.proceed(chain.request());
        if (proceed.code() == SambharConstant.API_UNAUTHORIZE) {
            this.mUserSharedPref.clearSession();
            EventBus.getDefault().post(new LogoutEvent());
        }
        return proceed;
    }
}
