package com.sambhar.sambharappreport.page.login;

import com.sambhar.sambharappreport.rest.AppRepository;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRepository> appRepositoryProvider;
    private final MembersInjector<LoginViewModel> loginViewModelMembersInjector;

    public LoginViewModel_Factory(MembersInjector<LoginViewModel> membersInjector, Provider<AppRepository> provider) {
        this.loginViewModelMembersInjector = membersInjector;
        this.appRepositoryProvider = provider;
    }

    public LoginViewModel get() {
        return (LoginViewModel) MembersInjectors.injectMembers(this.loginViewModelMembersInjector, new LoginViewModel((AppRepository) this.appRepositoryProvider.get()));
    }

    public static Factory<LoginViewModel> create(MembersInjector<LoginViewModel> membersInjector, Provider<AppRepository> provider) {
        return new LoginViewModel_Factory(membersInjector, provider);
    }
}
