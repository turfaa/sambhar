package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.sambhar.sambharappreport.R;

public abstract class ActivityMainBinding extends ViewDataBinding {
    @NonNull
    public final Button btImage;
    @NonNull
    public final Button btShareFacebook;
    @NonNull
    public final Button btShareIg;
    @NonNull
    public final Button btShareTwitter;
    @NonNull
    public final Button btVideo;
    @NonNull
    public final EditText etCaption;
    @NonNull
    public final ImageView ivImage;
    @NonNull
    public final TextView tvFacebookCount;
    @NonNull
    public final TextView tvInstagramCount;
    @NonNull
    public final TextView tvTwitterCount;

    protected ActivityMainBinding(DataBindingComponent dataBindingComponent, View view, int i, Button button, Button button2, Button button3, Button button4, Button button5, EditText editText, ImageView imageView, TextView textView, TextView textView2, TextView textView3) {
        super(dataBindingComponent, view, i);
        this.btImage = button;
        this.btShareFacebook = button2;
        this.btShareIg = button3;
        this.btShareTwitter = button4;
        this.btVideo = button5;
        this.etCaption = editText;
        this.ivImage = imageView;
        this.tvFacebookCount = textView;
        this.tvInstagramCount = textView2;
        this.tvTwitterCount = textView3;
    }

    @NonNull
    public static ActivityMainBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z) {
        return inflate(layoutInflater, viewGroup, z, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityMainBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, boolean z, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityMainBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, viewGroup, z, dataBindingComponent);
    }

    @NonNull
    public static ActivityMainBinding inflate(@NonNull LayoutInflater layoutInflater) {
        return inflate(layoutInflater, DataBindingUtil.getDefaultComponent());
    }

    @NonNull
    public static ActivityMainBinding inflate(@NonNull LayoutInflater layoutInflater, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityMainBinding) DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, null, false, dataBindingComponent);
    }

    public static ActivityMainBinding bind(@NonNull View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    public static ActivityMainBinding bind(@NonNull View view, @Nullable DataBindingComponent dataBindingComponent) {
        return (ActivityMainBinding) ViewDataBinding.bind(dataBindingComponent, view, R.layout.activity_main);
    }
}
