package com.sambhar.sambharappreport.di;

import android.app.Application;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.sambhar.sambharappreport.SambharApplication;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.rest.interceptor.AuthenticationInterceptor;
import com.sambhar.sambharappreport.rest.interceptor.HeaderInterceptor;
import dagger.Module;
import dagger.Provides;
import java.util.Date;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

@Module
public class AppModule {
    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public Context provideContext(Application application) {
        return application;
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public SambharApplication provideShambarApplication(Application application) {
        return (SambharApplication) application;
    }

    /* Access modifiers changed, original: 0000 */
    @Provides
    public UserSharedPref provideUserSharedPref(Context context) {
        return new UserSharedPref(context);
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public Gson provideGson() {
        return new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeAdapter()).create();
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public OkHttpClient provideOkHttpClient(HeaderInterceptor headerInterceptor, AuthenticationInterceptor authenticationInterceptor, ChuckInterceptor chuckInterceptor) {
        Builder builder = new Builder();
        builder.addInterceptor(headerInterceptor);
        builder.addInterceptor(authenticationInterceptor);
        builder.addInterceptor(chuckInterceptor);
        return builder.build();
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public HeaderInterceptor provideHeaderInterceptor(UserSharedPref userSharedPref) {
        return new HeaderInterceptor(userSharedPref);
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public AuthenticationInterceptor provideAuthenticationInterceptor(UserSharedPref userSharedPref) {
        return new AuthenticationInterceptor(userSharedPref);
    }

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public ChuckInterceptor provideChuckInterceptor(Context context) {
        return new ChuckInterceptor(context);
    }
}
