package com.sambhar.sambharappreport.di;

import android.app.Application;
import com.sambhar.sambharappreport.SambharApplication;
import com.sambhar.sambharappreport.rest.RestBuilderModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AndroidInjectionModule.class, SubModulesModule.class, AppModule.class, RestBuilderModule.class, ActivityModule.class})
public interface AppComponent {

    @dagger.Component.Builder
    public interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(SambharApplication sambharApplication);
}
