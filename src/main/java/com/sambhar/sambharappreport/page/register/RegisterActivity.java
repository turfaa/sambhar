package com.sambhar.sambharappreport.page.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog.Builder;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.data.SambharConstant;
import com.sambhar.sambharappreport.databinding.ActivityRegisterBinding;
import com.sambhar.sambharappreport.entity.GroupEntity;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.RegisterFormBody;
import com.sambhar.sambharappreport.entity.response.RegisterDataResponse;
import com.sambhar.sambharappreport.rest.Resource;
import java.util.ArrayList;
import java.util.Iterator;

public class RegisterActivity extends BaseActivity<RegisterViewModel, ActivityRegisterBinding> {
    public static String GROUP_DATA = "group_data";
    private static int GROUP_REQUEST_CODE = 200;
    public static String PROVINCE_DATA = "province_data";
    private static int PROVINCE_REQUEST_CODE = 100;
    private RegisterFormBody body;
    private ArrayList<GroupEntity> companyList;
    private int groupId;
    private ArrayList<GroupEntity> groupList;
    private int provinceId;

    public int setLayoutView() {
        return R.layout.activity_register;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setupActionBar("Register", true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setElevation(0.0f);
        }
        this.companyList = new ArrayList();
        this.groupList = new ArrayList();
        ((RegisterViewModel) getViewModel()).getValidateRegisterForm().observe(this, new -$$Lambda$RegisterActivity$kw3L8w6jFkith8Uh-tBatTbRaLk(this));
        ((ActivityRegisterBinding) getDataBinding()).tvTitleFacebook.setText("@");
        ((ActivityRegisterBinding) getDataBinding()).tvTitleTwitter.setText("@");
        ((ActivityRegisterBinding) getDataBinding()).tvTitleInstagram.setText("@");
        ((ActivityRegisterBinding) getDataBinding()).btRegister.setOnClickListener(new -$$Lambda$RegisterActivity$xFKW8YYh6ZG79V4Ib1PkBbeWkZA(this));
        ((RegisterViewModel) getViewModel()).registerData().observe(this, new -$$Lambda$RegisterActivity$T3oxwgj9IKL052h_Se58j-1vmXY(this));
        ((RegisterViewModel) getViewModel()).getRegisterData();
        ((ActivityRegisterBinding) getDataBinding()).etProvince.setOnClickListener(new -$$Lambda$RegisterActivity$gCrYmtVp4sy3DPOeW_cKbpjTIsg(this));
        ((ActivityRegisterBinding) getDataBinding()).etGroup.setOnClickListener(new -$$Lambda$RegisterActivity$Ugh-cBu1MxvLuOWogGdJ3Uvjz6I(this));
        ((RegisterViewModel) getViewModel()).registerUser().observe(this, new -$$Lambda$RegisterActivity$qNrgkcgNoXiiuFgKhhHAgvx8R-I(this));
    }

    public static /* synthetic */ void lambda$onCreate$0(RegisterActivity registerActivity, ValidatorEntity validatorEntity) {
        if (validatorEntity.isValid()) {
            ((RegisterViewModel) registerActivity.getViewModel()).sendRegisterUser(registerActivity.body);
        } else {
            registerActivity.showErrorSnackbar(((ActivityRegisterBinding) registerActivity.getDataBinding()).llRegisterRoot, validatorEntity.getMessage());
        }
    }

    public static /* synthetic */ void lambda$onCreate$1(RegisterActivity registerActivity, View view) {
        registerActivity.body = new RegisterFormBody();
        registerActivity.body.setUsername(((ActivityRegisterBinding) registerActivity.getDataBinding()).etUsername.getText().toString());
        registerActivity.body.setPassword(((ActivityRegisterBinding) registerActivity.getDataBinding()).etPassword.getText().toString());
        registerActivity.body.setProvince(((ActivityRegisterBinding) registerActivity.getDataBinding()).etProvince.getText().toString());
        registerActivity.body.setGroup(((ActivityRegisterBinding) registerActivity.getDataBinding()).etGroup.getText().toString());
        registerActivity.body.setFacebook(((ActivityRegisterBinding) registerActivity.getDataBinding()).etFacebook.getText().toString());
        registerActivity.body.setTwitter(((ActivityRegisterBinding) registerActivity.getDataBinding()).etTwitter.getText().toString());
        registerActivity.body.setInstagram(((ActivityRegisterBinding) registerActivity.getDataBinding()).etInstagram.getText().toString());
        registerActivity.body.setProvinceId(registerActivity.provinceId);
        registerActivity.body.setGroupId(registerActivity.groupId);
        ((RegisterViewModel) registerActivity.getViewModel()).validateRegisterForm(registerActivity.body);
    }

