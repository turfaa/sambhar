package com.sambhar.sambharappreport.page.main;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import com.sambhar.sambharappreport.data.UserSharedPref;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<UserSharedPref> mPrefProvider;
    private final Provider<Factory> mViewModelFactoryProvider;

    public MainActivity_MembersInjector(Provider<Factory> provider, Provider<UserSharedPref> provider2) {
        this.mViewModelFactoryProvider = provider;
        this.mPrefProvider = provider2;
    }

    public static MembersInjector<MainActivity> create(Provider<Factory> provider, Provider<UserSharedPref> provider2) {
        return new MainActivity_MembersInjector(provider, provider2);
    }

    public void injectMembers(MainActivity mainActivity) {
        if (mainActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(mainActivity, this.mViewModelFactoryProvider);
            mainActivity.mPref = (UserSharedPref) this.mPrefProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectMPref(MainActivity mainActivity, Provider<UserSharedPref> provider) {
        mainActivity.mPref = (UserSharedPref) provider.get();
    }
}
