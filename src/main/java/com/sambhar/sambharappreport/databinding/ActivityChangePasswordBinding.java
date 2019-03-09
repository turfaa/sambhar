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

public abstract class ActivityChangePasswordBinding extends ViewDataBinding {
    @NonNull
    public final ShambarButton btUpdatePassword;
    @NonNull
    public final ShambarEditText etNewPassword;
    @NonNull
    public final ShambarEditText etOldPassword;
    @NonNull
    public final LinearLayout llChangePasswordRoot;

    protected ActivityChangePasswordBinding(DataBindingComponent dataBindingComponent, View view, int i, ShambarButton shambarButton, ShambarEditText shambarEditText, ShambarEditText shambarEditText2, LinearLayout linearLayout) {
        super(dataBindingComponent, view, i);
        this.btUpdatePassword = shambarButton;
        this.etNewPassword = shambarEditText;
        this.etOldPassword = shambarEditText2;
        this.llChangePasswordRoot = linearLayout;
    }

    @NonNull
    public static ActivityChangePasswordBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityChangePasswordBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityChangePasswordBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_change_password, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityChangePasswordBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityChangePasswordBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityChangePasswordBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_change_password, null, false, dataBindingComponent);
    }

    public static ActivityChangePasswordBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityChangePasswordBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityChangePasswordBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_change_password);
    }
}