    public static /* synthetic */ void lambda$onCreate$2(RegisterActivity registerActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                registerActivity.showLoading("Please wait");
                return;
            case ERROR:
                registerActivity.dismissLoading();
                registerActivity.showDialogFailedLoadDataRegister();
                return;
            case SUCCESS:
                registerActivity.dismissLoading();
                registerActivity.companyList = ((RegisterDataResponse) resource.data).getEntity().getCompanyEntityList();
                registerActivity.groupList = ((RegisterDataResponse) resource.data).getEntity().getGroupEntityList();
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$3(RegisterActivity registerActivity, View view) {
        Intent intent = new Intent(registerActivity, RegisterDetailDataActivity.class);
        intent.putParcelableArrayListExtra(RegisterDetailDataActivity.LIST_DATA, registerActivity.companyList);
        intent.putExtra(RegisterDetailDataActivity.PICK_MODE, SambharConstant.PICK_MODE_PROVINCE);
        registerActivity.startActivityForResult(intent, PROVINCE_REQUEST_CODE);
    }

    public static /* synthetic */ void lambda$onCreate$4(RegisterActivity registerActivity, View view) {
        if (!TextUtils.isEmpty(((ActivityRegisterBinding) registerActivity.getDataBinding()).etProvince.getText().toString())) {
            Intent intent = new Intent(registerActivity, RegisterDetailDataActivity.class);
            intent.putParcelableArrayListExtra(RegisterDetailDataActivity.LIST_DATA, registerActivity.parseGroupData());
            intent.putExtra(RegisterDetailDataActivity.PICK_MODE, SambharConstant.PICK_MODE_GROUP);
            registerActivity.startActivityForResult(intent, GROUP_REQUEST_CODE);
        }
    }

    public static /* synthetic */ void lambda$onCreate$5(RegisterActivity registerActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                registerActivity.showLoading(registerActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                registerActivity.dismissLoading();
                registerActivity.showErrorSnackbar(((ActivityRegisterBinding) registerActivity.getDataBinding()).llRegisterRoot, resource.message);
                return;
            case SUCCESS:
                registerActivity.dismissLoading();
                registerActivity.finish();
                Toast.makeText(registerActivity, "Registrasi berhasil", 0).show();
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        GroupEntity groupEntity;
        if (i == PROVINCE_REQUEST_CODE) {
            if (i2 == -1) {
                groupEntity = (GroupEntity) intent.getParcelableExtra(PROVINCE_DATA);
                this.provinceId = groupEntity.getId();
                ((ActivityRegisterBinding) getDataBinding()).etProvince.setText(groupEntity.getName());
                this.groupId = -1;
                ((ActivityRegisterBinding) getDataBinding()).etGroup.setText("");
            }
        } else if (i == GROUP_REQUEST_CODE && i2 == -1) {
            groupEntity = (GroupEntity) intent.getParcelableExtra(GROUP_DATA);
            this.groupId = groupEntity.getId();
            ((ActivityRegisterBinding) getDataBinding()).etGroup.setText(groupEntity.getName());
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }

    private void showDialogFailedLoadDataRegister() {
        Builder builder = new Builder(this);
        builder.setCancelable(false);
        builder.setMessage((CharSequence) "Gagal memuat data yang dibutuhkan untuk registrasi, silahkan coba beberapa saat lagi");
        builder.setPositiveButton((CharSequence) "Tutup", new -$$Lambda$RegisterActivity$ATPZHoScY5fpDQRsD0PrMHJF958(this));
        builder.create().show();
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
