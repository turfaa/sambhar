package com.sambhar.sambharappreport.rest;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppRepository_Factory implements Factory<AppRepository> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRemoteDataSource> appRemoteDataSourceProvider;

    public AppRepository_Factory(Provider<AppRemoteDataSource> provider) {
        this.appRemoteDataSourceProvider = provider;
    }

    public AppRepository get() {
        return new AppRepository((AppRemoteDataSource) this.appRemoteDataSourceProvider.get());
    }

    public static Factory<AppRepository> create(Provider<AppRemoteDataSource> provider) {
        return new AppRepository_Factory(provider);
    }

    public static AppRepository newAppRepository(AppRemoteDataSource appRemoteDataSource) {
        return new AppRepository(appRemoteDataSource);
    }
}
