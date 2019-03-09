package com.sambhar.sambharappreport.page.changepassword;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.databinding.ActivityChangePasswordBinding;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.UpdatePasswordFormBody;
import com.sambhar.sambharappreport.rest.Resource;

public class ChangePasswordActivity extends BaseActivity<ChangePasswordViewModel, ActivityChangePasswordBinding> {
    private UpdatePasswordFormBody body;

    public int setLayoutView() {
        return R.layout.activity_change_password;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setupActionBar("Change Password", true);
        ((ChangePasswordViewModel) getViewModel()).changePassword().observe(this, new -$$Lambda$ChangePasswordActivity$-Hy9l9jhSsQvzB_dqPCsUgzfW7A(this));
        ((ChangePasswordViewModel) getViewModel()).getValidatChangePasswordForm().observe(this, new -$$Lambda$ChangePasswordActivity$Iexd-lSlieXpLrQiXxs7bYS5ZDw(this));
        ((ActivityChangePasswordBinding) getDataBinding()).btUpdatePassword.setOnClickListener(new -$$Lambda$ChangePasswordActivity$asOi0AIRmeOafwfSjw-G-C7EZMQ(this));
    }

    public static /* synthetic */ void lambda$onCreate$0(ChangePasswordActivity changePasswordActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                changePasswordActivity.showLoading(changePasswordActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                changePasswordActivity.dismissLoading();
                changePasswordActivity.showErrorSnackbar(((ActivityChangePasswordBinding) changePasswordActivity.getDataBinding()).llChangePasswordRoot, resource.message);
                return;
            case SUCCESS:
                changePasswordActivity.dismissLoading();
                Toast.makeText(changePasswordActivity, "Update Password Success", 0).show();
                changePasswordActivity.finish();
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$1(ChangePasswordActivity changePasswordActivity, ValidatorEntity validatorEntity) {
        if (validatorEntity.isValid()) {
            ((ChangePasswordViewModel) changePasswordActivity.getViewModel()).sendChangePassword(changePasswordActivity.body);
        } else {
            changePasswordActivity.showErrorSnackbar(((ActivityChangePasswordBinding) changePasswordActivity.getDataBinding()).llChangePasswordRoot, validatorEntity.getMessage());
        }
    }

    public static /* synthetic */ void lambda$onCreate$2(ChangePasswordActivity changePasswordActivity, View view) {
        changePasswordActivity.body = new UpdatePasswordFormBody();
        changePasswordActivity.body.setOldPassword(((ActivityChangePasswordBinding) changePasswordActivity.getDataBinding()).etOldPassword.getText().toString());
        changePasswordActivity.body.setNewPassword(((ActivityChangePasswordBinding) changePasswordActivity.getDataBinding()).etNewPassword.getText().toString());
        ((ChangePasswordViewModel) changePasswordActivity.getViewModel()).validateChangePasswordForm(changePasswordActivity.body);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }
}
