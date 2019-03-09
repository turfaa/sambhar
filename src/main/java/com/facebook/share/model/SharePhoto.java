package com.facebook.share.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import com.facebook.share.model.ShareMedia.Type;
import java.util.ArrayList;
import java.util.List;

public final class SharePhoto extends ShareMedia {
    public static final Creator<SharePhoto> CREATOR = new Creator<SharePhoto>() {
        public SharePhoto createFromParcel(Parcel parcel) {
            return new SharePhoto(parcel);
        }

        public SharePhoto[] newArray(int i) {
            return new SharePhoto[i];
        }
    };
    private final Bitmap bitmap;
    private final String caption;
    private final Uri imageUrl;
    private final boolean userGenerated;

    public static final class Builder extends com.facebook.share.model.ShareMedia.Builder<SharePhoto, Builder> {
        private Bitmap bitmap;
        private String caption;
        private Uri imageUrl;
        private boolean userGenerated;

        public Builder setBitmap(@Nullable Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder setImageUrl(@Nullable Uri uri) {
            this.imageUrl = uri;
            return this;
        }

        public Builder setUserGenerated(boolean z) {
            this.userGenerated = z;
            return this;
        }

        public Builder setCaption(@Nullable String str) {
            this.caption = str;
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public Uri getImageUrl() {
            return this.imageUrl;
        }

        /* Access modifiers changed, original: 0000 */
        public Bitmap getBitmap() {
            return this.bitmap;
        }

        public SharePhoto build() {
            return new SharePhoto(this, null);
        }

        public Builder readFrom(SharePhoto sharePhoto) {
            if (sharePhoto == null) {
                return this;
            }
            return ((Builder) super.readFrom((ShareMedia) sharePhoto)).setBitmap(sharePhoto.getBitmap()).setImageUrl(sharePhoto.getImageUrl()).setUserGenerated(sharePhoto.getUserGenerated()).setCaption(sharePhoto.getCaption());
        }

        /* Access modifiers changed, original: 0000 */
        public Builder readFrom(Parcel parcel) {
            return readFrom((SharePhoto) parcel.readParcelable(SharePhoto.class.getClassLoader()));
        }

        static void writePhotoListTo(Parcel parcel, int i, List<SharePhoto> list) {
            ShareMedia[] shareMediaArr = new ShareMedia[list.size()];
            for (int i2 = 0; i2 < list.size(); i2++) {
                shareMediaArr[i2] = (ShareMedia) list.get(i2);
            }
            parcel.writeParcelableArray(shareMediaArr, i);
        }

        static List<SharePhoto> readPhotoListFrom(Parcel parcel) {
            List<ShareMedia> readListFrom = com.facebook.share.model.ShareMedia.Builder.readListFrom(parcel);
            ArrayList arrayList = new ArrayList();
            for (ShareMedia shareMedia : readListFrom) {
                if (shareMedia instanceof SharePhoto) {
                    arrayList.add((SharePhoto) shareMedia);
                }
            }
            return arrayList;
        }
    }

    public int describeContents() {
        return 0;
    }

    /* synthetic */ SharePhoto(Builder builder, AnonymousClass1 anonymousClass1) {
        this(builder);
    }

    private SharePhoto(Builder builder) {
        super((com.facebook.share.model.ShareMedia.Builder) builder);
        this.bitmap = builder.bitmap;
        this.imageUrl = builder.imageUrl;
        this.userGenerated = builder.userGenerated;
        this.caption = builder.caption;
    }

    SharePhoto(Parcel parcel) {
        super(parcel);
        this.bitmap = (Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader());
        this.imageUrl = (Uri) parcel.readParcelable(Uri.class.getClassLoader());
        this.userGenerated = parcel.readByte() != (byte) 0;
        this.caption = parcel.readString();
    }

    @Nullable
    public Bitmap getBitmap() {
        return this.bitmap;
    }

    @Nullable
    public Uri getImageUrl() {
        return this.imageUrl;
    }

    public boolean getUserGenerated() {
        return this.userGenerated;
    }

    public String getCaption() {
        return this.caption;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.bitmap, 0);
        parcel.writeParcelable(this.imageUrl, 0);
        parcel.writeByte((byte) this.userGenerated);
        parcel.writeString(this.caption);
    }

    public Type getMediaType() {
        return Type.PHOTO;
    }
}
