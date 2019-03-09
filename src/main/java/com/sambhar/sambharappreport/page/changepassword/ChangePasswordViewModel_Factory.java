package com.sambhar.sambharappreport.page.changepassword;

import com.sambhar.sambharappreport.rest.AppRepository;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class ChangePasswordViewModel_Factory implements Factory<ChangePasswordViewModel> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRepository> appRepositoryProvider;
    private final MembersInjector<ChangePasswordViewModel> changePasswordViewModelMembersInjector;

    public ChangePasswordViewModel_Factory(MembersInjector<ChangePasswordViewModel> membersInjector, Provider<AppRepository> provider) {
        this.changePasswordViewModelMembersInjector = membersInjector;
        this.appRepositoryProvider = provider;
    }

    public ChangePasswordViewModel get() {
        return (ChangePasswordViewModel) MembersInjectors.injectMembers(this.changePasswordViewModelMembersInjector, new ChangePasswordViewModel((AppRepository) this.appRepositoryProvider.get()));
    }

    public static Factory<ChangePasswordViewModel> create(MembersInjector<ChangePasswordViewModel> membersInjector, Provider<AppRepository> provider) {
        return new ChangePasswordViewModel_Factory(membersInjector, provider);
    }
}
