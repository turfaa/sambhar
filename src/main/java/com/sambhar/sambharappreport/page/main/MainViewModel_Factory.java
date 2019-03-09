package com.sambhar.sambharappreport.page.main;

import com.sambhar.sambharappreport.rest.AppRepository;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class MainViewModel_Factory implements Factory<MainViewModel> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRepository> appRepositoryProvider;
    private final MembersInjector<MainViewModel> mainViewModelMembersInjector;

    public MainViewModel_Factory(MembersInjector<MainViewModel> membersInjector, Provider<AppRepository> provider) {
        this.mainViewModelMembersInjector = membersInjector;
        this.appRepositoryProvider = provider;
    }

    public MainViewModel get() {
        return (MainViewModel) MembersInjectors.injectMembers(this.mainViewModelMembersInjector, new MainViewModel((AppRepository) this.appRepositoryProvider.get()));
    }

    public static Factory<MainViewModel> create(MembersInjector<MainViewModel> membersInjector, Provider<AppRepository> provider) {
        return new MainViewModel_Factory(membersInjector, provider);
    }
}
