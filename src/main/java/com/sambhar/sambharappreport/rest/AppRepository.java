package com.sambhar.sambharappreport.rest;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import com.sambhar.sambharappreport.entity.bodypost.EditProfileBodyPost;
import com.sambhar.sambharappreport.entity.bodypost.JobCheckBodyPost;
import com.sambhar.sambharappreport.entity.bodypost.LoginFormBody;
import com.sambhar.sambharappreport.entity.bodypost.NotifyFormBody;
import com.sambhar.sambharappreport.entity.bodypost.RegisterFormBody;
import com.sambhar.sambharappreport.entity.bodypost.UpdatePasswordFormBody;
import com.sambhar.sambharappreport.entity.response.GeneralResponse;
import com.sambhar.sambharappreport.entity.response.JobCheckResponse;
import com.sambhar.sambharappreport.entity.response.JobCreateResponse;
import com.sambhar.sambharappreport.entity.response.LoginResponse;
import com.sambhar.sambharappreport.entity.response.NotifyResponse;
import com.sambhar.sambharappreport.entity.response.ProfileResponse;
import com.sambhar.sambharappreport.entity.response.RegisterDataResponse;
import com.sambhar.sambharappreport.entity.response.RegisterResponse;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppRepository {
    private final AppRemoteDataSource mAppRemoteDataSource;

    @Inject
    AppRepository(AppRemoteDataSource appRemoteDataSource) {
        this.mAppRemoteDataSource = appRemoteDataSource;
    }

    public LiveData<Resource<LoginResponse>> login(final LoginFormBody loginFormBody) {
        return new RemoteResource<LoginResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<LoginResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.login(loginFormBody);
            }
        }.asLiveData();
    }

    public LiveData<Resource<NotifyResponse>> notify(final NotifyFormBody notifyFormBody) {
        return new RemoteResource<NotifyResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<NotifyResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.notify(notifyFormBody);
            }
        }.asLiveData();
    }

    public LiveData<Resource<JobCreateResponse>> createJob() {
        return new RemoteResource<JobCreateResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<JobCreateResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.createJob();
            }
        }.asLiveData();
    }

    public LiveData<Resource<RegisterDataResponse>> getRegisterData() {
        return new RemoteResource<RegisterDataResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<RegisterDataResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.getRegisterData();
            }
        }.asLiveData();
    }

    public LiveData<Resource<RegisterResponse>> registerUser(final RegisterFormBody registerFormBody) {
        return new RemoteResource<RegisterResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<RegisterResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.registerUser(registerFormBody);
            }
        }.asLiveData();
    }

    public LiveData<Resource<ProfileResponse>> getProfileUser() {
        return new RemoteResource<ProfileResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<ProfileResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.getProfileUser();
            }
        }.asLiveData();
    }

    public LiveData<Resource<GeneralResponse>> updateProfile(final EditProfileBodyPost editProfileBodyPost) {
        return new RemoteResource<GeneralResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<GeneralResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.updateProfile(editProfileBodyPost);
            }
        }.asLiveData();
    }

    public LiveData<Resource<GeneralResponse>> updatePassword(final UpdatePasswordFormBody updatePasswordFormBody) {
        return new RemoteResource<GeneralResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<GeneralResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.updatePassword(updatePasswordFormBody);
            }
        }.asLiveData();
    }

    public LiveData<Resource<JobCheckResponse>> checkJobValidation(final JobCheckBodyPost jobCheckBodyPost) {
        return new RemoteResource<JobCheckResponse>() {
            /* Access modifiers changed, original: protected */
            @NonNull
            public LiveData<ApiResponse<JobCheckResponse>> createCall() {
                return AppRepository.this.mAppRemoteDataSource.jobCheckValidation(jobCheckBodyPost);
            }
        }.asLiveData();
    }
}
