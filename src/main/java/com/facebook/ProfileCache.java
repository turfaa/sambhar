package com.facebook;

import android.content.SharedPreferences;
import com.facebook.internal.Validate;
import org.json.JSONException;
import org.json.JSONObject;

final class ProfileCache {
    static final String CACHED_PROFILE_KEY = "com.facebook.ProfileManager.CachedProfile";
    static final String SHARED_PREFERENCES_NAME = "com.facebook.AccessTokenManager.SharedPreferences";
    private final SharedPreferences sharedPreferences = FacebookSdk.getApplicationContext().getSharedPreferences("com.facebook.AccessTokenManager.SharedPreferences", 0);

    ProfileCache() {
    }

    /* Access modifiers changed, original: 0000 */
    public Profile load() {
        String string = this.sharedPreferences.getString(CACHED_PROFILE_KEY, null);
        if (string != null) {
            try {
                return new Profile(new JSONObject(string));
            } catch (JSONException unused) {
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public void save(Profile profile) {
        Validate.notNull(profile, "profile");
        JSONObject toJSONObject = profile.toJSONObject();
        if (toJSONObject != null) {
            this.sharedPreferences.edit().putString(CACHED_PROFILE_KEY, toJSONObject.toString()).apply();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void clear() {
        this.sharedPreferences.edit().remove(CACHED_PROFILE_KEY).apply();
    }
}
