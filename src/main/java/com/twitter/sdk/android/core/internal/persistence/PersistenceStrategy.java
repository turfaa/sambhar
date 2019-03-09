package com.twitter.sdk.android.core.internal.persistence;

public interface PersistenceStrategy<T> {
    void clear();

    T restore();

    void save(T t);
}
