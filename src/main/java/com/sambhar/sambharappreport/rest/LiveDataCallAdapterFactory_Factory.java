package com.sambhar.sambharappreport.rest;

import com.google.gson.Gson;
import dagger.MembersInjector;
import dagger.internal.Factory;
import dagger.internal.MembersInjectors;
import javax.inject.Provider;

public final class LiveDataCallAdapterFactory_Factory implements Factory<LiveDataCallAdapterFactory> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Gson> gsonProvider;
    private final MembersInjector<LiveDataCallAdapterFactory> liveDataCallAdapterFactoryMembersInjector;

    public LiveDataCallAdapterFactory_Factory(MembersInjector<LiveDataCallAdapterFactory> membersInjector, Provider<Gson> provider) {
        this.liveDataCallAdapterFactoryMembersInjector = membersInjector;
        this.gsonProvider = provider;
    }

    public LiveDataCallAdapterFactory get() {
        return (LiveDataCallAdapterFactory) MembersInjectors.injectMembers(this.liveDataCallAdapterFactoryMembersInjector, new LiveDataCallAdapterFactory((Gson) this.gsonProvider.get()));
    }

    public static Factory<LiveDataCallAdapterFactory> create(MembersInjector<LiveDataCallAdapterFactory> membersInjector, Provider<Gson> provider) {
        return new LiveDataCallAdapterFactory_Factory(membersInjector, provider);
    }
}
