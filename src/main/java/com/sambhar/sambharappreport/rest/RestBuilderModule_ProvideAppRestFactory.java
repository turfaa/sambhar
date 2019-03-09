package com.sambhar.sambharappreport.rest;

import com.google.gson.Gson;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

public final class RestBuilderModule_ProvideAppRestFactory implements Factory<AppRest> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Gson> gsonProvider;
    private final Provider<LiveDataCallAdapterFactory> liveDataCallAdapterFactoryProvider;
    private final RestBuilderModule module;
    private final Provider<OkHttpClient> okHttpClientProvider;

    public RestBuilderModule_ProvideAppRestFactory(RestBuilderModule restBuilderModule, Provider<OkHttpClient> provider, Provider<Gson> provider2, Provider<LiveDataCallAdapterFactory> provider3) {
        this.module = restBuilderModule;
        this.okHttpClientProvider = provider;
        this.gsonProvider = provider2;
        this.liveDataCallAdapterFactoryProvider = provider3;
    }

    public AppRest get() {
        return (AppRest) Preconditions.checkNotNull(this.module.provideAppRest((OkHttpClient) this.okHttpClientProvider.get(), (Gson) this.gsonProvider.get(), (LiveDataCallAdapterFactory) this.liveDataCallAdapterFactoryProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<AppRest> create(RestBuilderModule restBuilderModule, Provider<OkHttpClient> provider, Provider<Gson> provider2, Provider<LiveDataCallAdapterFactory> provider3) {
        return new RestBuilderModule_ProvideAppRestFactory(restBuilderModule, provider, provider2, provider3);
    }

    public static AppRest proxyProvideAppRest(RestBuilderModule restBuilderModule, OkHttpClient okHttpClient, Gson gson, LiveDataCallAdapterFactory liveDataCallAdapterFactory) {
        return restBuilderModule.provideAppRest(okHttpClient, gson, liveDataCallAdapterFactory);
    }
}
