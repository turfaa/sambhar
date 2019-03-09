package com.sambhar.sambharappreport.di;

import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.rest.interceptor.HeaderInterceptor;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideHeaderInterceptorFactory implements Factory<HeaderInterceptor> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final AppModule module;
    private final Provider<UserSharedPref> userUtilsProvider;

    public AppModule_ProvideHeaderInterceptorFactory(AppModule appModule, Provider<UserSharedPref> provider) {
        this.module = appModule;
        this.userUtilsProvider = provider;
    }

    public HeaderInterceptor get() {
        return (HeaderInterceptor) Preconditions.checkNotNull(this.module.provideHeaderInterceptor((UserSharedPref) this.userUtilsProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<HeaderInterceptor> create(AppModule appModule, Provider<UserSharedPref> provider) {
        return new AppModule_ProvideHeaderInterceptorFactory(appModule, provider);
    }

    public static HeaderInterceptor proxyProvideHeaderInterceptor(AppModule appModule, UserSharedPref userSharedPref) {
        return appModule.provideHeaderInterceptor(userSharedPref);
    }
}
