package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sambhar.sambharappreport.R;

public abstract class ActivityRegisterDetailDataBinding extends ViewDataBinding {
    @NonNull
    public final RecyclerView rvRegisterDetail;

    protected ActivityRegisterDetailDataBinding(DataBindingComponent dataBindingComponent, View view, int i, RecyclerView recyclerView) {
        super(dataBindingComponent, view, i);
        this.rvRegisterDetail = recyclerView;
    }

    @NonNull
    public static ActivityRegisterDetailDataBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityRegisterDetailDataBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterDetailDataBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_register_detail_data, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityRegisterDetailDataBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityRegisterDetailDataBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterDetailDataBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_register_detail_data, null, false, dataBindingComponent);
    }

    public static ActivityRegisterDetailDataBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityRegisterDetailDataBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityRegisterDetailDataBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_register_detail_data);
    }
}
