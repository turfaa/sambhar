package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;
import com.sambhar.sambharappreport.base.view.ShambarTextView;

public abstract class ActivityHomeBinding extends ViewDataBinding {
    @NonNull
    public final ShambarButton btImage;
    @NonNull
    public final LinearLayout btShareFacebook;
    @NonNull
    public final LinearLayout btShareIg;
    @NonNull
    public final LinearLayout btShareTwitter;
    @NonNull
    public final ShambarButton btVideo;
    @NonNull
    public final ShambarEditText etCaption;
    @NonNull
    public final ShambarEditText etUrlReshare;
    @NonNull
    public final ImageView ivImage;
    @NonNull
    public final LinearLayout llHomeRoot;
    @NonNull
    public final LinearLayout llShareContainer;
    @NonNull
    public final ShambarTextView tvFacebookCount;
    @NonNull
    public final ShambarTextView tvInstagramCount;
    @NonNull
    public final ShambarTextView tvTwitterCount;
    @NonNull
    public final ShambarTextView tvUiFacebook;
    @NonNull
    public final ShambarTextView tvUiInstagram;
    @NonNull
    public final ShambarTextView tvUiTwitter;

    protected ActivityHomeBinding(DataBindingComponent dataBindingComponent, View view, int i, ShambarButton shambarButton, LinearLayout linearLayout, LinearLayout linearLayout2, LinearLayout linearLayout3, ShambarButton shambarButton2, ShambarEditText shambarEditText, ShambarEditText shambarEditText2, ImageView imageView, LinearLayout linearLayout4, LinearLayout linearLayout5, ShambarTextView shambarTextView, ShambarTextView shambarTextView2, ShambarTextView shambarTextView3, ShambarTextView shambarTextView4, ShambarTextView shambarTextView5, ShambarTextView shambarTextView6) {
        super(dataBindingComponent, view, i);
        this.btImage = shambarButton;
        this.btShareFacebook = linearLayout;
        this.btShareIg = linearLayout2;
        this.btShareTwitter = linearLayout3;
        this.btVideo = shambarButton2;
        this.etCaption = shambarEditText;
        this.etUrlReshare = shambarEditText2;
        this.ivImage = imageView;
        this.llHomeRoot = linearLayout4;
        this.llShareContainer = linearLayout5;
        this.tvFacebookCount = shambarTextView;
        this.tvInstagramCount = shambarTextView2;
        this.tvTwitterCount = shambarTextView3;
        this.tvUiFacebook = shambarTextView4;
        this.tvUiInstagram = shambarTextView5;
        this.tvUiTwitter = shambarTextView6;
    }

    @NonNull
    public static ActivityHomeBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityHomeBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityHomeBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_home, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityHomeBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityHomeBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityHomeBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_home, null, false, dataBindingComponent);
    }

    public static ActivityHomeBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityHomeBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityHomeBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_home);
    }
}
