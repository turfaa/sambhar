package com.sambhar.sambharappreport.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.sambhar.sambharappreport.R;

public class ActivityMainBindingImpl extends ActivityMainBinding {
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
        sViewsWithIds.put(R.id.tv_instagram_count, 1);
        sViewsWithIds.put(R.id.tv_facebook_count, 2);
        sViewsWithIds.put(R.id.tv_twitter_count, 3);
        sViewsWithIds.put(R.id.iv_image, 4);
        sViewsWithIds.put(R.id.et_caption, 5);
        sViewsWithIds.put(R.id.bt_image, 6);
        sViewsWithIds.put(R.id.bt_video, 7);
        sViewsWithIds.put(R.id.bt_share_ig, 8);
        sViewsWithIds.put(R.id.bt_share_facebook, 9);
        sViewsWithIds.put(R.id.bt_share_twitter, 10);
    }

    public ActivityMainBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, ViewDataBinding.mapBindings(dataBindingComponent, view, 11, sIncludes, sViewsWithIds));
    }

    private ActivityMainBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 0, (Button) objArr[6], (Button) objArr[9], (Button) objArr[8], (Button) objArr[10], (Button) objArr[7], (EditText) objArr[5], (ImageView) objArr[4], (TextView) objArr[2], (TextView) objArr[1], (TextView) objArr[3]);
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
