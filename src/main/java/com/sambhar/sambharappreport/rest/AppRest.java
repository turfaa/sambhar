package com.sambhar.sambharappreport.rest;

import android.arch.lifecycle.LiveData;
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
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AppRest {
    @POST("initiate")
    LiveData<ApiResponse<JobCheckResponse>> checkJobValidation(@Body JobCheckBodyPost jobCheckBodyPost);

    @GET("job/create")
    LiveData<ApiResponse<JobCreateResponse>> createJob();

    @GET("get-profile")
    LiveData<ApiResponse<ProfileResponse>> getProfileUser();

    @GET("groups")
    LiveData<ApiResponse<RegisterDataResponse>> getRegisterData();

    @POST("login")
    LiveData<ApiResponse<LoginResponse>> login(@Body LoginFormBody loginFormBody);

    @POST("notify")
    LiveData<ApiResponse<NotifyResponse>> notify(@Body NotifyFormBody notifyFormBody);

    @POST("register")
    LiveData<ApiResponse<RegisterResponse>> registerUser(@Body RegisterFormBody registerFormBody);

    @POST("update-password")
    LiveData<ApiResponse<GeneralResponse>> updatePassword(@Body UpdatePasswordFormBody updatePasswordFormBody);

    @POST("update-profile")
    LiveData<ApiResponse<GeneralResponse>> updateProfile(@Body EditProfileBodyPost editProfileBodyPost);
}
