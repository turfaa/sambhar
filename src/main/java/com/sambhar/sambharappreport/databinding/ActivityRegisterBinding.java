package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;
import com.sambhar.sambharappreport.base.view.ShambarTextView;

public abstract class ActivityRegisterBinding extends ViewDataBinding {
    @NonNull
    public final ShambarButton btRegister;
    @NonNull
    public final ShambarEditText etFacebook;
    @NonNull
    public final ShambarEditText etGroup;
    @NonNull
    public final ShambarEditText etInstagram;
    @NonNull
    public final ShambarEditText etPassword;
    @NonNull
    public final ShambarEditText etProvince;
    @NonNull
    public final ShambarEditText etTwitter;
    @NonNull
    public final ShambarEditText etUsername;
    @NonNull
    public final LinearLayout llRegisterRoot;
    @NonNull
    public final LinearLayout llTop;
    @NonNull
    public final ShambarTextView tvTitleFacebook;
    @NonNull
    public final ShambarTextView tvTitleInstagram;
    @NonNull
    public final ShambarTextView tvTitleTwitter;

    protected ActivityRegisterBinding(DataBindingComponent dataBindingComponent, View view, int i, ShambarButton shambarButton, ShambarEditText shambarEditText, ShambarEditText shambarEditText2, ShambarEditText shambarEditText3, ShambarEditText shambarEditText4, ShambarEditText shambarEditText5, ShambarEditText shambarEditText6, ShambarEditText shambarEditText7, LinearLayout linearLayout, LinearLayout linearLayout2, ShambarTextView shambarTextView, ShambarTextView shambarTextView2, ShambarTextView shambarTextView3) {
        super(dataBindingComponent, view, i);
        this.btRegister = shambarButton;
        this.etFacebook = shambarEditText;
        this.etGroup = shambarEditText2;
        this.etInstagram = shambarEditText3;
        this.etPassword = shambarEditText4;
        this.etProvince = shambarEditText5;
        this.etTwitter = shambarEditText6;
        this.etUsername = shambarEditText7;
        this.llRegisterRoot = linearLayout;
        this.llTop = linearLayout2;
        this.tvTitleFacebook = shambarTextView;
        this.tvTitleInstagram = shambarTextView2;
        this.tvTitleTwitter = shambarTextView3;
    }

    @NonNull
    public static ActivityRegisterBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityRegisterBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_register, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityRegisterBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityRegisterBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_register, null, false, dataBindingComponent);
    }

    public static ActivityRegisterBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityRegisterBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_register);
    }
}
