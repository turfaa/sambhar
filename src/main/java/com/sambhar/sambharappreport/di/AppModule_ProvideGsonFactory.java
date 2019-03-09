package com.sambhar.sambharappreport.di;

import com.google.gson.Gson;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class AppModule_ProvideGsonFactory implements Factory<Gson> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final AppModule module;

    public AppModule_ProvideGsonFactory(AppModule appModule) {
        this.module = appModule;
    }

    public Gson get() {
        return (Gson) Preconditions.checkNotNull(this.module.provideGson(), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<Gson> create(AppModule appModule) {
        return new AppModule_ProvideGsonFactory(appModule);
    }

    public static Gson proxyProvideGson(AppModule appModule) {
        return appModule.provideGson();
    }
}
