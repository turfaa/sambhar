package dagger.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Provider;

public final class SetFactory<T> implements Factory<Set<T>> {
    private static final Factory<Set<Object>> EMPTY_FACTORY = InstanceFactory.create(Collections.emptySet());
    private final List<Provider<Collection<T>>> collectionProviders;
    private final List<Provider<T>> individualProviders;

    public static final class Builder<T> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final List<Provider<Collection<T>>> collectionProviders;
        private final List<Provider<T>> individualProviders;

        static {
            Class cls = SetFactory.class;
        }

        private Builder(int i, int i2) {
            this.individualProviders = DaggerCollections.presizedList(i);
            this.collectionProviders = DaggerCollections.presizedList(i2);
        }

        public Builder<T> addProvider(Provider<? extends T> provider) {
            this.individualProviders.add(provider);
            return this;
        }

        public Builder<T> addCollectionProvider(Provider<? extends Collection<? extends T>> provider) {
            this.collectionProviders.add(provider);
            return this;
        }

        public SetFactory<T> build() {
            return new SetFactory(this.individualProviders, this.collectionProviders);
        }
    }

    public static <T> Factory<Set<T>> empty() {
        return EMPTY_FACTORY;
    }

    public static <T> Builder<T> builder(int i, int i2) {
        return new Builder(i, i2);
    }

    private SetFactory(List<Provider<T>> list, List<Provider<Collection<T>>> list2) {
        this.individualProviders = list;
        this.collectionProviders = list2;
    }

    public Set<T> get() {
        int size = this.individualProviders.size();
        ArrayList arrayList = new ArrayList(this.collectionProviders.size());
        int size2 = this.collectionProviders.size();
        int i = size;
        for (size = 0; size < size2; size++) {
            Collection collection = (Collection) ((Provider) this.collectionProviders.get(size)).get();
            i += collection.size();
            arrayList.add(collection);
        }
        HashSet newHashSetWithExpectedSize = DaggerCollections.newHashSetWithExpectedSize(i);
        size2 = this.individualProviders.size();
        for (i = 0; i < size2; i++) {
            newHashSetWithExpectedSize.add(Preconditions.checkNotNull(((Provider) this.individualProviders.get(i)).get()));
        }
        size2 = arrayList.size();
        for (int i2 = 0; i2 < size2; i2++) {
            for (Object checkNotNull : (Collection) arrayList.get(i2)) {
                newHashSetWithExpectedSize.add(Preconditions.checkNotNull(checkNotNull));
            }
        }
        return Collections.unmodifiableSet(newHashSetWithExpectedSize);
    }
}
