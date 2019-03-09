package dagger.internal;

import dagger.MembersInjector;

public final class MembersInjectors {

    private enum NoOpMembersInjector implements MembersInjector<Object> {
        INSTANCE;

        public void injectMembers(Object obj) {
            Preconditions.checkNotNull(obj);
        }
    }

    public static <T> T injectMembers(MembersInjector<T> membersInjector, T t) {
        membersInjector.injectMembers(t);
        return t;
    }

    public static <T> MembersInjector<T> noOp() {
        return NoOpMembersInjector.INSTANCE;
    }

    public static <T> MembersInjector<T> delegatingTo(MembersInjector<? super T> membersInjector) {
        return (MembersInjector) Preconditions.checkNotNull(membersInjector);
    }

    private MembersInjectors() {
    }
}
