package com.sambhar.sambharappreport.di;

import android.arch.lifecycle.ViewModel;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordActivity;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordViewModel;
import com.sambhar.sambharappreport.page.editprofile.EditProfileActivity;
import com.sambhar.sambharappreport.page.editprofile.EditProfileViewModel;
import com.sambhar.sambharappreport.page.login.LoginActivity;
import com.sambhar.sambharappreport.page.login.LoginViewModel;
import com.sambhar.sambharappreport.page.main.MainActivity;
import com.sambhar.sambharappreport.page.main.MainViewModel;
import com.sambhar.sambharappreport.page.register.RegisterActivity;
import com.sambhar.sambharappreport.page.register.RegisterDetailDataActivity;
import com.sambhar.sambharappreport.page.register.RegisterViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    @ActivityScope
    public abstract ChangePasswordActivity bindChangePasswordActivity();

    @IntoMap
    @Binds
    @ViewModelKey(ChangePasswordViewModel.class)
    public abstract ViewModel bindChangePasswordViewModel(ChangePasswordViewModel changePasswordViewModel);

    @ContributesAndroidInjector
    @ActivityScope
    public abstract EditProfileActivity bindEditProfileActivity();

    @IntoMap
    @Binds
    @ViewModelKey(EditProfileViewModel.class)
    public abstract ViewModel bindEditProfileViewModel(EditProfileViewModel editProfileViewModel);

    @ContributesAndroidInjector
    @ActivityScope
    public abstract LoginActivity bindLoginActivity();

    @IntoMap
    @Binds
    @ViewModelKey(LoginViewModel.class)
    public abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @ContributesAndroidInjector
    @ActivityScope
    public abstract MainActivity bindMainActivity();

    @IntoMap
    @Binds
    @ViewModelKey(MainViewModel.class)
    public abstract ViewModel bindMainViewModel(MainViewModel mainViewModel);

    @ContributesAndroidInjector
    @ActivityScope
    public abstract RegisterActivity bindRegisterActivity();

    @ContributesAndroidInjector
    @ActivityScope
    public abstract RegisterDetailDataActivity bindRegisterDetailDataActivity();

    @IntoMap
    @Binds
    @ViewModelKey(RegisterViewModel.class)
    public abstract ViewModel bindRegisterViewModel(RegisterViewModel registerViewModel);
}
