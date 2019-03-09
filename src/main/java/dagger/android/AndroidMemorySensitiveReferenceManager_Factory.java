package dagger.android;

import dagger.internal.Factory;
import dagger.internal.GwtIncompatible;
import dagger.releasablereferences.TypedReleasableReferenceManager;
import java.util.Set;
import javax.inject.Provider;

@GwtIncompatible
public final class AndroidMemorySensitiveReferenceManager_Factory implements Factory<AndroidMemorySensitiveReferenceManager> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Set<TypedReleasableReferenceManager<ReleaseReferencesAt>>> managersProvider;

    public AndroidMemorySensitiveReferenceManager_Factory(Provider<Set<TypedReleasableReferenceManager<ReleaseReferencesAt>>> provider) {
        this.managersProvider = provider;
    }

    public AndroidMemorySensitiveReferenceManager get() {
        return new AndroidMemorySensitiveReferenceManager((Set) this.managersProvider.get());
    }

    public static Factory<AndroidMemorySensitiveReferenceManager> create(Provider<Set<TypedReleasableReferenceManager<ReleaseReferencesAt>>> provider) {
        return new AndroidMemorySensitiveReferenceManager_Factory(provider);
    }

    public static AndroidMemorySensitiveReferenceManager newAndroidMemorySensitiveReferenceManager(Set<TypedReleasableReferenceManager<ReleaseReferencesAt>> set) {
        return new AndroidMemorySensitiveReferenceManager(set);
    }
}
