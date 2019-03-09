package com.sambhar.sambharappreport.di;

import android.app.Application;
import com.sambhar.sambharappreport.SambharApplication;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideShambarApplicationFactory implements Factory<SambharApplication> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Application> applicationProvider;
    private final AppModule module;

    public AppModule_ProvideShambarApplicationFactory(AppModule appModule, Provider<Application> provider) {
        this.module = appModule;
        this.applicationProvider = provider;
    }

    public SambharApplication get() {
        return (SambharApplication) Preconditions.checkNotNull(this.module.provideShambarApplication((Application) this.applicationProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<SambharApplication> create(AppModule appModule, Provider<Application> provider) {
        return new AppModule_ProvideShambarApplicationFactory(appModule, provider);
    }

    public static SambharApplication proxyProvideShambarApplication(AppModule appModule, Application application) {
        return appModule.provideShambarApplication(application);
    }
}
