package com.twitter.sdk.android.core.internal.oauth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.twitter.sdk.android.core.TwitterAuthToken;

public class OAuthResponse implements Parcelable {
    public static final Creator<OAuthResponse> CREATOR = new Creator<OAuthResponse>() {
        public OAuthResponse createFromParcel(Parcel parcel) {
            return new OAuthResponse(parcel, null);
        }

        public OAuthResponse[] newArray(int i) {
            return new OAuthResponse[i];
        }
    };
    public final TwitterAuthToken authToken;
    public final long userId;
    public final String userName;

    public int describeContents() {
        return 0;
    }

    /* synthetic */ OAuthResponse(Parcel parcel, AnonymousClass1 anonymousClass1) {
        this(parcel);
    }

    public OAuthResponse(TwitterAuthToken twitterAuthToken, String str, long j) {
        this.authToken = twitterAuthToken;
        this.userName = str;
        this.userId = j;
    }

    private OAuthResponse(Parcel parcel) {
        this.authToken = (TwitterAuthToken) parcel.readParcelable(TwitterAuthToken.class.getClassLoader());
        this.userName = parcel.readString();
        this.userId = parcel.readLong();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("authToken=");
        stringBuilder.append(this.authToken);
        stringBuilder.append(",userName=");
        stringBuilder.append(this.userName);
        stringBuilder.append(",userId=");
        stringBuilder.append(this.userId);
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.authToken, i);
        parcel.writeString(this.userName);
        parcel.writeLong(this.userId);
    }
}
