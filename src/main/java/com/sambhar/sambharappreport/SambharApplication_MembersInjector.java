package com.sambhar.sambharappreport;

import android.app.Activity;
import dagger.MembersInjector;
import dagger.android.DispatchingAndroidInjector;
import javax.inject.Provider;

public final class SambharApplication_MembersInjector implements MembersInjector<SambharApplication> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<DispatchingAndroidInjector<Activity>> dispatchingAndroidInjectorProvider;

    public SambharApplication_MembersInjector(Provider<DispatchingAndroidInjector<Activity>> provider) {
        this.dispatchingAndroidInjectorProvider = provider;
    }

    public static MembersInjector<SambharApplication> create(Provider<DispatchingAndroidInjector<Activity>> provider) {
        return new SambharApplication_MembersInjector(provider);
    }

    public void injectMembers(SambharApplication sambharApplication) {
        if (sambharApplication != null) {
            sambharApplication.dispatchingAndroidInjector = (DispatchingAndroidInjector) this.dispatchingAndroidInjectorProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectDispatchingAndroidInjector(SambharApplication sambharApplication, Provider<DispatchingAndroidInjector<Activity>> provider) {
        sambharApplication.dispatchingAndroidInjector = (DispatchingAndroidInjector) provider.get();
    }
}
