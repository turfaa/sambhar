package com.sambhar.sambharappreport.data;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UserSharedPref_Factory implements Factory<UserSharedPref> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Context> contextProvider;

    public UserSharedPref_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    public UserSharedPref get() {
        return new UserSharedPref((Context) this.contextProvider.get());
    }

    public static Factory<UserSharedPref> create(Provider<Context> provider) {
        return new UserSharedPref_Factory(provider);
    }
}
