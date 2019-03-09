package com.sambhar.sambharappreport.page.register;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.text.TextUtils;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.RegisterFormBody;
import com.sambhar.sambharappreport.entity.response.RegisterDataResponse;
import com.sambhar.sambharappreport.entity.response.RegisterResponse;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class RegisterViewModel extends BaseViewModel {
    private final AppRepository mAppRepository;
    private final LiveData<Resource<RegisterDataResponse>> mRegisterData;
    private final LiveData<Resource<RegisterResponse>> mRegisterUser;
    private final MutableLiveData<ValidatorEntity> mValidEntity = new MutableLiveData();
    private final MutableLiveData<Boolean> triggerRegisterData = new MutableLiveData();
    private final MutableLiveData<RegisterFormBody> triggerRegisterUser = new MutableLiveData();

    @Inject
    RegisterViewModel(AppRepository appRepository) {
        this.mAppRepository = appRepository;
        this.mRegisterData = Transformations.switchMap(this.triggerRegisterData, new -$$Lambda$RegisterViewModel$TVsASGamCPlf99eG4e7QKt8czRU(this));
        this.mRegisterUser = Transformations.switchMap(this.triggerRegisterUser, new -$$Lambda$RegisterViewModel$0Bc8IS5iGMSajN95L3ZBK0PXhe4(this));
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ValidatorEntity> getValidateRegisterForm() {
        return this.mValidEntity;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<RegisterDataResponse>> registerData() {
        return this.mRegisterData;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<RegisterResponse>> registerUser() {
        return this.mRegisterUser;
    }

    /* Access modifiers changed, original: protected */
    public void validateRegisterForm(RegisterFormBody registerFormBody) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setValid(true);
        if (TextUtils.isEmpty(registerFormBody.getInstagram()) && TextUtils.isEmpty(registerFormBody.getTwitter()) && TextUtils.isEmpty(registerFormBody.getFacebook())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Salah satu akun media sosial harus di isi");
        }
        if (TextUtils.isEmpty(registerFormBody.getGroup())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Group tidak boleh kosong");
        }
        if (TextUtils.isEmpty(registerFormBody.getProvince())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Provinsi tidak boleh kosong");
        }
        if (TextUtils.isEmpty(registerFormBody.getPassword())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Password tidak boleh kosong");
        }
        if (TextUtils.isEmpty(registerFormBody.getUsername())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Nama tidak boleh kosong");
        }
        this.mValidEntity.setValue(validatorEntity);
    }

    /* Access modifiers changed, original: protected */
    public void getRegisterData() {
        this.triggerRegisterData.setValue(Boolean.valueOf(true));
    }

    /* Access modifiers changed, original: protected */
    public void sendRegisterUser(RegisterFormBody registerFormBody) {
        this.triggerRegisterUser.setValue(registerFormBody);
    }
}
