package com.sambhar.sambharappreport.page.register;

import com.sambhar.sambharappreport.rest.AppRepository;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRepository> appRepositoryProvider;
    private final MembersInjector<RegisterViewModel> registerViewModelMembersInjector;

    public RegisterViewModel_Factory(MembersInjector<RegisterViewModel> membersInjector, Provider<AppRepository> provider) {
        this.registerViewModelMembersInjector = membersInjector;
        this.appRepositoryProvider = provider;
    }

    public RegisterViewModel get() {
        return (RegisterViewModel) MembersInjectors.injectMembers(this.registerViewModelMembersInjector, new RegisterViewModel((AppRepository) this.appRepositoryProvider.get()));
    }

    public static Factory<RegisterViewModel> create(MembersInjector<RegisterViewModel> membersInjector, Provider<AppRepository> provider) {
        return new RegisterViewModel_Factory(membersInjector, provider);
    }
}
