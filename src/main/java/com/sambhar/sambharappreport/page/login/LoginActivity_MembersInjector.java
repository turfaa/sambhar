package com.sambhar.sambharappreport.page.login;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import com.sambhar.sambharappreport.data.UserSharedPref;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class LoginActivity_MembersInjector implements MembersInjector<LoginActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<UserSharedPref> mPrefProvider;
    private final Provider<Factory> mViewModelFactoryProvider;

    public LoginActivity_MembersInjector(Provider<Factory> provider, Provider<UserSharedPref> provider2) {
        this.mViewModelFactoryProvider = provider;
        this.mPrefProvider = provider2;
    }

    public static MembersInjector<LoginActivity> create(Provider<Factory> provider, Provider<UserSharedPref> provider2) {
        return new LoginActivity_MembersInjector(provider, provider2);
    }

    public void injectMembers(LoginActivity loginActivity) {
        if (loginActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(loginActivity, this.mViewModelFactoryProvider);
            loginActivity.mPref = (UserSharedPref) this.mPrefProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectMPref(LoginActivity loginActivity, Provider<UserSharedPref> provider) {
        loginActivity.mPref = (UserSharedPref) provider.get();
    }
}
