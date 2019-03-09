package com.sambhar.sambharappreport.di;

import com.readystatesoftware.chuck.ChuckInterceptor;
import com.sambhar.sambharappreport.rest.interceptor.AuthenticationInterceptor;
import com.sambhar.sambharappreport.rest.interceptor.HeaderInterceptor;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

public final class AppModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AuthenticationInterceptor> authenticationInterceptorProvider;
    private final Provider<ChuckInterceptor> chuckInterceptorProvider;
    private final Provider<HeaderInterceptor> headerInterceptorProvider;
    private final AppModule module;

    public AppModule_ProvideOkHttpClientFactory(AppModule appModule, Provider<HeaderInterceptor> provider, Provider<AuthenticationInterceptor> provider2, Provider<ChuckInterceptor> provider3) {
        this.module = appModule;
        this.headerInterceptorProvider = provider;
        this.authenticationInterceptorProvider = provider2;
        this.chuckInterceptorProvider = provider3;
    }

    public OkHttpClient get() {
        return (OkHttpClient) Preconditions.checkNotNull(this.module.provideOkHttpClient((HeaderInterceptor) this.headerInterceptorProvider.get(), (AuthenticationInterceptor) this.authenticationInterceptorProvider.get(), (ChuckInterceptor) this.chuckInterceptorProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<OkHttpClient> create(AppModule appModule, Provider<HeaderInterceptor> provider, Provider<AuthenticationInterceptor> provider2, Provider<ChuckInterceptor> provider3) {
        return new AppModule_ProvideOkHttpClientFactory(appModule, provider, provider2, provider3);
    }

    public static OkHttpClient proxyProvideOkHttpClient(AppModule appModule, HeaderInterceptor headerInterceptor, AuthenticationInterceptor authenticationInterceptor, ChuckInterceptor chuckInterceptor) {
        return appModule.provideOkHttpClient(headerInterceptor, authenticationInterceptor, chuckInterceptor);
    }
}
