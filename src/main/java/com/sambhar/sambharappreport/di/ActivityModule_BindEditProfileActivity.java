package com.sambhar.sambharappreport.di;

import android.app.Activity;
import com.sambhar.sambharappreport.page.editprofile.EditProfileActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {EditProfileActivitySubcomponent.class})
public abstract class ActivityModule_BindEditProfileActivity {

    @Subcomponent
    @ActivityScope
    public interface EditProfileActivitySubcomponent extends AndroidInjector<EditProfileActivity> {

        @dagger.Subcomponent.Builder
        public static abstract class Builder extends dagger.android.AndroidInjector.Builder<EditProfileActivity> {
        }
    }

    @Binds
    @IntoMap
    @ActivityKey(EditProfileActivity.class)
    public abstract Factory<? extends Activity> bindAndroidInjectorFactory(Builder builder);

    private ActivityModule_BindEditProfileActivity() {
    }
}
