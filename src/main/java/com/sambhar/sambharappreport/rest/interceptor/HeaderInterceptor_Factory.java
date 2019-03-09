package com.sambhar.sambharappreport.rest.interceptor;

import com.sambhar.sambharappreport.data.UserSharedPref;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HeaderInterceptor_Factory implements Factory<HeaderInterceptor> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<UserSharedPref> userUtilsProvider;

    public HeaderInterceptor_Factory(Provider<UserSharedPref> provider) {
        this.userUtilsProvider = provider;
    }

    public HeaderInterceptor get() {
        return new HeaderInterceptor((UserSharedPref) this.userUtilsProvider.get());
    }

    public static Factory<HeaderInterceptor> create(Provider<UserSharedPref> provider) {
        return new HeaderInterceptor_Factory(provider);
    }
}
