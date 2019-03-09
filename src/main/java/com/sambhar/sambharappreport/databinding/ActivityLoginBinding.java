package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;

public abstract class ActivityLoginBinding extends ViewDataBinding {
    @NonNull
    public final ShambarButton btLogin;
    @NonNull
    public final ShambarButton btRegister;
    @NonNull
    public final ShambarEditText etEmail;
    @NonNull
    public final ShambarEditText etPassword;
    @NonNull
    public final RelativeLayout rlLoginRoot;

    protected ActivityLoginBinding(DataBindingComponent dataBindingComponent, View view, int i, ShambarButton shambarButton, ShambarButton shambarButton2, ShambarEditText shambarEditText, ShambarEditText shambarEditText2, RelativeLayout relativeLayout) {
        super(dataBindingComponent, view, i);
        this.btLogin = shambarButton;
        this.btRegister = shambarButton2;
        this.etEmail = shambarEditText;
        this.etPassword = shambarEditText2;
        this.rlLoginRoot = relativeLayout;
    }

    @NonNull
    public static ActivityLoginBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityLoginBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityLoginBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_login, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityLoginBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityLoginBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityLoginBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_login, null, false, dataBindingComponent);
    }

    public static ActivityLoginBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityLoginBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityLoginBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_login);
    }
}
