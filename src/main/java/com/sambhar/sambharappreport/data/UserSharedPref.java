package com.sambhar.sambharappreport.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import javax.inject.Inject;

public class UserSharedPref {
    public static final String FACEBOOK_COUNT = "facebook_count";
    public static final String FACEBOOK_STATUS = "facebook_status";
    public static final String INSTAGRAM_COUNT = "instagram_count";
    public static final String INSTAGRAM_STATUS = "instagram_status";
    public static final String IS_LOGIN = "is_login";
    public static final String PREF_NAME = "sambhar_user_pref";
    public static final String TWITTER_COUNT = "twitter_count";
    public static final String TWITTER_STATUS = "twitter_status";
    public static final String USER_TOKEN = "user_token";
    private Editor mEditor = this.mPref.edit();
    private final SharedPreferences mPref;

    @Inject
    public UserSharedPref(Context context) {
        this.mPref = context.getSharedPreferences(PREF_NAME, 0);
    }

    public void setIsLogin(boolean z) {
        this.mEditor.putBoolean(IS_LOGIN, z);
        this.mEditor.commit();
    }

    public boolean isLogin() {
        return this.mPref.getBoolean(IS_LOGIN, false);
    }

    public void setUserToken(String str) {
        this.mEditor.putString(USER_TOKEN, str);
        this.mEditor.apply();
    }

    public String getUserToken() {
        return this.mPref.getString(USER_TOKEN, "Guest Token");
    }

    public void setInstagramCount(int i) {
        this.mEditor.putInt(INSTAGRAM_COUNT, i);
        this.mEditor.apply();
    }

    public int getInstagramCount() {
        return this.mPref.getInt(INSTAGRAM_COUNT, 0);
    }

    public void setInstagramStatus(int i) {
        this.mEditor.putInt(INSTAGRAM_STATUS, i);
        this.mEditor.apply();
    }

    public int getInstagramStatus() {
        return this.mPref.getInt(INSTAGRAM_STATUS, 0);
    }

    public void setFacebookCount(int i) {
        this.mEditor.putInt(FACEBOOK_COUNT, i);
        this.mEditor.apply();
    }

    public int getFacebookCount() {
        return this.mPref.getInt(FACEBOOK_COUNT, 0);
    }

    public void setFacebookStatus(int i) {
        this.mEditor.putInt(FACEBOOK_STATUS, i);
        this.mEditor.apply();
    }

    public int getFacebookStatus() {
        return this.mPref.getInt(FACEBOOK_STATUS, 0);
    }

    public void setTwitterCount(int i) {
        this.mEditor.putInt(TWITTER_COUNT, i);
        this.mEditor.apply();
    }

    public int getTwitterCount() {
        return this.mPref.getInt(TWITTER_COUNT, 0);
    }

    public void setTwitterStatus(int i) {
        this.mEditor.putInt(TWITTER_STATUS, i);
        this.mEditor.apply();
    }

    public int getTwitterStatus() {
        return this.mPref.getInt(TWITTER_STATUS, 0);
    }

    public void clearSession() {
        this.mEditor.clear();
        this.mEditor.commit();
    }
}
