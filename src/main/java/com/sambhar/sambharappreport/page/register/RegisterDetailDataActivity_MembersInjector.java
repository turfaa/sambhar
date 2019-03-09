package com.sambhar.sambharappreport.page.register;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class RegisterDetailDataActivity_MembersInjector implements MembersInjector<RegisterDetailDataActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Factory> mViewModelFactoryProvider;

    public RegisterDetailDataActivity_MembersInjector(Provider<Factory> provider) {
        this.mViewModelFactoryProvider = provider;
    }

    public static MembersInjector<RegisterDetailDataActivity> create(Provider<Factory> provider) {
        return new RegisterDetailDataActivity_MembersInjector(provider);
    }

    public void injectMembers(RegisterDetailDataActivity registerDetailDataActivity) {
        if (registerDetailDataActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(registerDetailDataActivity, this.mViewModelFactoryProvider);
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }
}
