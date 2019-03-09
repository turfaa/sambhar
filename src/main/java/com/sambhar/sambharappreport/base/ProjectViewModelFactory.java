package com.sambhar.sambharappreport.base;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.support.annotation.NonNull;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Provider;

public class ProjectViewModelFactory implements Factory {
    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> mCreators;

    @Inject
    ProjectViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> map) {
        this.mCreators = map;
    }

    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> cls) {
        Provider provider = (Provider) this.mCreators.get(cls);
        if (provider == null) {
            for (Entry entry : this.mCreators.entrySet()) {
                if (cls.isAssignableFrom((Class) entry.getKey())) {
                    provider = (Provider) entry.getValue();
                    break;
                }
            }
        }
        if (provider != null) {
            try {
                return (ViewModel) provider.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unknown model class ");
        stringBuilder.append(cls);
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
