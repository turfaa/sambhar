package dagger.android;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.android.AndroidInjector.Factory;
import dagger.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

public final class DispatchingAndroidInjector<T> implements AndroidInjector<T> {
    private static final String NO_SUPERTYPES_BOUND_FORMAT = "No injector factory bound for Class<%s>";
    private static final String SUPERTYPES_BOUND_FORMAT = "No injector factory bound for Class<%1$s>. Injector factories were bound for supertypes of %1$s: %2$s. Did you mean to bind an injector factory for the subtype?";
    private final Map<Class<? extends T>, Provider<Factory<? extends T>>> injectorFactories;

    public static final class InvalidInjectorBindingException extends RuntimeException {
        InvalidInjectorBindingException(String str, ClassCastException classCastException) {
            super(str, classCastException);
        }
    }

    @Inject
    DispatchingAndroidInjector(Map<Class<? extends T>, Provider<Factory<? extends T>>> map) {
        this.injectorFactories = map;
    }

    @CanIgnoreReturnValue
    public boolean maybeInject(T t) {
        Provider provider = (Provider) this.injectorFactories.get(t.getClass());
        if (provider == null) {
            return false;
        }
        Factory factory = (Factory) provider.get();
        try {
            ((AndroidInjector) Preconditions.checkNotNull(factory.create(t), "%s.create(I) should not return null.", factory.getClass().getCanonicalName())).inject(t);
            return true;
        } catch (ClassCastException e) {
            throw new InvalidInjectorBindingException(String.format("%s does not implement AndroidInjector.Factory<%s>", new Object[]{factory.getClass().getCanonicalName(), t.getClass().getCanonicalName()}), e);
        }
    }

    public void inject(T t) {
        if (!maybeInject(t)) {
            throw new IllegalArgumentException(errorMessageSuggestions(t));
        }
    }

    private String errorMessageSuggestions(T t) {
        ArrayList arrayList = new ArrayList();
        for (Class cls : this.injectorFactories.keySet()) {
            if (cls.isInstance(t)) {
                arrayList.add(cls.getCanonicalName());
            }
        }
        Collections.sort(arrayList);
        return String.format(arrayList.isEmpty() ? NO_SUPERTYPES_BOUND_FORMAT : SUPERTYPES_BOUND_FORMAT, new Object[]{t.getClass().getCanonicalName(), arrayList});
    }
}
