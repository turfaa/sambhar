package com.sambhar.sambharappreport.page.changepassword;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class ChangePasswordActivity_MembersInjector implements MembersInjector<ChangePasswordActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Factory> mViewModelFactoryProvider;

    public ChangePasswordActivity_MembersInjector(Provider<Factory> provider) {
        this.mViewModelFactoryProvider = provider;
    }

    public static MembersInjector<ChangePasswordActivity> create(Provider<Factory> provider) {
        return new ChangePasswordActivity_MembersInjector(provider);
    }

    public void injectMembers(ChangePasswordActivity changePasswordActivity) {
        if (changePasswordActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(changePasswordActivity, this.mViewModelFactoryProvider);
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }
}
