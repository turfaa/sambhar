package com.sambhar.sambharappreport.rest;

import android.arch.lifecycle.LiveData;
import com.google.gson.Gson;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.CallAdapter;
import retrofit2.CallAdapter.Factory;
import retrofit2.Retrofit;

@Singleton
public class LiveDataCallAdapterFactory extends Factory {
    private final Gson mGson;

    @Inject
    public LiveDataCallAdapterFactory(Gson gson) {
        this.mGson = gson;
    }

    @Nullable
    public CallAdapter<?, ?> get(Type type, Annotation[] annotationArr, Retrofit retrofit) {
        if (Factory.getRawType(type) != LiveData.class) {
            return null;
        }
        type = Factory.getParameterUpperBound(0, (ParameterizedType) type);
        if (Factory.getRawType(type) != ApiResponse.class) {
            throw new IllegalArgumentException("type must be a resource");
        } else if (type instanceof ParameterizedType) {
            return new LiveDataCallAdapter(Factory.getParameterUpperBound(0, (ParameterizedType) type), this.mGson);
        } else {
            throw new IllegalArgumentException("resource must be parameterized");
        }
    }
}
