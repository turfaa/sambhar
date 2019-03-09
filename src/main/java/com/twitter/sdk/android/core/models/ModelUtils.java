package com.twitter.sdk.android.core.models;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ModelUtils {
    private ModelUtils() {
    }

    public static <T> List<T> getSafeList(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public static <K, V> Map<K, V> getSafeMap(Map<K, V> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }
}
