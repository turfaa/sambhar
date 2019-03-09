package com.sambhar.sambharappreport.page.editprofile;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.text.TextUtils;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.EditProfileBodyPost;
import com.sambhar.sambharappreport.entity.response.GeneralResponse;
import com.sambhar.sambharappreport.entity.response.ProfileResponse;
import com.sambhar.sambharappreport.entity.response.RegisterDataResponse;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class EditProfileViewModel extends BaseViewModel {
    private final AppRepository mAppRepository;
    private final LiveData<Resource<RegisterDataResponse>> mCompanyData;
    private final LiveData<Resource<ProfileResponse>> mProfileData;
    private final MutableLiveData<Boolean> mTriggerCompanyData = new MutableLiveData();
    private final MutableLiveData<Boolean> mTriggerProfileUser = new MutableLiveData();
    private final MutableLiveData<EditProfileBodyPost> mTriggerUpdateProfile = new MutableLiveData();
    private final LiveData<Resource<GeneralResponse>> mUpdateProfileData;
    private final MutableLiveData<ValidatorEntity> mValidEntity = new MutableLiveData();

    @Inject
    EditProfileViewModel(AppRepository appRepository) {
        this.mAppRepository = appRepository;
        this.mProfileData = Transformations.switchMap(this.mTriggerProfileUser, new -$$Lambda$EditProfileViewModel$w9SO8jxN9GgZTCw8cRbg59aiS3w(this));
        this.mCompanyData = Transformations.switchMap(this.mTriggerCompanyData, new -$$Lambda$EditProfileViewModel$1X1GxQlMMby8QSBMtJpK0AcurAw(this));
        this.mUpdateProfileData = Transformations.switchMap(this.mTriggerUpdateProfile, new -$$Lambda$EditProfileViewModel$53-aZuc7YRpNNmWlvbOSZvN0YcA(this));
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<ProfileResponse>> profileUserData() {
        return this.mProfileData;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<RegisterDataResponse>> companyData() {
        return this.mCompanyData;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<ValidatorEntity> getValidateEditProfileForm() {
        return this.mValidEntity;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<GeneralResponse>> updateProfileData() {
        return this.mUpdateProfileData;
    }

    /* Access modifiers changed, original: protected */
    public void validateRegisterForm(EditProfileBodyPost editProfileBodyPost) {
        ValidatorEntity validatorEntity = new ValidatorEntity();
        validatorEntity.setValid(true);
        if (TextUtils.isEmpty(editProfileBodyPost.getInstagramAccount()) && TextUtils.isEmpty(editProfileBodyPost.getTwitterAccount()) && TextUtils.isEmpty(editProfileBodyPost.getFbAccount())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Salah satu akun media sosial harus di isi");
        }
        if (TextUtils.isEmpty(editProfileBodyPost.getGroup())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Group tidak boleh kosong");
        }
        if (TextUtils.isEmpty(editProfileBodyPost.getProvince())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Provinsi tidak boleh kosong");
        }
        if (TextUtils.isEmpty(editProfileBodyPost.getUsername())) {
            validatorEntity.setValid(false);
            validatorEntity.setMessage("Nama tidak boleh kosong");
        }
        this.mValidEntity.setValue(validatorEntity);
    }

    /* Access modifiers changed, original: protected */
    public void getProfileUserData() {
        this.mTriggerProfileUser.setValue(Boolean.valueOf(true));
    }

    /* Access modifiers changed, original: protected */
    public void getCompanyData() {
        this.mTriggerCompanyData.setValue(Boolean.valueOf(true));
    }

    /* Access modifiers changed, original: protected */
    public void sendUpdateProfile(EditProfileBodyPost editProfileBodyPost) {
        this.mTriggerUpdateProfile.setValue(editProfileBodyPost);
    }
}
