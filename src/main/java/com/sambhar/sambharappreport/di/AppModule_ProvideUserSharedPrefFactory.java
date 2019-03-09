package com.sambhar.sambharappreport.di;

import android.content.Context;
import com.sambhar.sambharappreport.data.UserSharedPref;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class AppModule_ProvideUserSharedPrefFactory implements Factory<UserSharedPref> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Context> contextProvider;
    private final AppModule module;

    public AppModule_ProvideUserSharedPrefFactory(AppModule appModule, Provider<Context> provider) {
        this.module = appModule;
        this.contextProvider = provider;
    }

    public UserSharedPref get() {
        return (UserSharedPref) Preconditions.checkNotNull(this.module.provideUserSharedPref((Context) this.contextProvider.get()), "Cannot return null from a non-@Nullable @Provides method");
    }

    public static Factory<UserSharedPref> create(AppModule appModule, Provider<Context> provider) {
        return new AppModule_ProvideUserSharedPrefFactory(appModule, provider);
    }

    public static UserSharedPref proxyProvideUserSharedPref(AppModule appModule, Context context) {
        return appModule.provideUserSharedPref(context);
    }
}
