package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.login.LoginActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {LoginActivitySubcomponent.class})
public abstract class ActivityModule_BindLoginActivity {

    @Subcomponent
    @ActivityScope
    public interface LoginActivitySubcomponent extends AndroidInjector<LoginActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<LoginActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(LoginActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindLoginActivity() {
    }
}
