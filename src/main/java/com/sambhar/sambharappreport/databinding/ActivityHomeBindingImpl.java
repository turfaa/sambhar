package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarButton;
import com.sambhar.sambharappreport.base.view.ShambarEditText;
import com.sambhar.sambharappreport.base.view.ShambarTextView;

public class ActivityHomeBindingImpl extends ActivityHomeBinding {
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
        sViewsWithIds.put(R.id.ll_share_container, 1);
        sViewsWithIds.put(R.id.tv_ui_instagram, 2);
        sViewsWithIds.put(R.id.tv_instagram_count, 3);
        sViewsWithIds.put(R.id.tv_ui_facebook, 4);
        sViewsWithIds.put(R.id.tv_facebook_count, 5);
        sViewsWithIds.put(R.id.tv_ui_twitter, 6);
        sViewsWithIds.put(R.id.tv_twitter_count, 7);
        sViewsWithIds.put(R.id.iv_image, 8);
        sViewsWithIds.put(R.id.et_caption, 9);
        sViewsWithIds.put(R.id.et_url_reshare, 10);
        sViewsWithIds.put(R.id.bt_image, 11);
        sViewsWithIds.put(R.id.bt_video, 12);
        sViewsWithIds.put(R.id.bt_share_ig, 13);
        sViewsWithIds.put(R.id.bt_share_facebook, 14);
        sViewsWithIds.put(R.id.bt_share_twitter, 15);
    }

    public ActivityHomeBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, ViewDataBinding.mapBindings(dataBindingComponent, view, 16, sIncludes, sViewsWithIds));
    }

    private ActivityHomeBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (ShambarButton) objArr[11], (LinearLayout) objArr[14], (LinearLayout) objArr[13], (LinearLayout) objArr[15], (ShambarButton) objArr[12], (ShambarEditText) objArr[9], (ShambarEditText) objArr[10], (ImageView) objArr[8], (LinearLayout) objArr[0], (LinearLayout) objArr[1], (ShambarTextView) objArr[5], (ShambarTextView) objArr[3], (ShambarTextView) objArr[7], (ShambarTextView) objArr[4], (ShambarTextView) objArr[2], (ShambarTextView) objArr[6]);
        this.mDirtyFlags = -1;
        this.llHomeRoot.setTag(null);
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
