package com.facebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.facebook.internal.Utility;
import com.facebook.internal.Utility.GraphMeRequestWithCacheCallback;
import com.facebook.internal.Validate;
import com.facebook.share.internal.ShareConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AccessToken implements Parcelable {
    public static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String APPLICATION_ID_KEY = "application_id";
    public static final Creator<AccessToken> CREATOR = new Creator() {
        public AccessToken createFromParcel(Parcel parcel) {
            return new AccessToken(parcel);
        }

        public AccessToken[] newArray(int i) {
            return new AccessToken[i];
        }
    };
    private static final int CURRENT_JSON_FORMAT = 1;
    public static final String DATA_ACCESS_EXPIRATION_TIME = "data_access_expiration_time";
    private static final String DECLINED_PERMISSIONS_KEY = "declined_permissions";
    private static final AccessTokenSource DEFAULT_ACCESS_TOKEN_SOURCE = AccessTokenSource.FACEBOOK_APPLICATION_WEB;
    private static final Date DEFAULT_EXPIRATION_TIME = MAX_DATE;
    private static final Date DEFAULT_LAST_REFRESH_TIME = new Date();
    private static final String EXPIRES_AT_KEY = "expires_at";
    public static final String EXPIRES_IN_KEY = "expires_in";
    private static final String LAST_REFRESH_KEY = "last_refresh";
    private static final Date MAX_DATE = new Date(Long.MAX_VALUE);
    private static final String PERMISSIONS_KEY = "permissions";
    private static final String SOURCE_KEY = "source";
    private static final String TOKEN_KEY = "token";
    public static final String USER_ID_KEY = "user_id";
    private static final String VERSION_KEY = "version";
    private final String applicationId;
    private final Date dataAccessExpirationTime;
    private final Set<String> declinedPermissions;
    private final Date expires;
    private final Date lastRefresh;
    private final Set<String> permissions;
    private final AccessTokenSource source;
    private final String token;
    private final String userId;

    public interface AccessTokenCreationCallback {
        void onError(FacebookException facebookException);

        void onSuccess(AccessToken accessToken);
    }

    public interface AccessTokenRefreshCallback {
        void OnTokenRefreshFailed(FacebookException facebookException);

        void OnTokenRefreshed(AccessToken accessToken);
    }

    public int describeContents() {
        return 0;
    }

    public AccessToken(String str, String str2, String str3, @Nullable Collection<String> collection, @Nullable Collection<String> collection2, @Nullable AccessTokenSource accessTokenSource, @Nullable Date date, @Nullable Date date2, @Nullable Date date3) {
        Validate.notNullOrEmpty(str, "accessToken");
        Validate.notNullOrEmpty(str2, "applicationId");
        Validate.notNullOrEmpty(str3, "userId");
        if (date == null) {
            date = DEFAULT_EXPIRATION_TIME;
        }
        this.expires = date;
        this.permissions = Collections.unmodifiableSet(collection != null ? new HashSet(collection) : new HashSet());
        this.declinedPermissions = Collections.unmodifiableSet(collection2 != null ? new HashSet(collection2) : new HashSet());
        this.token = str;
        if (accessTokenSource == null) {
            accessTokenSource = DEFAULT_ACCESS_TOKEN_SOURCE;
        }
        this.source = accessTokenSource;
        if (date2 == null) {
            date2 = DEFAULT_LAST_REFRESH_TIME;
        }
        this.lastRefresh = date2;
        this.applicationId = str2;
        this.userId = str3;
        if (date3 == null || date3.getTime() == 0) {
            date3 = DEFAULT_EXPIRATION_TIME;
        }
        this.dataAccessExpirationTime = date3;
    }

    public static AccessToken getCurrentAccessToken() {
        return AccessTokenManager.getInstance().getCurrentAccessToken();
    }

    public static boolean isCurrentAccessTokenActive() {
        AccessToken currentAccessToken = AccessTokenManager.getInstance().getCurrentAccessToken();
        return (currentAccessToken == null || currentAccessToken.isExpired()) ? false : true;
    }

    public static boolean isDataAccessActive() {
        AccessToken currentAccessToken = AccessTokenManager.getInstance().getCurrentAccessToken();
        return (currentAccessToken == null || currentAccessToken.isDataAccessExpired()) ? false : true;
    }

    static void expireCurrentAccessToken() {
        AccessToken currentAccessToken = AccessTokenManager.getInstance().getCurrentAccessToken();
        if (currentAccessToken != null) {
            setCurrentAccessToken(createExpired(currentAccessToken));
        }
    }

    public static void setCurrentAccessToken(AccessToken accessToken) {
        AccessTokenManager.getInstance().setCurrentAccessToken(accessToken);
    }

    public static void refreshCurrentAccessTokenAsync() {
        AccessTokenManager.getInstance().refreshCurrentAccessToken(null);
    }

    public static void refreshCurrentAccessTokenAsync(AccessTokenRefreshCallback accessTokenRefreshCallback) {
        AccessTokenManager.getInstance().refreshCurrentAccessToken(accessTokenRefreshCallback);
    }

    public String getToken() {
        return this.token;
    }

    public Date getExpires() {
        return this.expires;
    }

    public Date getDataAccessExpirationTime() {
        return this.dataAccessExpirationTime;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public Set<String> getDeclinedPermissions() {
        return this.declinedPermissions;
    }

    public AccessTokenSource getSource() {
        return this.source;
    }

    public Date getLastRefresh() {
        return this.lastRefresh;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public String getUserId() {
        return this.userId;
    }

    public static void createFromNativeLinkingIntent(Intent intent, final String str, final AccessTokenCreationCallback accessTokenCreationCallback) {
        Validate.notNull(intent, "intent");
        if (intent.getExtras() == null) {
            accessTokenCreationCallback.onError(new FacebookException("No extras found on intent"));
            return;
        }
        final Bundle bundle = new Bundle(intent.getExtras());
        String string = bundle.getString("access_token");
        if (string == null || string.isEmpty()) {
            accessTokenCreationCallback.onError(new FacebookException("No access token found on intent"));
            return;
        }
        String string2 = bundle.getString(USER_ID_KEY);
        if (string2 == null || string2.isEmpty()) {
            Utility.getGraphMeRequestWithCacheAsync(string, new GraphMeRequestWithCacheCallback() {
                public void onSuccess(JSONObject jSONObject) {
                    try {
                        bundle.putString(AccessToken.USER_ID_KEY, jSONObject.getString(ShareConstants.WEB_DIALOG_PARAM_ID));
                        accessTokenCreationCallback.onSuccess(AccessToken.createFromBundle(null, bundle, AccessTokenSource.FACEBOOK_APPLICATION_WEB, new Date(), str));
                    } catch (JSONException unused) {
                        accessTokenCreationCallback.onError(new FacebookException("Unable to generate access token due to missing user id"));
                    }
                }

                public void onFailure(FacebookException facebookException) {
                    accessTokenCreationCallback.onError(facebookException);
                }
            });
        } else {
            accessTokenCreationCallback.onSuccess(createFromBundle(null, bundle, AccessTokenSource.FACEBOOK_APPLICATION_WEB, new Date(), str));
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{AccessToken");
        stringBuilder.append(" token:");
        stringBuilder.append(tokenToString());
        appendPermissions(stringBuilder);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AccessToken)) {
            return false;
        }
        AccessToken accessToken = (AccessToken) obj;
        if (!(this.expires.equals(accessToken.expires) && this.permissions.equals(accessToken.permissions) && this.declinedPermissions.equals(accessToken.declinedPermissions) && this.token.equals(accessToken.token) && this.source == accessToken.source && this.lastRefresh.equals(accessToken.lastRefresh) && (!this.applicationId != null ? accessToken.applicationId != null : !this.applicationId.equals(accessToken.applicationId)) && this.userId.equals(accessToken.userId) && this.dataAccessExpirationTime.equals(accessToken.dataAccessExpirationTime))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((((((((((((((527 + this.expires.hashCode()) * 31) + this.permissions.hashCode()) * 31) + this.declinedPermissions.hashCode()) * 31) + this.token.hashCode()) * 31) + this.source.hashCode()) * 31) + this.lastRefresh.hashCode()) * 31) + (this.applicationId == null ? 0 : this.applicationId.hashCode())) * 31) + this.userId.hashCode()) * 31) + this.dataAccessExpirationTime.hashCode();
    }

    @SuppressLint({"FieldGetter"})
    static AccessToken createFromRefresh(AccessToken accessToken, Bundle bundle) {
        if (accessToken.source == AccessTokenSource.FACEBOOK_APPLICATION_WEB || accessToken.source == AccessTokenSource.FACEBOOK_APPLICATION_NATIVE || accessToken.source == AccessTokenSource.FACEBOOK_APPLICATION_SERVICE) {
            Date bundleLongAsDate = Utility.getBundleLongAsDate(bundle, EXPIRES_IN_KEY, new Date(0));
            String string = bundle.getString("access_token");
            Date bundleLongAsDate2 = Utility.getBundleLongAsDate(bundle, DATA_ACCESS_EXPIRATION_TIME, new Date(0));
            if (Utility.isNullOrEmpty(string)) {
                return null;
            }
            return new AccessToken(string, accessToken.applicationId, accessToken.getUserId(), accessToken.getPermissions(), accessToken.getDeclinedPermissions(), accessToken.source, bundleLongAsDate, new Date(), bundleLongAsDate2);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid token source: ");
        stringBuilder.append(accessToken.source);
        throw new FacebookException(stringBuilder.toString());
    }

    static AccessToken createExpired(AccessToken accessToken) {
        return new AccessToken(accessToken.token, accessToken.applicationId, accessToken.getUserId(), accessToken.getPermissions(), accessToken.getDeclinedPermissions(), accessToken.source, new Date(), new Date(), accessToken.dataAccessExpirationTime);
    }

    static AccessToken createFromLegacyCache(Bundle bundle) {
        List permissionsFromBundle = getPermissionsFromBundle(bundle, LegacyTokenHelper.PERMISSIONS_KEY);
        List permissionsFromBundle2 = getPermissionsFromBundle(bundle, LegacyTokenHelper.DECLINED_PERMISSIONS_KEY);
        String applicationId = LegacyTokenHelper.getApplicationId(bundle);
        if (Utility.isNullOrEmpty(applicationId)) {
            applicationId = FacebookSdk.getApplicationId();
        }
        String str = applicationId;
        String token = LegacyTokenHelper.getToken(bundle);
        try {
            return new AccessToken(token, str, Utility.awaitGetGraphMeRequestWithCache(token).getString(ShareConstants.WEB_DIALOG_PARAM_ID), permissionsFromBundle, permissionsFromBundle2, LegacyTokenHelper.getSource(bundle), LegacyTokenHelper.getDate(bundle, LegacyTokenHelper.EXPIRATION_DATE_KEY), LegacyTokenHelper.getDate(bundle, LegacyTokenHelper.LAST_REFRESH_DATE_KEY), null);
        } catch (JSONException unused) {
            return null;
        }
    }

    static List<String> getPermissionsFromBundle(Bundle bundle, String str) {
        ArrayList stringArrayList = bundle.getStringArrayList(str);
        if (stringArrayList == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList(stringArrayList));
    }

    public boolean isExpired() {
        return new Date().after(this.expires);
    }

    public boolean isDataAccessExpired() {
        return new Date().after(this.dataAccessExpirationTime);
    }

    /* Access modifiers changed, original: 0000 */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("version", 1);
        jSONObject.put(TOKEN_KEY, this.token);
        jSONObject.put(EXPIRES_AT_KEY, this.expires.getTime());
        jSONObject.put("permissions", new JSONArray(this.permissions));
        jSONObject.put(DECLINED_PERMISSIONS_KEY, new JSONArray(this.declinedPermissions));
        jSONObject.put(LAST_REFRESH_KEY, this.lastRefresh.getTime());
        jSONObject.put("source", this.source.name());
        jSONObject.put(APPLICATION_ID_KEY, this.applicationId);
        jSONObject.put(USER_ID_KEY, this.userId);
        jSONObject.put(DATA_ACCESS_EXPIRATION_TIME, this.dataAccessExpirationTime.getTime());
        return jSONObject;
    }

    static AccessToken createFromJSONObject(JSONObject jSONObject) throws JSONException {
        if (jSONObject.getInt("version") <= 1) {
            String string = jSONObject.getString(TOKEN_KEY);
            Date date = new Date(jSONObject.getLong(EXPIRES_AT_KEY));
            JSONArray jSONArray = jSONObject.getJSONArray("permissions");
            JSONArray jSONArray2 = jSONObject.getJSONArray(DECLINED_PERMISSIONS_KEY);
            Date date2 = new Date(jSONObject.getLong(LAST_REFRESH_KEY));
            AccessTokenSource valueOf = AccessTokenSource.valueOf(jSONObject.getString("source"));
            return new AccessToken(string, jSONObject.getString(APPLICATION_ID_KEY), jSONObject.getString(USER_ID_KEY), Utility.jsonArrayToStringList(jSONArray), Utility.jsonArrayToStringList(jSONArray2), valueOf, date, date2, new Date(jSONObject.optLong(DATA_ACCESS_EXPIRATION_TIME, 0)));
        }
        throw new FacebookException("Unknown AccessToken serialization format.");
    }

    private static AccessToken createFromBundle(List<String> list, Bundle bundle, AccessTokenSource accessTokenSource, Date date, String str) {
        String string = bundle.getString("access_token");
        Date bundleLongAsDate = Utility.getBundleLongAsDate(bundle, EXPIRES_IN_KEY, date);
        String string2 = bundle.getString(USER_ID_KEY);
        Date bundleLongAsDate2 = Utility.getBundleLongAsDate(bundle, DATA_ACCESS_EXPIRATION_TIME, new Date(0));
        if (Utility.isNullOrEmpty(string) || bundleLongAsDate == null) {
            return null;
        }
        return new AccessToken(string, str, string2, list, null, accessTokenSource, bundleLongAsDate, new Date(), bundleLongAsDate2);
    }

    private String tokenToString() {
        if (this.token == null) {
            return "null";
        }
        return FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.INCLUDE_ACCESS_TOKENS) ? this.token : "ACCESS_TOKEN_REMOVED";
    }

    private void appendPermissions(StringBuilder stringBuilder) {
        stringBuilder.append(" permissions:");
        if (this.permissions == null) {
            stringBuilder.append("null");
            return;
        }
        stringBuilder.append("[");
        stringBuilder.append(TextUtils.join(", ", this.permissions));
        stringBuilder.append("]");
    }

    AccessToken(Parcel parcel) {
        this.expires = new Date(parcel.readLong());
        ArrayList arrayList = new ArrayList();
        parcel.readStringList(arrayList);
        this.permissions = Collections.unmodifiableSet(new HashSet(arrayList));
        arrayList.clear();
        parcel.readStringList(arrayList);
        this.declinedPermissions = Collections.unmodifiableSet(new HashSet(arrayList));
        this.token = parcel.readString();
        this.source = AccessTokenSource.valueOf(parcel.readString());
        this.lastRefresh = new Date(parcel.readLong());
        this.applicationId = parcel.readString();
        this.userId = parcel.readString();
        this.dataAccessExpirationTime = new Date(parcel.readLong());
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.expires.getTime());
        parcel.writeStringList(new ArrayList(this.permissions));
        parcel.writeStringList(new ArrayList(this.declinedPermissions));
        parcel.writeString(this.token);
        parcel.writeString(this.source.name());
        parcel.writeLong(this.lastRefresh.getTime());
        parcel.writeString(this.applicationId);
        parcel.writeString(this.userId);
        parcel.writeLong(this.dataAccessExpirationTime.getTime());
    }
}
