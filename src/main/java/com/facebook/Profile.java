package com.facebook;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.Utility;
import com.facebook.internal.Utility.GraphMeRequestWithCacheCallback;
import com.facebook.internal.Validate;
import org.json.JSONException;
import org.json.JSONObject;

public final class Profile implements Parcelable {
    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        public Profile createFromParcel(Parcel parcel) {
            return new Profile(parcel, null);
        }

        public Profile[] newArray(int i) {
            return new Profile[i];
        }
    };
    private static final String FIRST_NAME_KEY = "first_name";
    private static final String ID_KEY = "id";
    private static final String LAST_NAME_KEY = "last_name";
    private static final String LINK_URI_KEY = "link_uri";
    private static final String MIDDLE_NAME_KEY = "middle_name";
    private static final String NAME_KEY = "name";
    private static final String TAG = "Profile";
    @Nullable
    private final String firstName;
    @Nullable
    private final String id;
    @Nullable
    private final String lastName;
    @Nullable
    private final Uri linkUri;
    @Nullable
    private final String middleName;
    @Nullable
    private final String name;

    public int describeContents() {
        return 0;
    }

    /* synthetic */ Profile(Parcel parcel, AnonymousClass1 anonymousClass1) {
        this(parcel);
    }

    public static Profile getCurrentProfile() {
        return ProfileManager.getInstance().getCurrentProfile();
    }

    public static void setCurrentProfile(@Nullable Profile profile) {
        ProfileManager.getInstance().setCurrentProfile(profile);
    }

    public static void fetchProfileForCurrentAccessToken() {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if (AccessToken.isCurrentAccessTokenActive()) {
            Utility.getGraphMeRequestWithCacheAsync(currentAccessToken.getToken(), new GraphMeRequestWithCacheCallback() {
                public void onSuccess(JSONObject jSONObject) {
                    String optString = jSONObject.optString("id");
                    if (optString != null) {
                        String optString2 = jSONObject.optString("link");
                        Profile.setCurrentProfile(new Profile(optString, jSONObject.optString(Profile.FIRST_NAME_KEY), jSONObject.optString(Profile.MIDDLE_NAME_KEY), jSONObject.optString(Profile.LAST_NAME_KEY), jSONObject.optString("name"), optString2 != null ? Uri.parse(optString2) : null));
                    }
                }

                public void onFailure(FacebookException facebookException) {
                    String access$000 = Profile.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Got unexpected exception: ");
                    stringBuilder.append(facebookException);
                    Log.e(access$000, stringBuilder.toString());
                }
            });
        } else {
            setCurrentProfile(null);
        }
    }

    public Profile(String str, @Nullable String str2, @Nullable String str3, @Nullable String str4, @Nullable String str5, @Nullable Uri uri) {
        Validate.notNullOrEmpty(str, "id");
        this.id = str;
        this.firstName = str2;
        this.middleName = str3;
        this.lastName = str4;
        this.name = str5;
        this.linkUri = uri;
    }

    public Uri getProfilePictureUri(int i, int i2) {
        return ImageRequest.getProfilePictureUri(this.id, i, i2);
    }

    public String getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getName() {
        return this.name;
    }

    public Uri getLinkUri() {
        return this.linkUri;
    }

    /* JADX WARNING: Missing block: B:11:0x001c, code skipped:
            if (r5.firstName == null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:18:0x0031, code skipped:
            if (r5.middleName == null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:24:0x0044, code skipped:
            if (r5.lastName == null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:30:0x0057, code skipped:
            if (r5.name == null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:36:0x006a, code skipped:
            if (r5.linkUri == null) goto L_0x0075;
     */
    public boolean equals(java.lang.Object r5) {
        /*
        r4 = this;
        r0 = 1;
        if (r4 != r5) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = r5 instanceof com.facebook.Profile;
        r2 = 0;
        if (r1 != 0) goto L_0x000a;
    L_0x0009:
        return r2;
    L_0x000a:
        r5 = (com.facebook.Profile) r5;
        r1 = r4.id;
        r3 = r5.id;
        r1 = r1.equals(r3);
        if (r1 == 0) goto L_0x0021;
    L_0x0016:
        r1 = r4.firstName;
        if (r1 != 0) goto L_0x0021;
    L_0x001a:
        r5 = r5.firstName;
        if (r5 != 0) goto L_0x001f;
    L_0x001e:
        goto L_0x0075;
    L_0x001f:
        r0 = 0;
        goto L_0x0075;
    L_0x0021:
        r1 = r4.firstName;
        r3 = r5.firstName;
        r1 = r1.equals(r3);
        if (r1 == 0) goto L_0x0034;
    L_0x002b:
        r1 = r4.middleName;
        if (r1 != 0) goto L_0x0034;
    L_0x002f:
        r5 = r5.middleName;
        if (r5 != 0) goto L_0x001f;
    L_0x0033:
        goto L_0x0075;
    L_0x0034:
        r1 = r4.middleName;
        r3 = r5.middleName;
        r1 = r1.equals(r3);
        if (r1 == 0) goto L_0x0047;
    L_0x003e:
        r1 = r4.lastName;
        if (r1 != 0) goto L_0x0047;
    L_0x0042:
        r5 = r5.lastName;
        if (r5 != 0) goto L_0x001f;
    L_0x0046:
        goto L_0x0075;
    L_0x0047:
        r1 = r4.lastName;
        r3 = r5.lastName;
        r1 = r1.equals(r3);
        if (r1 == 0) goto L_0x005a;
    L_0x0051:
        r1 = r4.name;
        if (r1 != 0) goto L_0x005a;
    L_0x0055:
        r5 = r5.name;
        if (r5 != 0) goto L_0x001f;
    L_0x0059:
        goto L_0x0075;
    L_0x005a:
        r1 = r4.name;
        r3 = r5.name;
        r1 = r1.equals(r3);
        if (r1 == 0) goto L_0x006d;
    L_0x0064:
        r1 = r4.linkUri;
        if (r1 != 0) goto L_0x006d;
    L_0x0068:
        r5 = r5.linkUri;
        if (r5 != 0) goto L_0x001f;
    L_0x006c:
        goto L_0x0075;
    L_0x006d:
        r0 = r4.linkUri;
        r5 = r5.linkUri;
        r0 = r0.equals(r5);
    L_0x0075:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.Profile.equals(java.lang.Object):boolean");
    }

    public int hashCode() {
        int hashCode = 527 + this.id.hashCode();
        if (this.firstName != null) {
            hashCode = (hashCode * 31) + this.firstName.hashCode();
        }
        if (this.middleName != null) {
            hashCode = (hashCode * 31) + this.middleName.hashCode();
        }
        if (this.lastName != null) {
            hashCode = (hashCode * 31) + this.lastName.hashCode();
        }
        if (this.name != null) {
            hashCode = (hashCode * 31) + this.name.hashCode();
        }
        return this.linkUri != null ? (hashCode * 31) + this.linkUri.hashCode() : hashCode;
    }

    /* Access modifiers changed, original: 0000 */
    public JSONObject toJSONObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("id", this.id);
            jSONObject.put(FIRST_NAME_KEY, this.firstName);
            jSONObject.put(MIDDLE_NAME_KEY, this.middleName);
            jSONObject.put(LAST_NAME_KEY, this.lastName);
            jSONObject.put("name", this.name);
            if (this.linkUri == null) {
                return jSONObject;
            }
            jSONObject.put(LINK_URI_KEY, this.linkUri.toString());
            return jSONObject;
        } catch (JSONException unused) {
            return null;
        }
    }

    Profile(JSONObject jSONObject) {
        Uri uri = null;
        this.id = jSONObject.optString("id", null);
        this.firstName = jSONObject.optString(FIRST_NAME_KEY, null);
        this.middleName = jSONObject.optString(MIDDLE_NAME_KEY, null);
        this.lastName = jSONObject.optString(LAST_NAME_KEY, null);
        this.name = jSONObject.optString("name", null);
        String optString = jSONObject.optString(LINK_URI_KEY, null);
        if (optString != null) {
            uri = Uri.parse(optString);
        }
        this.linkUri = uri;
    }

    private Profile(Parcel parcel) {
        Uri uri;
        this.id = parcel.readString();
        this.firstName = parcel.readString();
        this.middleName = parcel.readString();
        this.lastName = parcel.readString();
        this.name = parcel.readString();
        String readString = parcel.readString();
        if (readString == null) {
            uri = null;
        } else {
            uri = Uri.parse(readString);
        }
        this.linkUri = uri;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.firstName);
        parcel.writeString(this.middleName);
        parcel.writeString(this.lastName);
        parcel.writeString(this.name);
        parcel.writeString(this.linkUri == null ? null : this.linkUri.toString());
    }
}
