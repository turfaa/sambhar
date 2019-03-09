package com.sambhar.sambharappreport.di;

import android.arch.lifecycle.ViewModelProvider.Factory;
import com.sambhar.sambharappreport.base.ProjectViewModelFactory;
import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

@Module
public abstract class BaseModule {
    @Singleton
    @Binds
    public abstract Factory bindViewModelFactory(ProjectViewModelFactory projectViewModelFactory);
}
