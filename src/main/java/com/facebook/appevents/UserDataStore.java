package com.facebook.appevents;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.appevents.internal.AppEventUtility;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDataStore {
    public static final String CITY = "ct";
    public static final String COUNTRY = "country";
    public static final String DATE_OF_BIRTH = "db";
    public static final String EMAIL = "em";
    public static final String FIRST_NAME = "fn";
    public static final String GENDER = "ge";
    public static final String LAST_NAME = "ln";
    public static final String PHONE = "ph";
    public static final String STATE = "st";
    private static final String TAG = "UserDataStore";
    private static final String USER_DATA_KEY = "com.facebook.appevents.UserDataStore.userData";
    public static final String ZIP = "zp";
    private static String hashedUserData;
    private static volatile boolean initialized = false;
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void initStore() {
        if (!initialized) {
            AppEventsLogger.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    UserDataStore.initAndWait();
                }
            });
        }
    }

    public static void setUserDataAndHash(final Bundle bundle) {
        if (!initialized) {
            Log.w(TAG, "initStore should have been called before calling setUserData");
            initAndWait();
        }
        AppEventsLogger.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                UserDataStore.lock.writeLock().lock();
                try {
                    UserDataStore.hashedUserData = UserDataStore.hashUserData(bundle);
                    Editor edit = PreferenceManager.getDefaultSharedPreferences(FacebookSdk.getApplicationContext()).edit();
                    edit.putString(UserDataStore.USER_DATA_KEY, UserDataStore.hashedUserData);
                    edit.apply();
                } finally {
                    UserDataStore.lock.writeLock().unlock();
                }
            }
        });
    }

    public static void setUserDataAndHash(@Nullable String str, @Nullable String str2, @Nullable String str3, @Nullable String str4, @Nullable String str5, @Nullable String str6, @Nullable String str7, @Nullable String str8, @Nullable String str9, @Nullable String str10) {
        Bundle bundle = new Bundle();
        if (str != null) {
            bundle.putString("em", str);
        }
        if (str2 != null) {
            bundle.putString(FIRST_NAME, str2);
        }
        if (str3 != null) {
            bundle.putString(LAST_NAME, str3);
        }
        if (str4 != null) {
            bundle.putString(PHONE, str4);
        }
        if (str5 != null) {
            bundle.putString(DATE_OF_BIRTH, str5);
        }
        if (str6 != null) {
            bundle.putString(GENDER, str6);
        }
        if (str7 != null) {
            bundle.putString(CITY, str7);
        }
        if (str8 != null) {
            bundle.putString(STATE, str8);
        }
        if (str9 != null) {
            bundle.putString(ZIP, str9);
        }
        if (str10 != null) {
            bundle.putString(COUNTRY, str10);
        }
        setUserDataAndHash(bundle);
    }

    public static String getHashedUserData() {
        if (!initialized) {
            Log.w(TAG, "initStore should have been called before calling setUserID");
            initAndWait();
        }
        lock.readLock().lock();
        try {
            String str = hashedUserData;
            return str;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static void initAndWait() {
        if (!initialized) {
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    hashedUserData = PreferenceManager.getDefaultSharedPreferences(FacebookSdk.getApplicationContext()).getString(USER_DATA_KEY, null);
                    initialized = true;
                    lock.writeLock().unlock();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private static String hashUserData(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        for (String str : bundle.keySet()) {
            try {
                String obj = bundle.get(str).toString();
                if (maybeSHA256Hashed(obj)) {
                    jSONObject.put(str, obj.toLowerCase());
                } else {
                    obj = encryptData(normalizeData(str, bundle.get(str).toString()));
                    if (obj != null) {
                        jSONObject.put(str, obj);
                    }
                }
            } catch (JSONException unused) {
            }
        }
        return jSONObject.toString();
    }

    private static String encryptData(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(str.getBytes());
            return AppEventUtility.bytesToHex(instance.digest());
        } catch (NoSuchAlgorithmException unused) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f  */
    private static java.lang.String normalizeData(java.lang.String r5, java.lang.String r6) {
        /*
        r0 = "";
        r1 = r5.hashCode();
        r2 = 3185; // 0xc71 float:4.463E-42 double:1.5736E-320;
        r3 = 1;
        r4 = 0;
        if (r1 == r2) goto L_0x0070;
    L_0x000c:
        r2 = 3240; // 0xca8 float:4.54E-42 double:1.601E-320;
        if (r1 == r2) goto L_0x0066;
    L_0x0010:
        r2 = 3272; // 0xcc8 float:4.585E-42 double:1.6166E-320;
        if (r1 == r2) goto L_0x005c;
    L_0x0014:
        r2 = 3294; // 0xcde float:4.616E-42 double:1.6275E-320;
        if (r1 == r2) goto L_0x0052;
    L_0x0018:
        r2 = 3458; // 0xd82 float:4.846E-42 double:1.7085E-320;
        if (r1 == r2) goto L_0x0048;
    L_0x001c:
        r2 = 3576; // 0xdf8 float:5.011E-42 double:1.767E-320;
        if (r1 == r2) goto L_0x003e;
    L_0x0020:
        r2 = 3681; // 0xe61 float:5.158E-42 double:1.8187E-320;
        if (r1 == r2) goto L_0x0034;
    L_0x0024:
        r2 = 957831062; // 0x39175796 float:1.443311E-4 double:4.732314223E-315;
        if (r1 == r2) goto L_0x002a;
    L_0x0029:
        goto L_0x007a;
    L_0x002a:
        r1 = "country";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x0032:
        r5 = 5;
        goto L_0x007b;
    L_0x0034:
        r1 = "st";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x003c:
        r5 = 4;
        goto L_0x007b;
    L_0x003e:
        r1 = "ph";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x0046:
        r5 = 6;
        goto L_0x007b;
    L_0x0048:
        r1 = "ln";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x0050:
        r5 = 2;
        goto L_0x007b;
    L_0x0052:
        r1 = "ge";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x005a:
        r5 = 7;
        goto L_0x007b;
    L_0x005c:
        r1 = "fn";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x0064:
        r5 = 1;
        goto L_0x007b;
    L_0x0066:
        r1 = "em";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x006e:
        r5 = 0;
        goto L_0x007b;
    L_0x0070:
        r1 = "ct";
        r5 = r5.equals(r1);
        if (r5 == 0) goto L_0x007a;
    L_0x0078:
        r5 = 3;
        goto L_0x007b;
    L_0x007a:
        r5 = -1;
    L_0x007b:
        switch(r5) {
            case 0: goto L_0x00a3;
            case 1: goto L_0x00a3;
            case 2: goto L_0x00a3;
            case 3: goto L_0x00a3;
            case 4: goto L_0x00a3;
            case 5: goto L_0x00a3;
            case 6: goto L_0x0096;
            case 7: goto L_0x007f;
            default: goto L_0x007e;
        };
    L_0x007e:
        goto L_0x00ab;
    L_0x007f:
        r5 = r6.trim();
        r5 = r5.toLowerCase();
        r6 = r5.length();
        if (r6 <= 0) goto L_0x0093;
    L_0x008d:
        r5 = r5.substring(r4, r3);
    L_0x0091:
        r0 = r5;
        goto L_0x00ab;
    L_0x0093:
        r5 = "";
        goto L_0x0091;
    L_0x0096:
        r5 = r6.trim();
        r6 = "[^0-9]";
        r0 = "";
        r0 = r5.replaceAll(r6, r0);
        goto L_0x00ab;
    L_0x00a3:
        r5 = r6.trim();
        r0 = r5.toLowerCase();
    L_0x00ab:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.appevents.UserDataStore.normalizeData(java.lang.String, java.lang.String):java.lang.String");
    }

    private static boolean maybeSHA256Hashed(String str) {
        return str.matches("[A-Fa-f0-9]{64}");
    }
}
