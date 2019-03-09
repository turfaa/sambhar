package com.sambhar.sambharappreport.page.editprofile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.data.SambharConstant;
import com.sambhar.sambharappreport.databinding.ActivityEditProfileBinding;
import com.sambhar.sambharappreport.entity.GroupEntity;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.EditProfileBodyPost;
import com.sambhar.sambharappreport.entity.response.ProfileResponse;
import com.sambhar.sambharappreport.entity.response.RegisterDataResponse;
import com.sambhar.sambharappreport.page.register.RegisterDetailDataActivity;
import com.sambhar.sambharappreport.rest.Resource;
import java.util.ArrayList;
import java.util.Iterator;

public class EditProfileActivity extends BaseActivity<EditProfileViewModel, ActivityEditProfileBinding> {
    public static String GROUP_DATA = "group_data";
    private static int GROUP_REQUEST_CODE = 200;
    public static String PROVINCE_DATA = "province_data";
    private static int PROVINCE_REQUEST_CODE = 100;
    private EditProfileBodyPost body;
    private ArrayList<GroupEntity> companyList;
    private int groupId;
    private ArrayList<GroupEntity> groupList;
    private int provinceId;

    public int setLayoutView() {
        return R.layout.activity_edit_profile;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.companyList = new ArrayList();
        this.groupList = new ArrayList();
        setupActionBar("Edit profile", true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setElevation(0.0f);
        }
        ((ActivityEditProfileBinding) getDataBinding()).tvEditProfileTitleFacebook.setText("@");
        ((ActivityEditProfileBinding) getDataBinding()).tvEditprofileTitleInstagram.setText("@");
        ((ActivityEditProfileBinding) getDataBinding()).tvEditprofileTitleTwitter.setText("@");
        ((EditProfileViewModel) getViewModel()).profileUserData().observe(this, new -$$Lambda$EditProfileActivity$yrc08Qfp6ZlO9Q4KeU0NSlCNOiU(this));
        ((EditProfileViewModel) getViewModel()).getProfileUserData();
        ((EditProfileViewModel) getViewModel()).companyData().observe(this, new -$$Lambda$EditProfileActivity$gPcn4UgP_8lGiJi5WvMvhM7C7-Q(this));
        ((ActivityEditProfileBinding) getDataBinding()).etEditprofileProvince.setOnClickListener(new -$$Lambda$EditProfileActivity$xr6x6PI7RyZJvQ-70tWNJvUJqko(this));
        ((ActivityEditProfileBinding) getDataBinding()).etEditprofileGroup.setOnClickListener(new -$$Lambda$EditProfileActivity$1eWhIjOV4xiDOJ7di58IH-l7kZ0(this));
        ((EditProfileViewModel) getViewModel()).getValidateEditProfileForm().observe(this, new -$$Lambda$EditProfileActivity$DEY2_VR5lNCxREoi28ren8y8L8A(this));
        ((ActivityEditProfileBinding) getDataBinding()).btUpdate.setOnClickListener(new -$$Lambda$EditProfileActivity$oRlAGyFasdl_qdX1AnSKIUWiIrA(this));
        ((EditProfileViewModel) getViewModel()).updateProfileData().observe(this, new -$$Lambda$EditProfileActivity$OVJQL2spHEWQRtsbzsJwD9nbxg0(this));
        ((ActivityEditProfileBinding) getDataBinding()).btToChangePassword.setOnClickListener(new -$$Lambda$EditProfileActivity$Ffqb2O9RbOqdMYRzWdn8dtj-TZA(this));
    }

