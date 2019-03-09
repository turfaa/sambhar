package com.sambhar.sambharappreport.page.changepassword;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.text.TextUtils;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.UpdatePasswordFormBody;
import com.sambhar.sambharappreport.entity.response.GeneralResponse;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class ChangePasswordViewModel extends BaseViewModel {
    private final AppRepository mAppRepository;
    private final LiveData<Resource<GeneralResponse>> mChangePassword;
    private final MutableLiveData<UpdatePasswordFormBody> mTriggerChangePassword = new MutableLiveData();
    private final MutableLiveData<ValidatorEntity> mValidEntity = new MutableLiveData();

    @Inject
    ChangePasswordViewModel(AppRepository appRepository) {
        this.mAppRepository = appRepository;
        this.mChangePassword = Transformations.switchMap(this.mTriggerChangePassword, new -$$Lambda$ChangePasswordViewModel$o3W_64EpmuaVlyNkf-wY94STfKQ(this));
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<GeneralResponse>> changePassword() {
        return this.mChangePassword;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ValidatorEntity> getValidatChangePasswordForm() {
        return this.mValidEntity;
    }

    /* Access modifiers changed, original: protected */
    public void validateChangePasswordForm(UpdatePasswordFormBody updatePasswordFormBody) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setValid(true);
        if (TextUtils.isEmpty(updatePasswordFormBody.getNewPassword())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("New Password tidak boleh kosong");
        }
        if (TextUtils.isEmpty(updatePasswordFormBody.getOldPassword())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Old Password tidak boleh kosong");
        }
        this.mValidEntity.setValue(validatorEntity);
    }

    /* Access modifiers changed, original: protected */
    public void sendChangePassword(UpdatePasswordFormBody updatePasswordFormBody) {
        this.mTriggerChangePassword.setValue(updatePasswordFormBody);
    }
}
