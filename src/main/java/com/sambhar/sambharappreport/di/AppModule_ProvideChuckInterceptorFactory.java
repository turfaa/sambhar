package com.sambhar.sambharappreport.di;

import android.content.Context;
import com.readystatesoftware.chuck.ChuckInterceptor;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideChuckInterceptorFactory implements Factory<ChuckInterceptor> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Context> contextProvider;
    private final AppModule module;

    public AppModule_ProvideChuckInterceptorFactory(AppModule appModule, Provider<Context> provider) {
        this.module = appModule;
        this.contextProvider = provider;
    }

    public ChuckInterceptor get() {
        return (ChuckInterceptor) Preconditions.checkNotNull(this.module.provideChuckInterceptor((Context) this.contextProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<ChuckInterceptor> create(AppModule appModule, Provider<Context> provider) {
        return new AppModule_ProvideChuckInterceptorFactory(appModule, provider);
    }

    public static ChuckInterceptor proxyProvideChuckInterceptor(AppModule appModule, Context context) {
        return appModule.provideChuckInterceptor(context);
    }
}
