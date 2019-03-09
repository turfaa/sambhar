package dagger.android.support;

import android.support.v4.app.Fragment;
import dagger.MembersInjector;
import dagger.android.DispatchingAndroidInjector;
import javax.inject.Provider;

public final class DaggerFragment_MembersInjector implements MembersInjector<DaggerFragment> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<DispatchingAndroidInjector<Fragment>> childFragmentInjectorProvider;

    public DaggerFragment_MembersInjector(Provider<DispatchingAndroidInjector<Fragment>> provider) {
        this.childFragmentInjectorProvider = provider;
    }

    public static MembersInjector<DaggerFragment> create(Provider<DispatchingAndroidInjector<Fragment>> provider) {
        return new DaggerFragment_MembersInjector(provider);
    }

    public void injectMembers(DaggerFragment daggerFragment) {
        if (daggerFragment != null) {
            daggerFragment.childFragmentInjector = (DispatchingAndroidInjector) this.childFragmentInjectorProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectChildFragmentInjector(DaggerFragment daggerFragment, Provider<DispatchingAndroidInjector<Fragment>> provider) {
        daggerFragment.childFragmentInjector = (DispatchingAndroidInjector) provider.get();
    }
}
