package dagger.android.support;

import android.app.Fragment;
import dagger.MembersInjector;
import dagger.android.DispatchingAndroidInjector;
import javax.inject.Provider;

public final class DaggerAppCompatActivity_MembersInjector implements MembersInjector<DaggerAppCompatActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<DispatchingAndroidInjector<Fragment>> frameworkFragmentInjectorProvider;
    private final Provider<DispatchingAndroidInjector<android.support.v4.app.Fragment>> supportFragmentInjectorProvider;

    public DaggerAppCompatActivity_MembersInjector(Provider<DispatchingAndroidInjector<android.support.v4.app.Fragment>> provider, Provider<DispatchingAndroidInjector<Fragment>> provider2) {
        this.supportFragmentInjectorProvider = provider;
        this.frameworkFragmentInjectorProvider = provider2;
    }

    public static MembersInjector<DaggerAppCompatActivity> create(Provider<DispatchingAndroidInjector<android.support.v4.app.Fragment>> provider, Provider<DispatchingAndroidInjector<Fragment>> provider2) {
        return new DaggerAppCompatActivity_MembersInjector(provider, provider2);
    }

    public void injectMembers(DaggerAppCompatActivity daggerAppCompatActivity) {
        if (daggerAppCompatActivity != null) {
            daggerAppCompatActivity.supportFragmentInjector = (DispatchingAndroidInjector) this.supportFragmentInjectorProvider.get();
            daggerAppCompatActivity.frameworkFragmentInjector = (DispatchingAndroidInjector) this.frameworkFragmentInjectorProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectSupportFragmentInjector(DaggerAppCompatActivity daggerAppCompatActivity, Provider<DispatchingAndroidInjector<android.support.v4.app.Fragment>> provider) {
        daggerAppCompatActivity.supportFragmentInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectFrameworkFragmentInjector(DaggerAppCompatActivity daggerAppCompatActivity, Provider<DispatchingAndroidInjector<Fragment>> provider) {
        daggerAppCompatActivity.frameworkFragmentInjector = (DispatchingAndroidInjector) provider.get();
    }
}
