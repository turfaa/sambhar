package com.facebook.appevents.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.FacebookSdk;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

class InAppPurchaseEventManager {
    private static final String AS_INTERFACE = "asInterface";
    private static final int CACHE_CLEAR_TIME_LIMIT_SEC = 604800;
    private static final String DETAILS_LIST = "DETAILS_LIST";
    private static final String GET_PURCHASES = "getPurchases";
    private static final String GET_PURCHASE_HISTORY = "getPurchaseHistory";
    private static final String GET_SKU_DETAILS = "getSkuDetails";
    private static final String INAPP = "inapp";
    private static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
    private static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String IN_APP_BILLING_SERVICE = "com.android.vending.billing.IInAppBillingService";
    private static final String IN_APP_BILLING_SERVICE_STUB = "com.android.vending.billing.IInAppBillingService$Stub";
    private static final String IS_BILLING_SUPPORTED = "isBillingSupported";
    private static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    private static final String LAST_CLEARED_TIME = "LAST_CLEARED_TIME";
    private static final String LAST_LOGGED_TIME_SEC = "LAST_LOGGED_TIME_SEC";
    private static final int MAX_QUERY_PURCHASE_NUM = 30;
    private static final String PACKAGE_NAME = FacebookSdk.getApplicationContext().getPackageName();
    private static final int PURCHASE_EXPIRE_TIME_SEC = 43200;
    private static final String PURCHASE_INAPP_STORE = "com.facebook.internal.PURCHASE";
    private static final int PURCHASE_STOP_QUERY_TIME_SEC = 1200;
    private static final String PURCHASE_SUBS_STORE = "com.facebook.internal.PURCHASE_SUBS";
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_STORE = "com.facebook.internal.SKU_DETAILS";
    private static final int SKU_DETAIL_EXPIRE_TIME_SEC = 43200;
    private static final String SUBSCRIPTION = "subs";
    private static final long SUBSCRIPTION_HARTBEAT_INTERVAL = 86400;
    private static final String TAG = InAppPurchaseEventManager.class.getCanonicalName();
    private static final HashMap<String, Class<?>> classMap = new HashMap();
    private static final HashMap<String, Method> methodMap = new HashMap();
    private static final SharedPreferences purchaseInappSharedPrefs = FacebookSdk.getApplicationContext().getSharedPreferences(PURCHASE_INAPP_STORE, 0);
    private static final SharedPreferences purchaseSubsSharedPrefs = FacebookSdk.getApplicationContext().getSharedPreferences(PURCHASE_SUBS_STORE, 0);
    private static final SharedPreferences skuDetailSharedPrefs = FacebookSdk.getApplicationContext().getSharedPreferences(SKU_DETAILS_STORE, 0);

    InAppPurchaseEventManager() {
    }

    @Nullable
    public static Object asInterface(Context context, IBinder iBinder) {
        return invokeMethod(context, IN_APP_BILLING_SERVICE_STUB, AS_INTERFACE, null, new Object[]{iBinder});
    }

