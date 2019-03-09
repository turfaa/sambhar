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
import javax.inject.Inject;

public class AppRemoteDataSource {
    private final AppRest mAppRest;

    @Inject
    AppRemoteDataSource(AppRest appRest) {
        this.mAppRest = appRest;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<LoginResponse>> login(LoginFormBody loginFormBody) {
        return this.mAppRest.login(loginFormBody);
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<NotifyResponse>> notify(NotifyFormBody notifyFormBody) {
        return this.mAppRest.notify(notifyFormBody);
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<JobCreateResponse>> createJob() {
        return this.mAppRest.createJob();
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<RegisterDataResponse>> getRegisterData() {
        return this.mAppRest.getRegisterData();
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<RegisterResponse>> registerUser(RegisterFormBody registerFormBody) {
        return this.mAppRest.registerUser(registerFormBody);
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<ProfileResponse>> getProfileUser() {
        return this.mAppRest.getProfileUser();
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<GeneralResponse>> updateProfile(EditProfileBodyPost editProfileBodyPost) {
        return this.mAppRest.updateProfile(editProfileBodyPost);
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<GeneralResponse>> updatePassword(UpdatePasswordFormBody updatePasswordFormBody) {
        return this.mAppRest.updatePassword(updatePasswordFormBody);
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ApiResponse<JobCheckResponse>> jobCheckValidation(JobCheckBodyPost jobCheckBodyPost) {
        return this.mAppRest.checkJobValidation(jobCheckBodyPost);
    }
}
