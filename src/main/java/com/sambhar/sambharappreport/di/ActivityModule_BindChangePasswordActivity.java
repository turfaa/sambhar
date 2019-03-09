package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {ChangePasswordActivitySubcomponent.class})
public abstract class ActivityModule_BindChangePasswordActivity {

    @Subcomponent
    @ActivityScope
    public interface ChangePasswordActivitySubcomponent extends AndroidInjector<ChangePasswordActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<ChangePasswordActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(ChangePasswordActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindChangePasswordActivity() {
    }
}