    public static Map<String, String> getSkuDetails(Context context, ArrayList<String> arrayList, Object obj, boolean z) {
        Map readSkuDetailsFromCache = readSkuDetailsFromCache(arrayList);
        ArrayList arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (!readSkuDetailsFromCache.containsKey(str)) {
                arrayList2.add(str);
            }
        }
        readSkuDetailsFromCache.putAll(getSkuDetailsFromGoogle(context, arrayList2, obj, z));
        return readSkuDetailsFromCache;
    }

    private static Map<String, String> getSkuDetailsFromGoogle(Context context, ArrayList<String> arrayList, Object obj, boolean z) {
        HashMap hashMap = new HashMap();
        if (obj == null || arrayList.isEmpty()) {
            return hashMap;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(ITEM_ID_LIST, arrayList);
        Object[] objArr = new Object[4];
        int i = 0;
        objArr[0] = Integer.valueOf(3);
        objArr[1] = PACKAGE_NAME;
        objArr[2] = z ? SUBSCRIPTION : INAPP;
        objArr[3] = bundle;
        Object invokeMethod = invokeMethod(context, IN_APP_BILLING_SERVICE, GET_SKU_DETAILS, obj, objArr);
        if (invokeMethod != null) {
            Bundle bundle2 = (Bundle) invokeMethod;
            if (bundle2.getInt(RESPONSE_CODE) == 0) {
                ArrayList stringArrayList = bundle2.getStringArrayList(DETAILS_LIST);
                if (stringArrayList != null && arrayList.size() == stringArrayList.size()) {
                    while (i < arrayList.size()) {
                        hashMap.put(arrayList.get(i), stringArrayList.get(i));
                        i++;
                    }
                }
                writeSkuDetailsToCache(hashMap);
            }
        }
        return hashMap;
    }

    private static Map<String, String> readSkuDetailsFromCache(ArrayList<String> arrayList) {
        HashMap hashMap = new HashMap();
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            String string = skuDetailSharedPrefs.getString(str, null);
            if (string != null) {
                String[] split = string.split(";", 2);
                if (currentTimeMillis - Long.parseLong(split[0]) < 43200) {
                    hashMap.put(str, split[1]);
                }
            }
        }
        return hashMap;
    }

    private static void writeSkuDetailsToCache(Map<String, String> map) {
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        Editor edit = skuDetailSharedPrefs.edit();
        for (Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(currentTimeMillis);
            stringBuilder.append(";");
            stringBuilder.append((String) entry.getValue());
            edit.putString(str, stringBuilder.toString());
        }
        edit.apply();
    }

    private static Boolean isBillingSupported(Context context, Object obj, String str) {
        boolean z = false;
        if (obj == null) {
            return Boolean.valueOf(false);
        }
        Object invokeMethod = invokeMethod(context, IN_APP_BILLING_SERVICE, IS_BILLING_SUPPORTED, obj, new Object[]{Integer.valueOf(3), PACKAGE_NAME, str});
        if (invokeMethod != null && ((Integer) invokeMethod).intValue() == 0) {
            z = true;
        }
        return Boolean.valueOf(z);
    }

    public static ArrayList<String> getPurchasesInapp(Context context, Object obj) {
        return filterPurchasesInapp(getPurchases(context, obj, INAPP));
    }

    public static ArrayList<String> getPurchasesSubsExpire(Context context, Object obj) {
        ArrayList arrayList = new ArrayList();
        Map all = purchaseSubsSharedPrefs.getAll();
        if (all.isEmpty()) {
            return arrayList;
        }
        String str;
        ArrayList purchases = getPurchases(context, obj, SUBSCRIPTION);
        HashSet hashSet = new HashSet();
        Iterator it = purchases.iterator();
        while (it.hasNext()) {
            try {
                hashSet.add(new JSONObject((String) it.next()).getString("productId"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing purchase json", e);
            }
        }
        HashSet<String> hashSet2 = new HashSet();
        for (Entry key : all.entrySet()) {
            str = (String) key.getKey();
            if (!hashSet.contains(str)) {
                hashSet2.add(str);
            }
        }
        Editor edit = purchaseSubsSharedPrefs.edit();
        for (String str2 : hashSet2) {
            str = purchaseSubsSharedPrefs.getString(str2, "");
            edit.remove(str2);
            if (!str.isEmpty()) {
                arrayList.add(purchaseSubsSharedPrefs.getString(str2, ""));
            }
        }
        edit.apply();
        return arrayList;
    }

    public static Map<String, SubscriptionType> getPurchasesSubs(Context context, Object obj) {
        HashMap hashMap = new HashMap();
        Iterator it = getPurchases(context, obj, SUBSCRIPTION).iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            SubscriptionType subsType = getSubsType(str);
            if (!(subsType == SubscriptionType.DUPLICATED || subsType == SubscriptionType.UNKNOWN)) {
                hashMap.put(str, subsType);
            }
        }
        return hashMap;
    }

    private static SubscriptionType getSubsType(String str) {
        SubscriptionType subscriptionType = null;
        try {
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            JSONObject jSONObject = new JSONObject(str);
            str = jSONObject.getString("productId");
            String string = purchaseSubsSharedPrefs.getString(str, "");
            JSONObject jSONObject2 = string.isEmpty() ? new JSONObject() : new JSONObject(string);
            if (!jSONObject2.optString("purchaseToken").equals(jSONObject.get("purchaseToken"))) {
                subscriptionType = currentTimeMillis - (jSONObject.getLong("purchaseTime") / 1000) < 43200 ? SubscriptionType.NEW : SubscriptionType.HEARTBEAT;
            }
            if (subscriptionType == null && !string.isEmpty()) {
                boolean z = jSONObject2.getBoolean("autoRenewing");
                boolean z2 = jSONObject.getBoolean("autoRenewing");
                if (!z2 && z) {
                    subscriptionType = SubscriptionType.CANCEL;
                } else if (!z && z2) {
                    subscriptionType = SubscriptionType.RESTORE;
                }
            }
            if (subscriptionType == null && !string.isEmpty()) {
                if (currentTimeMillis - jSONObject2.getLong(LAST_LOGGED_TIME_SEC) > SUBSCRIPTION_HARTBEAT_INTERVAL) {
                    subscriptionType = SubscriptionType.HEARTBEAT;
                } else {
                    subscriptionType = SubscriptionType.DUPLICATED;
                }
            }
            if (subscriptionType != SubscriptionType.DUPLICATED) {
                jSONObject.put(LAST_LOGGED_TIME_SEC, currentTimeMillis);
                purchaseSubsSharedPrefs.edit().putString(str, jSONObject.toString()).apply();
            }
            return subscriptionType;
        } catch (JSONException e) {
            Log.e(TAG, "parsing purchase failure: ", e);
            return SubscriptionType.UNKNOWN;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x005c A:{SYNTHETIC, EDGE_INSN: B:19:0x005c->B:17:0x005c ?: BREAK  } */
    private static java.util.ArrayList<java.lang.String> getPurchases(android.content.Context r9, java.lang.Object r10, java.lang.String r11) {
        /*
        r0 = new java.util.ArrayList;
        r0.<init>();
        if (r10 != 0) goto L_0x0008;
    L_0x0007:
        return r0;
    L_0x0008:
        r1 = isBillingSupported(r9, r10, r11);
        r1 = r1.booleanValue();
        if (r1 == 0) goto L_0x005c;
    L_0x0012:
        r1 = 0;
        r2 = 0;
        r3 = r2;
        r4 = 0;
    L_0x0016:
        r5 = 4;
        r5 = new java.lang.Object[r5];
        r6 = 3;
        r7 = java.lang.Integer.valueOf(r6);
        r5[r1] = r7;
        r7 = 1;
        r8 = PACKAGE_NAME;
        r5[r7] = r8;
        r7 = 2;
        r5[r7] = r11;
        r5[r6] = r3;
        r3 = "com.android.vending.billing.IInAppBillingService";
        r6 = "getPurchases";
        r3 = invokeMethod(r9, r3, r6, r10, r5);
        if (r3 == 0) goto L_0x0055;
    L_0x0034:
        r3 = (android.os.Bundle) r3;
        r5 = "RESPONSE_CODE";
        r5 = r3.getInt(r5);
        if (r5 != 0) goto L_0x0055;
    L_0x003e:
        r5 = "INAPP_PURCHASE_DATA_LIST";
        r5 = r3.getStringArrayList(r5);
        if (r5 == 0) goto L_0x005c;
    L_0x0046:
        r6 = r5.size();
        r4 = r4 + r6;
        r0.addAll(r5);
        r5 = "INAPP_CONTINUATION_TOKEN";
        r3 = r3.getString(r5);
        goto L_0x0056;
    L_0x0055:
        r3 = r2;
    L_0x0056:
        r5 = 30;
        if (r4 >= r5) goto L_0x005c;
    L_0x005a:
        if (r3 != 0) goto L_0x0016;
    L_0x005c:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.appevents.internal.InAppPurchaseEventManager.getPurchases(android.content.Context, java.lang.Object, java.lang.String):java.util.ArrayList");
    }

    public static ArrayList<String> getPurchaseHistoryInapp(Context context, Object obj) {
        ArrayList arrayList = new ArrayList();
        if (obj == null) {
            return arrayList;
        }
        Class cls = getClass(context, IN_APP_BILLING_SERVICE);
        if (cls == null || getMethod(cls, GET_PURCHASE_HISTORY) == null) {
            return arrayList;
        }
        return filterPurchasesInapp(getPurchaseHistory(context, obj, INAPP));
    }

    private static ArrayList<String> getPurchaseHistory(Context context, Object obj, String str) {
        ArrayList arrayList = new ArrayList();
        if (isBillingSupported(context, obj, str).booleanValue()) {
            int i = 0;
            Boolean valueOf = Boolean.valueOf(false);
            Object obj2 = null;
            int i2 = 0;
            while (true) {
                Context context2 = context;
                obj2 = invokeMethod(context2, IN_APP_BILLING_SERVICE, GET_PURCHASE_HISTORY, obj, new Object[]{Integer.valueOf(6), PACKAGE_NAME, str, obj2, new Bundle()});
                if (obj2 != null) {
                    long currentTimeMillis = System.currentTimeMillis() / 1000;
                    Bundle bundle = (Bundle) obj2;
                    if (bundle.getInt(RESPONSE_CODE) == 0) {
                        Boolean valueOf2;
                        Iterator it = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST).iterator();
                        while (it.hasNext()) {
                            String str2 = (String) it.next();
                            try {
                                if (currentTimeMillis - (new JSONObject(str2).getLong("purchaseTime") / 1000) > 1200) {
                                    valueOf2 = Boolean.valueOf(true);
                                    break;
                                }
                                arrayList.add(str2);
                                i2++;
                            } catch (JSONException e) {
                                Log.e(TAG, "parsing purchase failure: ", e);
                            }
                        }
                        valueOf2 = valueOf;
                        valueOf = valueOf2;
                        obj2 = bundle.getString(INAPP_CONTINUATION_TOKEN);
                        if (i2 >= 30 || obj2 == null || valueOf.booleanValue()) {
                            break;
                        }
                        i = 0;
                    }
                }
                obj2 = null;
                i = 0;
            }
        }
        return arrayList;
    }

    private static ArrayList<String> filterPurchasesInapp(ArrayList<String> arrayList) {
        ArrayList arrayList2 = new ArrayList();
        Editor edit = purchaseInappSharedPrefs.edit();
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            try {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString("productId");
                long j = jSONObject.getLong("purchaseTime");
                String string2 = jSONObject.getString("purchaseToken");
                if (currentTimeMillis - (j / 1000) <= 43200) {
                    if (!purchaseInappSharedPrefs.getString(string, "").equals(string2)) {
                        edit.putString(string, string2);
                        arrayList2.add(str);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "parsing purchase failure: ", e);
            }
        }
        edit.apply();
        return arrayList2;
    }

    @Nullable
    private static Method getMethod(Class<?> cls, String str) {
        Throwable e;
        String str2;
        StringBuilder stringBuilder;
        Method method = (Method) methodMap.get(str);
        if (method != null) {
            return method;
        }
        Class[] clsArr = null;
        Object obj = -1;
        try {
            switch (str.hashCode()) {
                case -1801122596:
                    if (str.equals(GET_PURCHASES)) {
                        obj = 3;
                        break;
                    }
                    break;
                case -1450694211:
                    if (str.equals(IS_BILLING_SUPPORTED)) {
                        obj = 2;
                        break;
                    }
                    break;
                case -1123215065:
                    if (str.equals(AS_INTERFACE)) {
                        obj = null;
                        break;
                    }
                    break;
                case -594356707:
                    if (str.equals(GET_PURCHASE_HISTORY)) {
                        obj = 4;
                        break;
                    }
                    break;
                case -573310373:
                    if (str.equals(GET_SKU_DETAILS)) {
                        obj = 1;
                        break;
                    }
                    break;
                default:
                    break;
            }
            switch (obj) {
                case null:
                    clsArr = new Class[]{IBinder.class};
                    break;
                case 1:
                    clsArr = new Class[]{Integer.TYPE, String.class, String.class, Bundle.class};
                    break;
                case 2:
                    clsArr = new Class[]{Integer.TYPE, String.class, String.class};
                    break;
                case 3:
                    clsArr = new Class[]{Integer.TYPE, String.class, String.class, String.class};
                    break;
                case 4:
                    clsArr = new Class[]{Integer.TYPE, String.class, String.class, String.class, Bundle.class};
                    break;
                default:
                    break;
            }
            Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
            try {
                methodMap.put(str, declaredMethod);
                method = declaredMethod;
            } catch (NoSuchMethodException e2) {
                Method method2 = declaredMethod;
                e = e2;
                method = method2;
                str2 = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append(cls.getName());
                stringBuilder.append(".");
                stringBuilder.append(str);
                stringBuilder.append(" method not found");
                Log.e(str2, stringBuilder.toString(), e);
                return method;
            }
        } catch (NoSuchMethodException e3) {
            e = e3;
            str2 = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append(cls.getName());
            stringBuilder.append(".");
            stringBuilder.append(str);
            stringBuilder.append(" method not found");
            Log.e(str2, stringBuilder.toString(), e);
            return method;
        }
        return method;
    }

    @Nullable
    private static Class<?> getClass(Context context, String str) {
        Throwable e;
        Class<?> cls = (Class) classMap.get(str);
        if (cls != null) {
            return cls;
        }
        Class<?> loadClass;
        try {
            loadClass = context.getClassLoader().loadClass(str);
            try {
                classMap.put(str, loadClass);
            } catch (ClassNotFoundException e2) {
                Throwable th = e2;
                cls = loadClass;
                e = th;
            }
        } catch (ClassNotFoundException e3) {
            e = e3;
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(" is not available, please add ");
            stringBuilder.append(str);
            stringBuilder.append(" to the project.");
            Log.e(str2, stringBuilder.toString(), e);
            loadClass = cls;
            return loadClass;
        }
        return loadClass;
    }

    @Nullable
    private static Object invokeMethod(Context context, String str, String str2, Object obj, Object[] objArr) {
        String str3;
        StringBuilder stringBuilder;
        Class cls = getClass(context, str);
        if (cls == null) {
            return null;
        }
        Method method = getMethod(cls, str2);
        if (method == null) {
            return null;
        }
        if (obj != null) {
            obj = cls.cast(obj);
        }
        try {
            return method.invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            str3 = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal access to method ");
            stringBuilder.append(cls.getName());
            stringBuilder.append(".");
            stringBuilder.append(method.getName());
            Log.e(str3, stringBuilder.toString(), e);
            return null;
        } catch (InvocationTargetException e2) {
            str3 = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Invocation target exception in ");
            stringBuilder.append(cls.getName());
            stringBuilder.append(".");
            stringBuilder.append(method.getName());
            Log.e(str3, stringBuilder.toString(), e2);
            return null;
        }
    }

    public static void clearSkuDetailsCache() {
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        long j = skuDetailSharedPrefs.getLong(LAST_CLEARED_TIME, 0);
        if (j == 0) {
            skuDetailSharedPrefs.edit().putLong(LAST_CLEARED_TIME, currentTimeMillis).apply();
        } else if (currentTimeMillis - j > 604800) {
            skuDetailSharedPrefs.edit().clear().putLong(LAST_CLEARED_TIME, currentTimeMillis).apply();
        }
    }
}
