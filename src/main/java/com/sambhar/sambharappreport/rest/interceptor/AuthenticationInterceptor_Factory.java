package com.sambhar.sambharappreport.rest.interceptor;

import com.sambhar.sambharappreport.data.UserSharedPref;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AuthenticationInterceptor_Factory implements Factory<AuthenticationInterceptor> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<UserSharedPref> userSharedPrefProvider;

    public AuthenticationInterceptor_Factory(Provider<UserSharedPref> provider) {
        this.userSharedPrefProvider = provider;
    }

    public AuthenticationInterceptor get() {
        return new AuthenticationInterceptor((UserSharedPref) this.userSharedPrefProvider.get());
    }

    public static Factory<AuthenticationInterceptor> create(Provider<UserSharedPref> provider) {
        return new AuthenticationInterceptor_Factory(provider);
    }
}
