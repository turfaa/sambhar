package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;
import com.sambhar.sambharappreport.base.view.ShambarTextView;

public class ActivityRegisterBindingImpl extends ActivityRegisterBinding {
    @Nullable
    private static final IncludedLayouts sIncludes = null;
    @Nullable
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;
    @NonNull
    private final ScrollView mboundView0;

    /* Access modifiers changed, original: protected */
    public boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    public boolean setVariable(int i, @Nullable Object obj) {
        return true;
    }

    static {
        sViewsWithIds.put(R.id.ll_register_root, 1);
        sViewsWithIds.put(R.id.ll_top, 2);
        sViewsWithIds.put(R.id.et_username, 3);
        sViewsWithIds.put(R.id.et_password, 4);
        sViewsWithIds.put(R.id.et_province, 5);
        sViewsWithIds.put(R.id.et_group, 6);
        sViewsWithIds.put(R.id.tv_title_facebook, 7);
        sViewsWithIds.put(R.id.et_facebook, 8);
        sViewsWithIds.put(R.id.tv_title_twitter, 9);
        sViewsWithIds.put(R.id.et_twitter, 10);
        sViewsWithIds.put(R.id.tv_title_instagram, 11);
        sViewsWithIds.put(R.id.et_instagram, 12);
        sViewsWithIds.put(R.id.bt_register, 13);
    }

    public ActivityRegisterBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, ViewDataBinding.mapBindings(dataBindingComponent, view, 14, sIncludes, sViewsWithIds));
    }

    private ActivityRegisterBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ShambarButton) objArr[13], (ShambarEditText) objArr[8], (ShambarEditText) objArr[6], (ShambarEditText) objArr[12], (ShambarEditText) objArr[4], (ShambarEditText) objArr[5], (ShambarEditText) objArr[10], (ShambarEditText) objArr[3], (LinearLayout) objArr[1], (LinearLayout) objArr[2], (ShambarTextView) objArr[7], (ShambarTextView) objArr[11], (ShambarTextView) objArr[9]);
        this.mDirtyFlags = -1;
        this.mboundView0 = (ScrollView) objArr[0];
        this.mboundView0.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 1;
        }
        requestRebind();
    }

    public boolean hasPendingBindings() {
        synchronized (this) {
            if (this.mDirtyFlags != 0) {
                return true;
            }
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public void executeBindings() {
        synchronized (this) {
            long j = this.mDirtyFlags;
            this.mDirtyFlags = 0;
        }
    }
}
