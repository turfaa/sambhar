package com.facebook.share.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;

public final class ShareOpenGraphAction extends ShareOpenGraphValueContainer<ShareOpenGraphAction, Builder> {
    public static final Creator<ShareOpenGraphAction> CREATOR = new Creator<ShareOpenGraphAction>() {
        public ShareOpenGraphAction createFromParcel(Parcel parcel) {
            return new ShareOpenGraphAction(parcel);
        }

        public ShareOpenGraphAction[] newArray(int i) {
            return new ShareOpenGraphAction[i];
        }
    };

    public static final class Builder extends com.facebook.share.model.ShareOpenGraphValueContainer.Builder<ShareOpenGraphAction, Builder> {
        private static final String ACTION_TYPE_KEY = "og:type";

        public Builder setActionType(String str) {
            putString(ACTION_TYPE_KEY, str);
            return this;
        }

        public ShareOpenGraphAction build() {
            return new ShareOpenGraphAction(this, null);
        }

        public Builder readFrom(ShareOpenGraphAction shareOpenGraphAction) {
            if (shareOpenGraphAction == null) {
                return this;
            }
            return ((Builder) super.readFrom((ShareOpenGraphValueContainer) shareOpenGraphAction)).setActionType(shareOpenGraphAction.getActionType());
        }

        /* Access modifiers changed, original: 0000 */
        public Builder readFrom(Parcel parcel) {
            return readFrom((ShareOpenGraphAction) parcel.readParcelable(ShareOpenGraphAction.class.getClassLoader()));
        }
    }

    /* synthetic */ ShareOpenGraphAction(Builder builder, AnonymousClass1 anonymousClass1) {
        this(builder);
    }

    private ShareOpenGraphAction(Builder builder) {
        super((com.facebook.share.model.ShareOpenGraphValueContainer.Builder) builder);
    }

    ShareOpenGraphAction(Parcel parcel) {
        super(parcel);
    }

    @Nullable
    public String getActionType() {
        return getString("og:type");
    }
}
