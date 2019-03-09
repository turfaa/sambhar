package com.sambhar.sambharappreport.base;

import android.arch.lifecycle.ViewModel;
import dagger.internal.Factory;
import java.util.Map;
import javax.inject.Provider;

public final class ProjectViewModelFactory_Factory implements Factory<ProjectViewModelFactory> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Map<Class<? extends ViewModel>, Provider<ViewModel>>> creatorsProvider;

    public ProjectViewModelFactory_Factory(Provider<Map<Class<? extends ViewModel>, Provider<ViewModel>>> provider) {
        this.creatorsProvider = provider;
    }

    public ProjectViewModelFactory get() {
        return new ProjectViewModelFactory((Map) this.creatorsProvider.get());
    }

    public static Factory<ProjectViewModelFactory> create(Provider<Map<Class<? extends ViewModel>, Provider<ViewModel>>> provider) {
        return new ProjectViewModelFactory_Factory(provider);
    }

    public static ProjectViewModelFactory newProjectViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> map) {
        return new ProjectViewModelFactory(map);
    }
}
