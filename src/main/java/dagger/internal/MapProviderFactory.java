package dagger.internal;

import dagger.Lazy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;

public final class MapProviderFactory<K, V> implements Factory<Map<K, Provider<V>>>, Lazy<Map<K, Provider<V>>> {
    private static final MapProviderFactory<Object, Object> EMPTY = new MapProviderFactory(Collections.emptyMap());
    private final Map<K, Provider<V>> contributingMap;

    public static final class Builder<K, V> {
        private final LinkedHashMap<K, Provider<V>> mapBuilder;

        private Builder(int i) {
            this.mapBuilder = DaggerCollections.newLinkedHashMapWithExpectedSize(i);
        }

        public MapProviderFactory<K, V> build() {
            return new MapProviderFactory(this.mapBuilder);
        }

        public Builder<K, V> put(K k, Provider<V> provider) {
            if (k == null) {
                throw new NullPointerException("The key is null");
            } else if (provider != null) {
                this.mapBuilder.put(k, provider);
                return this;
            } else {
                throw new NullPointerException("The provider of the value is null");
            }
        }
    }

    public static <K, V> Builder<K, V> builder(int i) {
        return new Builder(i);
    }

    public static <K, V> MapProviderFactory<K, V> empty() {
        return EMPTY;
    }

    private MapProviderFactory(Map<K, Provider<V>> map) {
        this.contributingMap = Collections.unmodifiableMap(map);
    }

    public Map<K, Provider<V>> get() {
        return this.contributingMap;
    }
}
