package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RelativeLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;

public class ActivityLoginBindingImpl extends ActivityLoginBinding {
    @Nullable
    private static final IncludedLayouts sIncludes = null;
    @Nullable
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    /* Access modifiers changed, original: protected */
    public boolean onFieldChange(int i, Object obj, int i2) {
        return false;
    }

    public boolean setVariable(int i, @Nullable Object obj) {
        return true;
    }

    static {
        sViewsWithIds.put(R.id.et_email, 1);
        sViewsWithIds.put(R.id.et_password, 2);
        sViewsWithIds.put(R.id.bt_login, 3);
        sViewsWithIds.put(R.id.bt_register, 4);
    }

    public ActivityLoginBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, ViewDataBinding.mapBindings(dataBindingComponent, view, 5, sIncludes, sViewsWithIds));
    }

    private ActivityLoginBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ShambarButton) objArr[3], (ShambarButton) objArr[4], (ShambarEditText) objArr[1], (ShambarEditText) objArr[2], (RelativeLayout) objArr[0]);
        this.mDirtyFlags = -1;
        this.rlLoginRoot.setTag(null);
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
