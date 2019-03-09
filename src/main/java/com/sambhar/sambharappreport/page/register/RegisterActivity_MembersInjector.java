package com.sambhar.sambharappreport.page.register;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class RegisterActivity_MembersInjector implements MembersInjector<RegisterActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Factory> mViewModelFactoryProvider;

    public RegisterActivity_MembersInjector(Provider<Factory> provider) {
        this.mViewModelFactoryProvider = provider;
    }

    public static MembersInjector<RegisterActivity> create(Provider<Factory> provider) {
        return new RegisterActivity_MembersInjector(provider);
    }

    public void injectMembers(RegisterActivity registerActivity) {
        if (registerActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(registerActivity, this.mViewModelFactoryProvider);
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }
}
