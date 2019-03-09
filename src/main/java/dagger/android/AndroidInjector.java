package dagger.android;

import com.google.errorprone.annotations.DoNotMock;
import dagger.BindsInstance;

@DoNotMock("Faked versions of AndroidInjector are much clearer than a mock. See https://google.github.io/dagger/testing")
public interface AndroidInjector<T> {

    @DoNotMock
    public interface Factory<T> {
        AndroidInjector<T> create(T t);
    }

    @DoNotMock
    public static abstract class Builder<T> implements Factory<T> {
        public abstract AndroidInjector<T> build();

        @BindsInstance
        public abstract void seedInstance(T t);

        public final AndroidInjector<T> create(T t) {
            seedInstance(t);
            return build();
        }
    }

    void inject(T t);
}
