package dagger.internal;

import java.lang.ref.WeakReference;
import javax.inject.Provider;

@GwtIncompatible
public final class ReferenceReleasingProvider<T> implements Provider<T> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Object NULL = new Object();
    private final Provider<T> provider;
    private volatile Object strongReference;
    private volatile WeakReference<T> weakReference;

    private ReferenceReleasingProvider(Provider<T> provider) {
        this.provider = provider;
    }

    public void releaseStrongReference() {
        Object obj = this.strongReference;
        if (obj != null && obj != NULL) {
            synchronized (this) {
                this.weakReference = new WeakReference(obj);
                this.strongReference = null;
            }
        }
    }

    public void restoreStrongReference() {
        Object obj = this.strongReference;
        if (this.weakReference != null && obj == null) {
            synchronized (this) {
                obj = this.strongReference;
                if (this.weakReference != null && obj == null) {
                    obj = this.weakReference.get();
                    if (obj != null) {
                        this.strongReference = obj;
                        this.weakReference = null;
                    }
                }
            }
        }
    }

    public T get() {
        T currentValue = currentValue();
        if (currentValue == null) {
            synchronized (this) {
                currentValue = currentValue();
                if (currentValue == null) {
                    currentValue = this.provider.get();
                    if (currentValue == null) {
                        currentValue = NULL;
                    }
                    this.strongReference = currentValue;
                }
            }
        }
        return currentValue == NULL ? null : currentValue;
    }

    private Object currentValue() {
        Object obj = this.strongReference;
        if (obj != null) {
            return obj;
        }
        return this.weakReference != null ? this.weakReference.get() : null;
    }

    public static <T> ReferenceReleasingProvider<T> create(Provider<T> provider, ReferenceReleasingProviderManager referenceReleasingProviderManager) {
        ReferenceReleasingProvider referenceReleasingProvider = new ReferenceReleasingProvider((Provider) Preconditions.checkNotNull(provider));
        referenceReleasingProviderManager.addProvider(referenceReleasingProvider);
        return referenceReleasingProvider;
    }
}
