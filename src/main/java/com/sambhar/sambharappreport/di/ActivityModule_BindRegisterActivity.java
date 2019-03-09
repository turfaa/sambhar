package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.register.RegisterActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {RegisterActivitySubcomponent.class})
public abstract class ActivityModule_BindRegisterActivity {

    @Subcomponent
    @ActivityScope
    public interface RegisterActivitySubcomponent extends AndroidInjector<RegisterActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<RegisterActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(RegisterActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindRegisterActivity() {
    }
}
