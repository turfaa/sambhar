package com.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class LegacyTokenHelper {
    public static final String APPLICATION_ID_KEY = "com.facebook.TokenCachingStrategy.ApplicationId";
    public static final String DECLINED_PERMISSIONS_KEY = "com.facebook.TokenCachingStrategy.DeclinedPermissions";
    public static final String DEFAULT_CACHE_KEY = "com.facebook.SharedPreferencesTokenCachingStrategy.DEFAULT_KEY";
    public static final String EXPIRATION_DATE_KEY = "com.facebook.TokenCachingStrategy.ExpirationDate";
    private static final long INVALID_BUNDLE_MILLISECONDS = Long.MIN_VALUE;
    private static final String IS_SSO_KEY = "com.facebook.TokenCachingStrategy.IsSSO";
    private static final String JSON_VALUE = "value";
    private static final String JSON_VALUE_ENUM_TYPE = "enumType";
    private static final String JSON_VALUE_TYPE = "valueType";
    public static final String LAST_REFRESH_DATE_KEY = "com.facebook.TokenCachingStrategy.LastRefreshDate";
    public static final String PERMISSIONS_KEY = "com.facebook.TokenCachingStrategy.Permissions";
    private static final String TAG = "LegacyTokenHelper";
    public static final String TOKEN_KEY = "com.facebook.TokenCachingStrategy.Token";
    public static final String TOKEN_SOURCE_KEY = "com.facebook.TokenCachingStrategy.AccessTokenSource";
    private static final String TYPE_BOOLEAN = "bool";
    private static final String TYPE_BOOLEAN_ARRAY = "bool[]";
    private static final String TYPE_BYTE = "byte";
    private static final String TYPE_BYTE_ARRAY = "byte[]";
    private static final String TYPE_CHAR = "char";
    private static final String TYPE_CHAR_ARRAY = "char[]";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_DOUBLE_ARRAY = "double[]";
    private static final String TYPE_ENUM = "enum";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_FLOAT_ARRAY = "float[]";
    private static final String TYPE_INTEGER = "int";
    private static final String TYPE_INTEGER_ARRAY = "int[]";
    private static final String TYPE_LONG = "long";
    private static final String TYPE_LONG_ARRAY = "long[]";
    private static final String TYPE_SHORT = "short";
    private static final String TYPE_SHORT_ARRAY = "short[]";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_STRING_LIST = "stringList";
    private SharedPreferences cache;
    private String cacheKey;

    public LegacyTokenHelper(Context context) {
        this(context, null);
    }

    public LegacyTokenHelper(Context context, String str) {
        Validate.notNull(context, "context");
        if (Utility.isNullOrEmpty(str)) {
            str = DEFAULT_CACHE_KEY;
        }
        this.cacheKey = str;
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            context = applicationContext;
        }
        this.cache = context.getSharedPreferences(this.cacheKey, 0);
    }

    public Bundle load() {
        Bundle bundle = new Bundle();
        for (String str : this.cache.getAll().keySet()) {
            try {
                deserializeKey(str, bundle);
            } catch (JSONException e) {
                LoggingBehavior loggingBehavior = LoggingBehavior.CACHE;
                String str2 = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error reading cached value for key: '");
                stringBuilder.append(str);
                stringBuilder.append("' -- ");
                stringBuilder.append(e);
                Logger.log(loggingBehavior, 5, str2, stringBuilder.toString());
                return null;
            }
        }
        return bundle;
    }

    public void save(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        Editor edit = this.cache.edit();
        for (String str : bundle.keySet()) {
            try {
                serializeKey(str, bundle, edit);
            } catch (JSONException e) {
                LoggingBehavior loggingBehavior = LoggingBehavior.CACHE;
                String str2 = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error processing value for key: '");
                stringBuilder.append(str);
                stringBuilder.append("' -- ");
                stringBuilder.append(e);
                Logger.log(loggingBehavior, 5, str2, stringBuilder.toString());
                return;
            }
        }
        edit.apply();
    }

    public void clear() {
        this.cache.edit().clear().apply();
    }

    /* JADX WARNING: Missing block: B:12:0x0022, code skipped:
            return false;
     */
    public static boolean hasTokenInformation(android.os.Bundle r6) {
        /*
        r0 = 0;
        if (r6 != 0) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = "com.facebook.TokenCachingStrategy.Token";
        r1 = r6.getString(r1);
        if (r1 == 0) goto L_0x0022;
    L_0x000c:
        r1 = r1.length();
        if (r1 != 0) goto L_0x0013;
    L_0x0012:
        goto L_0x0022;
    L_0x0013:
        r1 = "com.facebook.TokenCachingStrategy.ExpirationDate";
        r2 = 0;
        r4 = r6.getLong(r1, r2);
        r6 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1));
        if (r6 != 0) goto L_0x0020;
    L_0x001f:
        return r0;
    L_0x0020:
        r6 = 1;
        return r6;
    L_0x0022:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.LegacyTokenHelper.hasTokenInformation(android.os.Bundle):boolean");
    }

    public static String getToken(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return bundle.getString(TOKEN_KEY);
    }

    public static void putToken(Bundle bundle, String str) {
        Validate.notNull(bundle, "bundle");
        Validate.notNull(str, JSON_VALUE);
        bundle.putString(TOKEN_KEY, str);
    }

    public static Date getExpirationDate(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return getDate(bundle, EXPIRATION_DATE_KEY);
    }

    public static void putExpirationDate(Bundle bundle, Date date) {
        Validate.notNull(bundle, "bundle");
        Validate.notNull(date, JSON_VALUE);
        putDate(bundle, EXPIRATION_DATE_KEY, date);
    }

    public static long getExpirationMilliseconds(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return bundle.getLong(EXPIRATION_DATE_KEY);
    }

    public static void putExpirationMilliseconds(Bundle bundle, long j) {
        Validate.notNull(bundle, "bundle");
        bundle.putLong(EXPIRATION_DATE_KEY, j);
    }

    public static Set<String> getPermissions(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        ArrayList stringArrayList = bundle.getStringArrayList(PERMISSIONS_KEY);
        if (stringArrayList == null) {
            return null;
        }
        return new HashSet(stringArrayList);
    }

    public static void putPermissions(Bundle bundle, Collection<String> collection) {
        Validate.notNull(bundle, "bundle");
        Validate.notNull(collection, JSON_VALUE);
        bundle.putStringArrayList(PERMISSIONS_KEY, new ArrayList(collection));
    }

    public static void putDeclinedPermissions(Bundle bundle, Collection<String> collection) {
        Validate.notNull(bundle, "bundle");
        Validate.notNull(collection, JSON_VALUE);
        bundle.putStringArrayList(DECLINED_PERMISSIONS_KEY, new ArrayList(collection));
    }

    public static AccessTokenSource getSource(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        if (bundle.containsKey(TOKEN_SOURCE_KEY)) {
            return (AccessTokenSource) bundle.getSerializable(TOKEN_SOURCE_KEY);
        }
        return bundle.getBoolean(IS_SSO_KEY) ? AccessTokenSource.FACEBOOK_APPLICATION_WEB : AccessTokenSource.WEB_VIEW;
    }

    public static void putSource(Bundle bundle, AccessTokenSource accessTokenSource) {
        Validate.notNull(bundle, "bundle");
        bundle.putSerializable(TOKEN_SOURCE_KEY, accessTokenSource);
    }

    public static Date getLastRefreshDate(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return getDate(bundle, LAST_REFRESH_DATE_KEY);
    }

    public static void putLastRefreshDate(Bundle bundle, Date date) {
        Validate.notNull(bundle, "bundle");
        Validate.notNull(date, JSON_VALUE);
        putDate(bundle, LAST_REFRESH_DATE_KEY, date);
    }

    public static long getLastRefreshMilliseconds(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return bundle.getLong(LAST_REFRESH_DATE_KEY);
    }

    public static void putLastRefreshMilliseconds(Bundle bundle, long j) {
        Validate.notNull(bundle, "bundle");
        bundle.putLong(LAST_REFRESH_DATE_KEY, j);
    }

    public static String getApplicationId(Bundle bundle) {
        Validate.notNull(bundle, "bundle");
        return bundle.getString(APPLICATION_ID_KEY);
    }

    public static void putApplicationId(Bundle bundle, String str) {
        Validate.notNull(bundle, "bundle");
        bundle.putString(APPLICATION_ID_KEY, str);
    }

    static Date getDate(Bundle bundle, String str) {
        if (bundle == null) {
            return null;
        }
        long j = bundle.getLong(str, INVALID_BUNDLE_MILLISECONDS);
        if (j == INVALID_BUNDLE_MILLISECONDS) {
            return null;
        }
        return new Date(j);
    }

    static void putDate(Bundle bundle, String str, Date date) {
        bundle.putLong(str, date.getTime());
    }

    private void serializeKey(String str, Bundle bundle, Editor editor) throws JSONException {
        Object obj = bundle.get(str);
        if (obj != null) {
            Object obj2;
            JSONObject jSONObject = new JSONObject();
            Object obj3 = null;
            if (obj instanceof Byte) {
                obj2 = TYPE_BYTE;
                jSONObject.put(JSON_VALUE, ((Byte) obj).intValue());
            } else if (obj instanceof Short) {
                obj2 = TYPE_SHORT;
                jSONObject.put(JSON_VALUE, ((Short) obj).intValue());
            } else if (obj instanceof Integer) {
                obj2 = TYPE_INTEGER;
                jSONObject.put(JSON_VALUE, ((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                obj2 = TYPE_LONG;
                jSONObject.put(JSON_VALUE, ((Long) obj).longValue());
            } else if (obj instanceof Float) {
                obj2 = TYPE_FLOAT;
                jSONObject.put(JSON_VALUE, ((Float) obj).doubleValue());
            } else if (obj instanceof Double) {
                obj2 = TYPE_DOUBLE;
                jSONObject.put(JSON_VALUE, ((Double) obj).doubleValue());
            } else if (obj instanceof Boolean) {
                obj2 = TYPE_BOOLEAN;
                jSONObject.put(JSON_VALUE, ((Boolean) obj).booleanValue());
            } else if (obj instanceof Character) {
                obj2 = TYPE_CHAR;
                jSONObject.put(JSON_VALUE, obj.toString());
            } else if (obj instanceof String) {
                obj2 = TYPE_STRING;
                jSONObject.put(JSON_VALUE, (String) obj);
            } else if (obj instanceof Enum) {
                obj2 = TYPE_ENUM;
                jSONObject.put(JSON_VALUE, obj.toString());
                jSONObject.put(JSON_VALUE_ENUM_TYPE, obj.getClass().getName());
            } else {
                String str2;
                JSONArray jSONArray = new JSONArray();
                int i = 0;
                int length;
                if (obj instanceof byte[]) {
                    str2 = TYPE_BYTE_ARRAY;
                    byte[] bArr = (byte[]) obj;
                    length = bArr.length;
                    while (i < length) {
                        jSONArray.put(bArr[i]);
                        i++;
                    }
                } else if (obj instanceof short[]) {
                    str2 = TYPE_SHORT_ARRAY;
                    short[] sArr = (short[]) obj;
                    length = sArr.length;
                    while (i < length) {
                        jSONArray.put(sArr[i]);
                        i++;
                    }
                } else if (obj instanceof int[]) {
                    str2 = TYPE_INTEGER_ARRAY;
                    int[] iArr = (int[]) obj;
                    length = iArr.length;
                    while (i < length) {
                        jSONArray.put(iArr[i]);
                        i++;
                    }
                } else if (obj instanceof long[]) {
                    str2 = TYPE_LONG_ARRAY;
                    long[] jArr = (long[]) obj;
                    length = jArr.length;
                    while (i < length) {
                        jSONArray.put(jArr[i]);
                        i++;
                    }
                } else if (obj instanceof float[]) {
                    str2 = TYPE_FLOAT_ARRAY;
                    float[] fArr = (float[]) obj;
                    length = fArr.length;
                    while (i < length) {
                        jSONArray.put((double) fArr[i]);
                        i++;
                    }
                } else if (obj instanceof double[]) {
                    str2 = TYPE_DOUBLE_ARRAY;
                    double[] dArr = (double[]) obj;
                    length = dArr.length;
                    while (i < length) {
                        jSONArray.put(dArr[i]);
                        i++;
                    }
                } else if (obj instanceof boolean[]) {
                    str2 = TYPE_BOOLEAN_ARRAY;
                    boolean[] zArr = (boolean[]) obj;
                    length = zArr.length;
                    while (i < length) {
                        jSONArray.put(zArr[i]);
                        i++;
                    }
                } else if (obj instanceof char[]) {
                    str2 = TYPE_CHAR_ARRAY;
                    char[] cArr = (char[]) obj;
                    length = cArr.length;
                    while (i < length) {
                        jSONArray.put(String.valueOf(cArr[i]));
                        i++;
                    }
                } else if (obj instanceof List) {
                    str2 = TYPE_STRING_LIST;
                    for (Object obj4 : (List) obj) {
                        Object obj42;
                        if (obj42 == null) {
                            obj42 = JSONObject.NULL;
                        }
                        jSONArray.put(obj42);
                    }
                } else {
                    obj2 = null;
                }
                String str3 = str2;
                obj3 = jSONArray;
                obj2 = str3;
            }
            if (obj2 != null) {
                jSONObject.put(JSON_VALUE_TYPE, obj2);
                if (obj3 != null) {
                    jSONObject.putOpt(JSON_VALUE, obj3);
                }
                editor.putString(str, jSONObject.toString());
            }
        }
    }

    private void deserializeKey(String str, Bundle bundle) throws JSONException {
        JSONObject jSONObject = new JSONObject(this.cache.getString(str, "{}"));
        String string = jSONObject.getString(JSON_VALUE_TYPE);
        if (string.equals(TYPE_BOOLEAN)) {
            bundle.putBoolean(str, jSONObject.getBoolean(JSON_VALUE));
            return;
        }
        int i = 0;
        JSONArray jSONArray;
        if (string.equals(TYPE_BOOLEAN_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            boolean[] zArr = new boolean[jSONArray.length()];
            while (i < zArr.length) {
                zArr[i] = jSONArray.getBoolean(i);
                i++;
            }
            bundle.putBooleanArray(str, zArr);
        } else if (string.equals(TYPE_BYTE)) {
            bundle.putByte(str, (byte) jSONObject.getInt(JSON_VALUE));
        } else if (string.equals(TYPE_BYTE_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            byte[] bArr = new byte[jSONArray.length()];
            while (i < bArr.length) {
                bArr[i] = (byte) jSONArray.getInt(i);
                i++;
            }
            bundle.putByteArray(str, bArr);
        } else if (string.equals(TYPE_SHORT)) {
            bundle.putShort(str, (short) jSONObject.getInt(JSON_VALUE));
        } else if (string.equals(TYPE_SHORT_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            short[] sArr = new short[jSONArray.length()];
            while (i < sArr.length) {
                sArr[i] = (short) jSONArray.getInt(i);
                i++;
            }
            bundle.putShortArray(str, sArr);
        } else if (string.equals(TYPE_INTEGER)) {
            bundle.putInt(str, jSONObject.getInt(JSON_VALUE));
        } else if (string.equals(TYPE_INTEGER_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            int[] iArr = new int[jSONArray.length()];
            while (i < iArr.length) {
                iArr[i] = jSONArray.getInt(i);
                i++;
            }
            bundle.putIntArray(str, iArr);
        } else if (string.equals(TYPE_LONG)) {
            bundle.putLong(str, jSONObject.getLong(JSON_VALUE));
        } else if (string.equals(TYPE_LONG_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            long[] jArr = new long[jSONArray.length()];
            while (i < jArr.length) {
                jArr[i] = jSONArray.getLong(i);
                i++;
            }
            bundle.putLongArray(str, jArr);
        } else if (string.equals(TYPE_FLOAT)) {
            bundle.putFloat(str, (float) jSONObject.getDouble(JSON_VALUE));
        } else if (string.equals(TYPE_FLOAT_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            float[] fArr = new float[jSONArray.length()];
            while (i < fArr.length) {
                fArr[i] = (float) jSONArray.getDouble(i);
                i++;
            }
            bundle.putFloatArray(str, fArr);
        } else if (string.equals(TYPE_DOUBLE)) {
            bundle.putDouble(str, jSONObject.getDouble(JSON_VALUE));
        } else if (string.equals(TYPE_DOUBLE_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            double[] dArr = new double[jSONArray.length()];
            while (i < dArr.length) {
                dArr[i] = jSONArray.getDouble(i);
                i++;
            }
            bundle.putDoubleArray(str, dArr);
        } else if (string.equals(TYPE_CHAR)) {
            string = jSONObject.getString(JSON_VALUE);
            if (string != null && string.length() == 1) {
                bundle.putChar(str, string.charAt(0));
            }
        } else if (string.equals(TYPE_CHAR_ARRAY)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            char[] cArr = new char[jSONArray.length()];
            for (int i2 = 0; i2 < cArr.length; i2++) {
                String string2 = jSONArray.getString(i2);
                if (string2 != null && string2.length() == 1) {
                    cArr[i2] = string2.charAt(0);
                }
            }
            bundle.putCharArray(str, cArr);
        } else if (string.equals(TYPE_STRING)) {
            bundle.putString(str, jSONObject.getString(JSON_VALUE));
        } else if (string.equals(TYPE_STRING_LIST)) {
            jSONArray = jSONObject.getJSONArray(JSON_VALUE);
            int length = jSONArray.length();
            ArrayList arrayList = new ArrayList(length);
            while (i < length) {
                Object obj = jSONArray.get(i);
                arrayList.add(i, obj == JSONObject.NULL ? null : (String) obj);
                i++;
            }
            bundle.putStringArrayList(str, arrayList);
        } else if (string.equals(TYPE_ENUM)) {
            try {
                bundle.putSerializable(str, Enum.valueOf(Class.forName(jSONObject.getString(JSON_VALUE_ENUM_TYPE)), jSONObject.getString(JSON_VALUE)));
            } catch (ClassNotFoundException | IllegalArgumentException unused) {
            }
        }
    }
}
