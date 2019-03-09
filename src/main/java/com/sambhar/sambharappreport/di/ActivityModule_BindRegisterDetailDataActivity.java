package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.register.RegisterDetailDataActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {RegisterDetailDataActivitySubcomponent.class})
public abstract class ActivityModule_BindRegisterDetailDataActivity {

    @Subcomponent
    @ActivityScope
    public interface RegisterDetailDataActivitySubcomponent extends AndroidInjector<RegisterDetailDataActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<RegisterDetailDataActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(RegisterDetailDataActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindRegisterDetailDataActivity() {
    }
}
