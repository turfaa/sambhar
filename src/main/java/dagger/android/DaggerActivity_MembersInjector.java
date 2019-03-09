package dagger.android;

import android.app.Fragment;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class DaggerActivity_MembersInjector implements MembersInjector<DaggerActivity> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<DispatchingAndroidInjector<Fragment>> fragmentInjectorProvider;

    public DaggerActivity_MembersInjector(Provider<DispatchingAndroidInjector<Fragment>> provider) {
        this.fragmentInjectorProvider = provider;
    }

    public static MembersInjector<DaggerActivity> create(Provider<DispatchingAndroidInjector<Fragment>> provider) {
        return new DaggerActivity_MembersInjector(provider);
    }

    public void injectMembers(DaggerActivity daggerActivity) {
        if (daggerActivity != null) {
            daggerActivity.fragmentInjector = (DispatchingAndroidInjector) this.fragmentInjectorProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectFragmentInjector(DaggerActivity daggerActivity, Provider<DispatchingAndroidInjector<Fragment>> provider) {
        daggerActivity.fragmentInjector = (DispatchingAndroidInjector) provider.get();
    }
}
