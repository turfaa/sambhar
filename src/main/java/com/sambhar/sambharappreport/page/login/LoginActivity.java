package com.sambhar.sambharappreport.page.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.databinding.ActivityLoginBinding;
import com.sambhar.sambharappreport.entity.ValidatorEntity;
import com.sambhar.sambharappreport.entity.bodypost.LoginFormBody;
import com.sambhar.sambharappreport.entity.response.LoginResponse;
import com.sambhar.sambharappreport.page.main.MainActivity;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {
    public static final String SHOW_LOGOUT_POPUP = "show_logout_pop_up";
    private LoginFormBody body;
    @Inject
    UserSharedPref mPref;

    public int setLayoutView() {
        return R.layout.activity_login;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.mPref.isLogin()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        if (getIntent().getBooleanExtra(SHOW_LOGOUT_POPUP, false)) {
            Builder builder = new Builder(this);
            builder.setMessage((CharSequence) "Sesi anda telah berakhir");
            builder.setPositiveButton((CharSequence) "OK", -$$Lambda$LoginActivity$lGiec4PRAMdNKaJBP_lgosgW54Y.INSTANCE);
            builder.create().show();
        }
        ((LoginViewModel) getViewModel()).login().observe(this, new -$$Lambda$LoginActivity$bT8Jlxr9k15deeAmuv-BVrqULOU(this));
        ((LoginViewModel) getViewModel()).getValidateLogin().observe(this, new -$$Lambda$LoginActivity$YDNdaS2lUEW5hlBwlw7x1dFlMpE(this));
        ((ActivityLoginBinding) getDataBinding()).btLogin.setOnClickListener(new -$$Lambda$LoginActivity$HIvOjvCDWeqkeG2z3D5gqd44WKM(this));
        ((ActivityLoginBinding) getDataBinding()).btRegister.setOnClickListener(new -$$Lambda$LoginActivity$RG5ofrf5546YfTH8JX0HRdkaPFQ(this));
    }

    public static /* synthetic */ void lambda$onCreate$1(LoginActivity loginActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                loginActivity.showLoading(loginActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                loginActivity.dismissLoading();
                loginActivity.showErrorSnackbar(((ActivityLoginBinding) loginActivity.getDataBinding()).rlLoginRoot, resource.message);
                return;
            case SUCCESS:
                loginActivity.dismissLoading();
                loginActivity.mPref.setUserToken(((LoginResponse) resource.data).getLoginEntity().getToken());
                loginActivity.mPref.setIsLogin(true);
                loginActivity.mPref.setFacebookCount(((LoginResponse) resource.data).getLoginEntity().getCount().getFbCount());
                loginActivity.mPref.setFacebookStatus(((LoginResponse) resource.data).getLoginEntity().getFbStatus());
                loginActivity.mPref.setTwitterCount(((LoginResponse) resource.data).getLoginEntity().getCount().getTwitterCount());
                loginActivity.mPref.setTwitterStatus(((LoginResponse) resource.data).getLoginEntity().getTwitterStatus());
                loginActivity.mPref.setInstagramCount(((LoginResponse) resource.data).getLoginEntity().getCount().getInstagramCount());
                loginActivity.mPref.setInstagramStatus(((LoginResponse) resource.data).getLoginEntity().getInstagramStatus());
                loginActivity.startActivity(new Intent(loginActivity, MainActivity.class));
                loginActivity.finish();
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$2(LoginActivity loginActivity, ValidatorEntity validatorEntity) {
        if (validatorEntity.isValid()) {
            ((LoginViewModel) loginActivity.getViewModel()).doLogin(loginActivity.body);
        } else {
            loginActivity.showErrorSnackbar(((ActivityLoginBinding) loginActivity.getDataBinding()).rlLoginRoot, validatorEntity.getMessage());
        }
    }

    public static /* synthetic */ void lambda$onCreate$3(LoginActivity loginActivity, View view) {
        String obj = ((ActivityLoginBinding) loginActivity.getDataBinding()).etEmail.getText().toString();
        String obj2 = ((ActivityLoginBinding) loginActivity.getDataBinding()).etPassword.getText().toString();
        loginActivity.body = new LoginFormBody();
        loginActivity.body.setUsername(obj);
        loginActivity.body.setPassword(obj2);
        ((LoginViewModel) loginActivity.getViewModel()).validateLoginForm(loginActivity.body);
    }
}
