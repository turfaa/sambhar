package dagger.internal;

import dagger.Lazy;
import javax.inject.Provider;

public final class DoubleCheck<T> implements Provider<T>, Lazy<T> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Object UNINITIALIZED = new Object();
    private volatile Object instance = UNINITIALIZED;
    private volatile Provider<T> provider;

    private DoubleCheck(Provider<T> provider) {
        this.provider = provider;
    }

    public T get() {
        T t = this.instance;
        if (t == UNINITIALIZED) {
            synchronized (this) {
                t = this.instance;
                if (t == UNINITIALIZED) {
                    t = this.provider.get();
                    T t2 = this.instance;
                    if (t2 != UNINITIALIZED) {
                        if (t2 != t) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Scoped provider was invoked recursively returning different results: ");
                            stringBuilder.append(t2);
                            stringBuilder.append(" & ");
                            stringBuilder.append(t);
                            stringBuilder.append(". This is likely due to a circular dependency.");
                            throw new IllegalStateException(stringBuilder.toString());
                        }
                    }
                    this.instance = t;
                    this.provider = null;
                }
            }
        }
        return t;
    }

    public static <T> Provider<T> provider(Provider<T> provider) {
        Preconditions.checkNotNull(provider);
        if (provider instanceof DoubleCheck) {
            return provider;
        }
        return new DoubleCheck(provider);
    }

    public static <T> Lazy<T> lazy(Provider<T> provider) {
        if (provider instanceof Lazy) {
            return (Lazy) provider;
        }
        return new DoubleCheck((Provider) Preconditions.checkNotNull(provider));
    }
}
