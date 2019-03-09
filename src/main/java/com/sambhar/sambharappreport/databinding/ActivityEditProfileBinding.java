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

public abstract class ActivityEditProfileBinding extends ViewDataBinding {
    @NonNull
    public final ShambarButton btToChangePassword;
    @NonNull
    public final ShambarButton btUpdate;
    @NonNull
    public final ShambarEditText etEditprofileFacebook;
    @NonNull
    public final ShambarEditText etEditprofileGroup;
    @NonNull
    public final ShambarEditText etEditprofileInstagram;
    @NonNull
    public final ShambarEditText etEditprofileProvince;
    @NonNull
    public final ShambarEditText etEditprofileTwitter;
    @NonNull
    public final ShambarEditText etEditprofileUsername;
    @NonNull
    public final LinearLayout llEditprofileRoot;
    @NonNull
    public final LinearLayout llTop;
    @NonNull
    public final ShambarTextView tvEditProfileTitleFacebook;
    @NonNull
    public final ShambarTextView tvEditprofileTitleInstagram;
    @NonNull
    public final ShambarTextView tvEditprofileTitleTwitter;

    protected ActivityEditProfileBinding(DataBindingComponent dataBindingComponent, View view, int i, ShambarButton shambarButton, ShambarButton shambarButton2, ShambarEditText shambarEditText, ShambarEditText shambarEditText2, ShambarEditText shambarEditText3, ShambarEditText shambarEditText4, ShambarEditText shambarEditText5, ShambarEditText shambarEditText6, LinearLayout linearLayout, LinearLayout linearLayout2, ShambarTextView shambarTextView, ShambarTextView shambarTextView2, ShambarTextView shambarTextView3) {
        super(dataBindingComponent, view, i);
        this.btToChangePassword = shambarButton;
        this.btUpdate = shambarButton2;
        this.etEditprofileFacebook = shambarEditText;
        this.etEditprofileGroup = shambarEditText2;
        this.etEditprofileInstagram = shambarEditText3;
        this.etEditprofileProvince = shambarEditText4;
        this.etEditprofileTwitter = shambarEditText5;
        this.etEditprofileUsername = shambarEditText6;
        this.llEditprofileRoot = linearLayout;
        this.llTop = linearLayout2;
        this.tvEditProfileTitleFacebook = shambarTextView;
        this.tvEditprofileTitleInstagram = shambarTextView2;
        this.tvEditprofileTitleTwitter = shambarTextView3;
    }

    @NonNull
    public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityEditProfileBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_edit_profile, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityEditProfileBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_edit_profile, null, false, dataBindingComponent);
    }

    public static ActivityEditProfileBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityEditProfileBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityEditProfileBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_edit_profile);
    }
}
