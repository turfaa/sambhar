package com.facebook.share.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ShareMediaContent extends ShareContent<ShareMediaContent, Builder> {
    public static final Creator<ShareMediaContent> CREATOR = new Creator<ShareMediaContent>() {
        public ShareMediaContent createFromParcel(Parcel parcel) {
            return new ShareMediaContent(parcel);
        }

        public ShareMediaContent[] newArray(int i) {
            return new ShareMediaContent[i];
        }
    };
    private final List<ShareMedia> media;

    public static class Builder extends com.facebook.share.model.ShareContent.Builder<ShareMediaContent, Builder> {
        private final List<ShareMedia> media = new ArrayList();

        public Builder addMedium(@Nullable ShareMedia shareMedia) {
            if (shareMedia != null) {
                Object build;
                if (shareMedia instanceof SharePhoto) {
                    build = new com.facebook.share.model.SharePhoto.Builder().readFrom((SharePhoto) shareMedia).build();
                } else if (shareMedia instanceof ShareVideo) {
                    build = new com.facebook.share.model.ShareVideo.Builder().readFrom((ShareVideo) shareMedia).build();
                } else {
                    throw new IllegalArgumentException("medium must be either a SharePhoto or ShareVideo");
                }
                this.media.add(build);
            }
            return this;
        }

        public Builder addMedia(@Nullable List<ShareMedia> list) {
            if (list != null) {
                for (ShareMedia addMedium : list) {
                    addMedium(addMedium);
                }
            }
            return this;
        }

        public ShareMediaContent build() {
            return new ShareMediaContent(this, null);
        }

        public Builder readFrom(ShareMediaContent shareMediaContent) {
            if (shareMediaContent == null) {
                return this;
            }
            return ((Builder) super.readFrom((ShareContent) shareMediaContent)).addMedia(shareMediaContent.getMedia());
        }

        public Builder setMedia(@Nullable List<ShareMedia> list) {
            this.media.clear();
            addMedia(list);
            return this;
        }
    }

    public int describeContents() {
        return 0;
    }

    /* synthetic */ ShareMediaContent(Builder builder, AnonymousClass1 anonymousClass1) {
        this(builder);
    }

    private ShareMediaContent(Builder builder) {
        super((com.facebook.share.model.ShareContent.Builder) builder);
        this.media = Collections.unmodifiableList(builder.media);
    }

    ShareMediaContent(Parcel parcel) {
        super(parcel);
        this.media = Arrays.asList((ShareMedia[]) parcel.readParcelableArray(ShareMedia.class.getClassLoader()));
    }

    @Nullable
    public List<ShareMedia> getMedia() {
        return this.media;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelableArray((ShareMedia[]) this.media.toArray(), i);
    }
}
