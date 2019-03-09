package com.sambhar.sambharappreport.page.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.text.TextUtils;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.LoginFormBody;
import com.sambhar.sambharappreport.entity.response.LoginResponse;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class LoginViewModel extends BaseViewModel {
    private final AppRepository mAppRepository;
    private final LiveData<Resource<LoginResponse>> mLoginData;
    private final MutableLiveData<LoginFormBody> mTriggerLogin = new MutableLiveData();
    private final MutableLiveData<ValidatorEntity> mValidEntity = new MutableLiveData();

    @Inject
    LoginViewModel(AppRepository appRepository) {
        this.mAppRepository = appRepository;
        this.mLoginData = Transformations.switchMap(this.mTriggerLogin, new -$$Lambda$LoginViewModel$w3W0K-snYswHdhnjlQT9HMqCqpQ(this));
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<LoginResponse>> login() {
        return this.mLoginData;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ValidatorEntity> getValidateLogin() {
        return this.mValidEntity;
    }

    /* Access modifiers changed, original: protected */
    public void validateLoginForm(LoginFormBody loginFormBody) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setValid(true);
        if (TextUtils.isEmpty(loginFormBody.getPassword())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Password tidak boleh kosong");
        }
        if (TextUtils.isEmpty(loginFormBody.getUsername())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Email tidak boleh kosong");
        }
        this.mValidEntity.setValue(validatorEntity);
    }

    /* Access modifiers changed, original: protected */
    public void doLogin(LoginFormBody loginFormBody) {
        this.mTriggerLogin.setValue(loginFormBody);
    }
}
