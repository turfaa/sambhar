package com.sambhar.sambharappreport.di;

import android.app.Application;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideContextFactory implements Factory<Context> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Application> applicationProvider;
    private final AppModule module;

    public AppModule_ProvideContextFactory(AppModule appModule, Provider<Application> provider) {
        this.module = appModule;
        this.applicationProvider = provider;
    }

    public Context get() {
        return (Context) Preconditions.checkNotNull(this.module.provideContext((Application) this.applicationProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<Context> create(AppModule appModule, Provider<Application> provider) {
        return new AppModule_ProvideContextFactory(appModule, provider);
    }

    public static Context proxyProvideContext(AppModule appModule, Application application) {
        return appModule.provideContext(application);
    }
}
