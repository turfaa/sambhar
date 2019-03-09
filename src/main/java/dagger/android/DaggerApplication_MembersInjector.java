package dagger.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class DaggerApplication_MembersInjector implements MembersInjector<DaggerApplication> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<DispatchingAndroidInjector<Activity>> activityInjectorProvider;
    private final Provider<DispatchingAndroidInjector<BroadcastReceiver>> broadcastReceiverInjectorProvider;
    private final Provider<DispatchingAndroidInjector<ContentProvider>> contentProviderInjectorProvider;
    private final Provider<DispatchingAndroidInjector<Fragment>> fragmentInjectorProvider;
    private final Provider<DispatchingAndroidInjector<Service>> serviceInjectorProvider;

    public DaggerApplication_MembersInjector(Provider<DispatchingAndroidInjector<Activity>> provider, Provider<DispatchingAndroidInjector<BroadcastReceiver>> provider2, Provider<DispatchingAndroidInjector<Fragment>> provider3, Provider<DispatchingAndroidInjector<Service>> provider4, Provider<DispatchingAndroidInjector<ContentProvider>> provider5) {
        this.activityInjectorProvider = provider;
        this.broadcastReceiverInjectorProvider = provider2;
        this.fragmentInjectorProvider = provider3;
        this.serviceInjectorProvider = provider4;
        this.contentProviderInjectorProvider = provider5;
    }

    public static MembersInjector<DaggerApplication> create(Provider<DispatchingAndroidInjector<Activity>> provider, Provider<DispatchingAndroidInjector<BroadcastReceiver>> provider2, Provider<DispatchingAndroidInjector<Fragment>> provider3, Provider<DispatchingAndroidInjector<Service>> provider4, Provider<DispatchingAndroidInjector<ContentProvider>> provider5) {
        return new DaggerApplication_MembersInjector(provider, provider2, provider3, provider4, provider5);
    }

    public void injectMembers(DaggerApplication daggerApplication) {
        if (daggerApplication != null) {
            daggerApplication.activityInjector = (DispatchingAndroidInjector) this.activityInjectorProvider.get();
            daggerApplication.broadcastReceiverInjector = (DispatchingAndroidInjector) this.broadcastReceiverInjectorProvider.get();
            daggerApplication.fragmentInjector = (DispatchingAndroidInjector) this.fragmentInjectorProvider.get();
            daggerApplication.serviceInjector = (DispatchingAndroidInjector) this.serviceInjectorProvider.get();
            daggerApplication.contentProviderInjector = (DispatchingAndroidInjector) this.contentProviderInjectorProvider.get();
            daggerApplication.setInjected();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static void injectActivityInjector(DaggerApplication daggerApplication, Provider<DispatchingAndroidInjector<Activity>> provider) {
        daggerApplication.activityInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectBroadcastReceiverInjector(DaggerApplication daggerApplication, Provider<DispatchingAndroidInjector<BroadcastReceiver>> provider) {
        daggerApplication.broadcastReceiverInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectFragmentInjector(DaggerApplication daggerApplication, Provider<DispatchingAndroidInjector<Fragment>> provider) {
        daggerApplication.fragmentInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectServiceInjector(DaggerApplication daggerApplication, Provider<DispatchingAndroidInjector<Service>> provider) {
        daggerApplication.serviceInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectContentProviderInjector(DaggerApplication daggerApplication, Provider<DispatchingAndroidInjector<ContentProvider>> provider) {
        daggerApplication.contentProviderInjector = (DispatchingAndroidInjector) provider.get();
    }

    public static void injectSetInjected(DaggerApplication daggerApplication) {
        daggerApplication.setInjected();
    }
}
