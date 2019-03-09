package com.sambhar.sambharappreport.rest;

import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RestBuilderModule {
    String HTTPS_API_WEALTFLOW_URL = "http://120.29.226.193:3080/";

    /* Access modifiers changed, original: 0000 */
    @Singleton
    @Provides
    public AppRest provideAppRest(OkHttpClient okHttpClient, Gson gson, LiveDataCallAdapterFactory liveDataCallAdapterFactory) {
        return (AppRest) new Builder().baseUrl(this.HTTPS_API_WEALTFLOW_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(liveDataCallAdapterFactory).build().create(AppRest.class);
    }
}
