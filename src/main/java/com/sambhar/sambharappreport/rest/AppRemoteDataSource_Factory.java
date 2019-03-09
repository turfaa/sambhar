package com.sambhar.sambharappreport.rest;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppRemoteDataSource_Factory implements Factory<AppRemoteDataSource> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<AppRest> appRestProvider;

    public AppRemoteDataSource_Factory(Provider<AppRest> provider) {
        this.appRestProvider = provider;
    }

    public AppRemoteDataSource get() {
        return new AppRemoteDataSource((AppRest) this.appRestProvider.get());
    }

    public static Factory<AppRemoteDataSource> create(Provider<AppRest> provider) {
        return new AppRemoteDataSource_Factory(provider);
    }

    public static AppRemoteDataSource newAppRemoteDataSource(AppRest appRest) {
        return new AppRemoteDataSource(appRest);
    }
}