    public static /* synthetic */ void lambda$onCreate$0(EditProfileActivity editProfileActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                editProfileActivity.showLoading(editProfileActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                editProfileActivity.dismissLoading();
                return;
            case SUCCESS:
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileUsername.setText(((ProfileResponse) resource.data).getProfileEntity().getUsername());
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileProvince.setText(((ProfileResponse) resource.data).getProfileEntity().getCompanies());
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileGroup.setText(((ProfileResponse) resource.data).getProfileEntity().getGroup());
                String replace = ((ProfileResponse) resource.data).getProfileEntity().getFacebookAccount().replace("@", "");
                String replace2 = ((ProfileResponse) resource.data).getProfileEntity().getTwitterAccount().replace("@", "");
                String replace3 = ((ProfileResponse) resource.data).getProfileEntity().getInstagramAccount().replace("@", "");
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileFacebook.setText(replace);
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileTwitter.setText(replace2);
                ((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileInstagram.setText(replace3);
                editProfileActivity.dismissLoading();
                ((EditProfileViewModel) editProfileActivity.getViewModel()).getCompanyData();
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$1(EditProfileActivity editProfileActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                editProfileActivity.showLoading(editProfileActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                editProfileActivity.dismissLoading();
                return;
            case SUCCESS:
                editProfileActivity.dismissLoading();
                editProfileActivity.companyList = ((RegisterDataResponse) resource.data).getEntity().getCompanyEntityList();
                editProfileActivity.groupList = ((RegisterDataResponse) resource.data).getEntity().getGroupEntityList();
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$2(EditProfileActivity editProfileActivity, View view) {
        Intent intent = new Intent(editProfileActivity, RegisterDetailDataActivity.class);
        intent.putParcelableArrayListExtra(RegisterDetailDataActivity.LIST_DATA, editProfileActivity.companyList);
        intent.putExtra(RegisterDetailDataActivity.PICK_MODE, SambharConstant.PICK_MODE_PROVINCE);
        editProfileActivity.startActivityForResult(intent, PROVINCE_REQUEST_CODE);
    }

    public static /* synthetic */ void lambda$onCreate$3(EditProfileActivity editProfileActivity, View view) {
        if (!TextUtils.isEmpty(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileProvince.getText().toString())) {
            Intent intent = new Intent(editProfileActivity, RegisterDetailDataActivity.class);
            intent.putParcelableArrayListExtra(RegisterDetailDataActivity.LIST_DATA, editProfileActivity.parseGroupData());
            intent.putExtra(RegisterDetailDataActivity.PICK_MODE, SambharConstant.PICK_MODE_GROUP);
            editProfileActivity.startActivityForResult(intent, GROUP_REQUEST_CODE);
        }
    }

    public static /* synthetic */ void lambda$onCreate$4(EditProfileActivity editProfileActivity, ValidatorEntity validatorEntity) {
        if (validatorEntity.isValid()) {
            ((EditProfileViewModel) editProfileActivity.getViewModel()).sendUpdateProfile(editProfileActivity.body);
        } else {
            editProfileActivity.showErrorSnackbar(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).llEditprofileRoot, validatorEntity.getMessage());
        }
    }

    public static /* synthetic */ void lambda$onCreate$5(EditProfileActivity editProfileActivity, View view) {
        editProfileActivity.body = new EditProfileBodyPost();
        editProfileActivity.body.setUsername(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileUsername.getText().toString());
        editProfileActivity.body.setProvince(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileProvince.getText().toString());
        editProfileActivity.body.setGroup(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileGroup.getText().toString());
        editProfileActivity.body.setFbAccount(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileFacebook.getText().toString());
        editProfileActivity.body.setTwitterAccount(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileTwitter.getText().toString());
        editProfileActivity.body.setInstagramAccount(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileInstagram.getText().toString());
        if (!(editProfileActivity.provinceId == 0 || editProfileActivity.groupId == 0)) {
            editProfileActivity.body.setCompanyId(editProfileActivity.provinceId);
            editProfileActivity.body.setGroupId(editProfileActivity.groupId);
            editProfileActivity.body.setProvince(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileProvince.getText().toString());
            editProfileActivity.body.setGroup(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).etEditprofileGroup.getText().toString());
        }
        ((EditProfileViewModel) editProfileActivity.getViewModel()).validateRegisterForm(editProfileActivity.body);
    }

    public static /* synthetic */ void lambda$onCreate$6(EditProfileActivity editProfileActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                editProfileActivity.showLoading(editProfileActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                editProfileActivity.dismissLoading();
                editProfileActivity.showErrorSnackbar(((ActivityEditProfileBinding) editProfileActivity.getDataBinding()).llEditprofileRoot, resource.message);
                return;
            case SUCCESS:
                editProfileActivity.dismissLoading();
                Toast.makeText(editProfileActivity, "Edit profile berhasil", 0).show();
                editProfileActivity.finish();
                return;
            default:
                return;
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        super.onActivityResult(i, i2, intent);
        GroupEntity groupEntity;
        if (i == PROVINCE_REQUEST_CODE) {
            if (i2 == -1) {
                groupEntity = (GroupEntity) intent.getParcelableExtra(PROVINCE_DATA);
                this.provinceId = groupEntity.getId();
                ((ActivityEditProfileBinding) getDataBinding()).etEditprofileProvince.setText(groupEntity.getName());
                this.groupId = -1;
                ((ActivityEditProfileBinding) getDataBinding()).etEditprofileGroup.setText("");
            }
        } else if (i == GROUP_REQUEST_CODE && i2 == -1) {
            groupEntity = (GroupEntity) intent.getParcelableExtra(GROUP_DATA);
            this.groupId = groupEntity.getId();
            ((ActivityEditProfileBinding) getDataBinding()).etEditprofileGroup.setText(groupEntity.getName());
        }
    }

    private ArrayList<GroupEntity> parseGroupData() {
        ArrayList arrayList = new ArrayList();
        Iterator it = this.groupList.iterator();
        while (it.hasNext()) {
            GroupEntity groupEntity = (GroupEntity) it.next();
            if (groupEntity.getCompanyId() == this.provinceId) {
                arrayList.add(groupEntity);
            }
        }
        return arrayList;
    }
}
