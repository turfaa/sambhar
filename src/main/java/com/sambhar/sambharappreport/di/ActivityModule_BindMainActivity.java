package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.main.MainActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {MainActivitySubcomponent.class})
public abstract class ActivityModule_BindMainActivity {

    @Subcomponent
    @ActivityScope
    public interface MainActivitySubcomponent extends AndroidInjector<MainActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<MainActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(MainActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindMainActivity() {
    }
}
