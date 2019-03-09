package com.sambhar.sambharappreport.page.editprofile;

import com.sambhar.sambharappreport.rest.AppRepository;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class EditProfileViewModel_Factory implements Factory<EditProfileViewModel> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRepository> appRepositoryProvider;
    private final MembersInjector<EditProfileViewModel> editProfileViewModelMembersInjector;

    public EditProfileViewModel_Factory(MembersInjector<EditProfileViewModel> membersInjector, Provider<AppRepository> provider) {
        this.editProfileViewModelMembersInjector = membersInjector;
        this.appRepositoryProvider = provider;
    }

    public EditProfileViewModel get() {
        return (EditProfileViewModel) MembersInjectors.injectMembers(this.editProfileViewModelMembersInjector, new EditProfileViewModel((AppRepository) this.appRepositoryProvider.get()));
    }

    public static Factory<EditProfileViewModel> create(MembersInjector<EditProfileViewModel> membersInjector, Provider<AppRepository> provider) {
        return new EditProfileViewModel_Factory(membersInjector, provider);
    }
}
