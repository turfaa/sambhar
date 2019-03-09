package com.twitter.sdk.android.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.SerializedName;

public class TwitterAuthToken extends AuthToken implements Parcelable {
    public static final Creator<TwitterAuthToken> CREATOR = new Creator<TwitterAuthToken>() {
        public TwitterAuthToken createFromParcel(Parcel parcel) {
            return new TwitterAuthToken(parcel, null);
        }

        public TwitterAuthToken[] newArray(int i) {
            return new TwitterAuthToken[i];
        }
    };
    @SerializedName("secret")
    public final String secret;
    @SerializedName("token")
    public final String token;

    public int describeContents() {
        return 0;
    }

    public boolean isExpired() {
        return false;
    }

    public TwitterAuthToken(String str, String str2) {
        this.token = str;
        this.secret = str2;
    }

    TwitterAuthToken(String str, String str2, long j) {
        super(j);
        this.token = str;
        this.secret = str2;
    }

    private TwitterAuthToken(Parcel parcel) {
        this.token = parcel.readString();
        this.secret = parcel.readString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("token=");
        stringBuilder.append(this.token);
        stringBuilder.append(",secret=");
        stringBuilder.append(this.secret);
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.token);
        parcel.writeString(this.secret);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TwitterAuthToken)) {
            return false;
        }
        TwitterAuthToken twitterAuthToken = (TwitterAuthToken) obj;
        if (this.secret == null ? twitterAuthToken.secret == null : this.secret.equals(twitterAuthToken.secret)) {
            return this.token == null ? twitterAuthToken.token == null : this.token.equals(twitterAuthToken.token);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.token != null ? this.token.hashCode() : 0) * 31;
        if (this.secret != null) {
            i = this.secret.hashCode();
        }
        return hashCode + i;
    }
}
