package com.facebook.share.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public final class ShareMessengerMediaTemplateContent extends ShareContent<ShareMessengerMediaTemplateContent, Builder> {
    public static final Creator<ShareMessengerMediaTemplateContent> CREATOR = new Creator<ShareMessengerMediaTemplateContent>() {
        public ShareMessengerMediaTemplateContent createFromParcel(Parcel parcel) {
            return new ShareMessengerMediaTemplateContent(parcel);
        }

        public ShareMessengerMediaTemplateContent[] newArray(int i) {
            return new ShareMessengerMediaTemplateContent[i];
        }
    };
    private final String attachmentId;
    private final ShareMessengerActionButton button;
    private final MediaType mediaType;
    private final Uri mediaUrl;

    public enum MediaType {
        IMAGE,
        VIDEO
    }

    public static class Builder extends com.facebook.share.model.ShareContent.Builder<ShareMessengerMediaTemplateContent, Builder> {
        private String attachmentId;
        private ShareMessengerActionButton button;
        private MediaType mediaType;
        private Uri mediaUrl;

        public Builder setMediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder setAttachmentId(String str) {
            this.attachmentId = str;
            return this;
        }

        public Builder setMediaUrl(Uri uri) {
            this.mediaUrl = uri;
            return this;
        }

        public Builder setButton(ShareMessengerActionButton shareMessengerActionButton) {
            this.button = shareMessengerActionButton;
            return this;
        }

        public Builder readFrom(ShareMessengerMediaTemplateContent shareMessengerMediaTemplateContent) {
            if (shareMessengerMediaTemplateContent == null) {
                return this;
            }
            return ((Builder) super.readFrom((ShareContent) shareMessengerMediaTemplateContent)).setMediaType(shareMessengerMediaTemplateContent.getMediaType()).setAttachmentId(shareMessengerMediaTemplateContent.getAttachmentId()).setMediaUrl(shareMessengerMediaTemplateContent.getMediaUrl()).setButton(shareMessengerMediaTemplateContent.getButton());
        }

        public ShareMessengerMediaTemplateContent build() {
            return new ShareMessengerMediaTemplateContent(this, null);
        }
    }

    public int describeContents() {
        return 0;
    }

    /* synthetic */ ShareMessengerMediaTemplateContent(Builder builder, AnonymousClass1 anonymousClass1) {
        this(builder);
    }

    private ShareMessengerMediaTemplateContent(Builder builder) {
        super((com.facebook.share.model.ShareContent.Builder) builder);
        this.mediaType = builder.mediaType;
        this.attachmentId = builder.attachmentId;
        this.mediaUrl = builder.mediaUrl;
        this.button = builder.button;
    }

    ShareMessengerMediaTemplateContent(Parcel parcel) {
        super(parcel);
        this.mediaType = (MediaType) parcel.readSerializable();
        this.attachmentId = parcel.readString();
        this.mediaUrl = (Uri) parcel.readParcelable(Uri.class.getClassLoader());
        this.button = (ShareMessengerActionButton) parcel.readParcelable(ShareMessengerActionButton.class.getClassLoader());
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public String getAttachmentId() {
        return this.attachmentId;
    }

    public Uri getMediaUrl() {
        return this.mediaUrl;
    }

    public ShareMessengerActionButton getButton() {
        return this.button;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(this.mediaType);
        parcel.writeString(this.attachmentId);
        parcel.writeParcelable(this.mediaUrl, i);
        parcel.writeParcelable(this.button, i);
    }
}
