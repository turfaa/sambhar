package dagger.android;

import dagger.internal.Factory;
import java.util.Map;
import javax.inject.Provider;

public final class DispatchingAndroidInjector_Factory<T> implements Factory<DispatchingAndroidInjector<T>> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Map<Class<? extends T>, Provider<AndroidInjector.Factory<? extends T>>>> injectorFactoriesProvider;

    public DispatchingAndroidInjector_Factory(Provider<Map<Class<? extends T>, Provider<AndroidInjector.Factory<? extends T>>>> provider) {
        this.injectorFactoriesProvider = provider;
    }

    public DispatchingAndroidInjector<T> get() {
        return new DispatchingAndroidInjector((Map) this.injectorFactoriesProvider.get());
    }

    public static <T> Factory<DispatchingAndroidInjector<T>> create(Provider<Map<Class<? extends T>, Provider<AndroidInjector.Factory<? extends T>>>> provider) {
        return new DispatchingAndroidInjector_Factory(provider);
    }

    public static <T> DispatchingAndroidInjector<T> newDispatchingAndroidInjector(Map<Class<? extends T>, Provider<AndroidInjector.Factory<? extends T>>> map) {
        return new DispatchingAndroidInjector(map);
    }
}
