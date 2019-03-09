package com.sambhar.sambharappreport.page.editprofile;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.BaseActivity_MembersInjector;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class EditProfileActivity_MembersInjector implements MembersInjector<EditProfileActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Factory> mViewModelFactoryProvider;

    public EditProfileActivity_MembersInjector(Provider<Factory> provider) {
        this.mViewModelFactoryProvider = provider;
    }

    public static MembersInjector<EditProfileActivity> create(Provider<Factory> provider) {
        return new EditProfileActivity_MembersInjector(provider);
    }

    public void injectMembers(EditProfileActivity editProfileActivity) {
        if (editProfileActivity != null) {
            BaseActivity_MembersInjector.injectMViewModelFactory(editProfileActivity, this.mViewModelFactoryProvider);
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }
}
