package com.sambhar.sambharappreport.di;

import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.rest.interceptor.AuthenticationInterceptor;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideAuthenticationInterceptorFactory implements Factory<AuthenticationInterceptor> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final AppModule module;
    private final Provider<UserSharedPref> userSharedPrefProvider;

    public AppModule_ProvideAuthenticationInterceptorFactory(AppModule appModule, Provider<UserSharedPref> provider) {
        this.module = appModule;
        this.userSharedPrefProvider = provider;
    }

    public AuthenticationInterceptor get() {
        return (AuthenticationInterceptor) Preconditions.checkNotNull(this.module.provideAuthenticationInterceptor((UserSharedPref) this.userSharedPrefProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<AuthenticationInterceptor> create(AppModule appModule, Provider<UserSharedPref> provider) {
        return new AppModule_ProvideAuthenticationInterceptorFactory(appModule, provider);
    }

    public static AuthenticationInterceptor proxyProvideAuthenticationInterceptor(AppModule appModule, UserSharedPref userSharedPref) {
        return appModule.provideAuthenticationInterceptor(userSharedPref);
    }
}
