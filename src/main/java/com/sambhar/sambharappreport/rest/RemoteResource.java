package com.sambhar.sambharappreport.rest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

public abstract class RemoteResource<Response> {
    private final MediatorLiveData<Resource<Response>> mResponse = new MediatorLiveData();

    @MainThread
    @NonNull
    public abstract LiveData<ApiResponse<Response>> createCall();

    @MainThread
    protected RemoteResource() {
        this.mResponse.setValue(Resource.loading(null));
        this.mResponse.addSource(createCall(), new -$$Lambda$RemoteResource$g-3kWAKV_t2VcMupspS5IKY6H3g(this));
    }

    public static /* synthetic */ void lambda$new$0(RemoteResource remoteResource, ApiResponse apiResponse) {
        if (apiResponse.isSuccessful()) {
            remoteResource.setValue(Resource.success(apiResponse.getBody(), apiResponse.getStatusCode()));
        } else if (apiResponse.getNetworkCode() == 0) {
            remoteResource.setValue(Resource.error(apiResponse.getNetworkCode(), "Maaf, gagal menghubungi server. silahkan cek koneksi internet anda"));
        } else {
            remoteResource.setValue(Resource.error(apiResponse.getNetworkCode(), apiResponse.getErrorMessage()));
        }
    }

    @MainThread
    private void setValue(Resource<Response> resource) {
        if (!equals(this.mResponse.getValue(), resource)) {
            this.mResponse.setValue(resource);
        }
    }

    private static boolean equals(Object obj, Object obj2) {
        return obj == obj2 || (obj != null && obj.equals(obj2));
    }

    public LiveData<Resource<Response>> asLiveData() {
        return this.mResponse;
    }
}
