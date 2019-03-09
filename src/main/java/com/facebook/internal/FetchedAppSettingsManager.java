package com.facebook.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.appevents.codeless.internal.UnityReflection;
import com.facebook.appevents.internal.AutomaticAnalyticsLogger;
import com.facebook.appevents.internal.Constants;
import com.facebook.appevents.internal.InAppPurchaseActivityLifecycleTracker;
import com.facebook.internal.FetchedAppSettings.DialogFeatureConfig;
import com.facebook.share.internal.ShareConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class FetchedAppSettingsManager {
    private static final String ADVERTISER_ID_KEY = "advertiser_id";
    private static final String APPLICATION_FIELDS = "fields";
    private static final String APP_SETTINGS_PREFS_KEY_FORMAT = "com.facebook.internal.APP_SETTINGS.%s";
    private static final String APP_SETTINGS_PREFS_STORE = "com.facebook.internal.preferences.APP_SETTINGS";
    private static final String APP_SETTING_ANDROID_SDK_ERROR_CATEGORIES = "android_sdk_error_categories";
    private static final String APP_SETTING_APP_EVENTS_EVENT_BINDINGS = "auto_event_mapping_android";
    private static final String APP_SETTING_APP_EVENTS_FEATURE_BITMASK = "app_events_feature_bitmask";
    private static final String APP_SETTING_APP_EVENTS_SESSION_TIMEOUT = "app_events_session_timeout";
    private static final String APP_SETTING_CUSTOM_TABS_ENABLED = "gdpv4_chrome_custom_tabs_enabled";
    private static final String APP_SETTING_DIALOG_CONFIGS = "android_dialog_configs";
    private static final String[] APP_SETTING_FIELDS = new String[]{APP_SETTING_SUPPORTS_IMPLICIT_SDK_LOGGING, APP_SETTING_NUX_CONTENT, APP_SETTING_NUX_ENABLED, APP_SETTING_CUSTOM_TABS_ENABLED, APP_SETTING_DIALOG_CONFIGS, APP_SETTING_ANDROID_SDK_ERROR_CATEGORIES, APP_SETTING_APP_EVENTS_SESSION_TIMEOUT, APP_SETTING_APP_EVENTS_FEATURE_BITMASK, APP_SETTING_APP_EVENTS_EVENT_BINDINGS, APP_SETTING_SMART_LOGIN_OPTIONS, SMART_LOGIN_BOOKMARK_ICON_URL, SMART_LOGIN_MENU_ICON_URL};
    private static final String APP_SETTING_NUX_CONTENT = "gdpv4_nux_content";
    private static final String APP_SETTING_NUX_ENABLED = "gdpv4_nux_enabled";
    private static final String APP_SETTING_SMART_LOGIN_OPTIONS = "seamless_login";
    private static final String APP_SETTING_SUPPORTS_IMPLICIT_SDK_LOGGING = "supports_implicit_sdk_logging";
    private static final int AUTOMATIC_LOGGING_ENABLED_BITMASK_FIELD = 8;
    private static final int CODELESS_EVENTS_ENABLED_BITMASK_FIELD = 32;
    private static final int IAP_AUTOMATIC_LOGGING_ENABLED_BITMASK_FIELD = 16;
    private static final String SDK_UPDATE_MESSAGE = "sdk_update_message";
    private static final String SMART_LOGIN_BOOKMARK_ICON_URL = "smart_login_bookmark_icon_url";
    private static final String SMART_LOGIN_MENU_ICON_URL = "smart_login_menu_icon_url";
    private static final String TAG = "FetchedAppSettingsManager";
    private static final int TRACK_UNINSTALL_ENABLED_BITMASK_FIELD = 256;
    private static final Map<String, FetchedAppSettings> fetchedAppSettings = new ConcurrentHashMap();
    private static final ConcurrentLinkedQueue<FetchedAppSettingsCallback> fetchedAppSettingsCallbacks = new ConcurrentLinkedQueue();
    private static boolean isUnityInit = false;
    private static final AtomicReference<FetchAppSettingState> loadingState = new AtomicReference(FetchAppSettingState.NOT_LOADED);
    private static boolean printedSDKUpdatedMessage = false;
    @Nullable
    private static JSONArray unityEventBindings = null;

    enum FetchAppSettingState {
        NOT_LOADED,
        LOADING,
        SUCCESS,
        ERROR
    }

    public interface FetchedAppSettingsCallback {
        void onError();

        void onSuccess(FetchedAppSettings fetchedAppSettings);
    }

    public static void loadAppSettingsAsync() {
        final Context applicationContext = FacebookSdk.getApplicationContext();
        final String applicationId = FacebookSdk.getApplicationId();
        if (Utility.isNullOrEmpty(applicationId)) {
            loadingState.set(FetchAppSettingState.ERROR);
            pollCallbacks();
        } else if (fetchedAppSettings.containsKey(applicationId)) {
            loadingState.set(FetchAppSettingState.SUCCESS);
            pollCallbacks();
        } else {
            Object obj = (loadingState.compareAndSet(FetchAppSettingState.NOT_LOADED, FetchAppSettingState.LOADING) || loadingState.compareAndSet(FetchAppSettingState.ERROR, FetchAppSettingState.LOADING)) ? 1 : null;
            if (obj == null) {
                pollCallbacks();
                return;
            }
            final String format = String.format(APP_SETTINGS_PREFS_KEY_FORMAT, new Object[]{applicationId});
            FacebookSdk.getExecutor().execute(new Runnable() {
                public void run() {
                    SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(FetchedAppSettingsManager.APP_SETTINGS_PREFS_STORE, 0);
                    FetchedAppSettings fetchedAppSettings = null;
                    String string = sharedPreferences.getString(format, null);
                    if (!Utility.isNullOrEmpty(string)) {
                        JSONObject jSONObject;
                        try {
                            jSONObject = new JSONObject(string);
                        } catch (JSONException e) {
                            Utility.logd("FacebookSDK", e);
                            jSONObject = null;
                        }
                        if (jSONObject != null) {
                            fetchedAppSettings = FetchedAppSettingsManager.parseAppSettingsFromJSON(applicationId, jSONObject);
                        }
                    }
                    JSONObject access$100 = FetchedAppSettingsManager.getAppSettingsQueryResponse(applicationId);
                    if (access$100 != null) {
                        FetchedAppSettingsManager.parseAppSettingsFromJSON(applicationId, access$100);
                        sharedPreferences.edit().putString(format, access$100.toString()).apply();
                    }
                    if (fetchedAppSettings != null) {
                        string = fetchedAppSettings.getSdkUpdateMessage();
                        if (!(FetchedAppSettingsManager.printedSDKUpdatedMessage || string == null || string.length() <= 0)) {
                            FetchedAppSettingsManager.printedSDKUpdatedMessage = true;
                            Log.w(FetchedAppSettingsManager.TAG, string);
                        }
                    }
                    FetchedAppGateKeepersManager.queryAppGateKeepers(applicationId, true);
                    AutomaticAnalyticsLogger.logActivateAppEvent();
                    InAppPurchaseActivityLifecycleTracker.update();
                    FetchedAppSettingsManager.loadingState.set(FetchedAppSettingsManager.fetchedAppSettings.containsKey(applicationId) ? FetchAppSettingState.SUCCESS : FetchAppSettingState.ERROR);
                    FetchedAppSettingsManager.pollCallbacks();
                }
            });
        }
    }

    @Nullable
    public static FetchedAppSettings getAppSettingsWithoutQuery(String str) {
        return str != null ? (FetchedAppSettings) fetchedAppSettings.get(str) : null;
    }

    public static void getAppSettingsAsync(FetchedAppSettingsCallback fetchedAppSettingsCallback) {
        fetchedAppSettingsCallbacks.add(fetchedAppSettingsCallback);
        loadAppSettingsAsync();
    }

    /* JADX WARNING: Missing block: B:24:0x0070, code skipped:
            return;
     */
    private static synchronized void pollCallbacks() {
        /*
        r0 = com.facebook.internal.FetchedAppSettingsManager.class;
        monitor-enter(r0);
        r1 = loadingState;	 Catch:{ all -> 0x0071 }
        r1 = r1.get();	 Catch:{ all -> 0x0071 }
        r1 = (com.facebook.internal.FetchedAppSettingsManager.FetchAppSettingState) r1;	 Catch:{ all -> 0x0071 }
        r2 = com.facebook.internal.FetchedAppSettingsManager.FetchAppSettingState.NOT_LOADED;	 Catch:{ all -> 0x0071 }
        r2 = r2.equals(r1);	 Catch:{ all -> 0x0071 }
        if (r2 != 0) goto L_0x006f;
    L_0x0013:
        r2 = com.facebook.internal.FetchedAppSettingsManager.FetchAppSettingState.LOADING;	 Catch:{ all -> 0x0071 }
        r2 = r2.equals(r1);	 Catch:{ all -> 0x0071 }
        if (r2 == 0) goto L_0x001c;
    L_0x001b:
        goto L_0x006f;
    L_0x001c:
        r2 = com.facebook.FacebookSdk.getApplicationId();	 Catch:{ all -> 0x0071 }
        r3 = fetchedAppSettings;	 Catch:{ all -> 0x0071 }
        r2 = r3.get(r2);	 Catch:{ all -> 0x0071 }
        r2 = (com.facebook.internal.FetchedAppSettings) r2;	 Catch:{ all -> 0x0071 }
        r3 = new android.os.Handler;	 Catch:{ all -> 0x0071 }
        r4 = android.os.Looper.getMainLooper();	 Catch:{ all -> 0x0071 }
        r3.<init>(r4);	 Catch:{ all -> 0x0071 }
        r4 = com.facebook.internal.FetchedAppSettingsManager.FetchAppSettingState.ERROR;	 Catch:{ all -> 0x0071 }
        r1 = r4.equals(r1);	 Catch:{ all -> 0x0071 }
        if (r1 == 0) goto L_0x0054;
    L_0x0039:
        r1 = fetchedAppSettingsCallbacks;	 Catch:{ all -> 0x0071 }
        r1 = r1.isEmpty();	 Catch:{ all -> 0x0071 }
        if (r1 != 0) goto L_0x0052;
    L_0x0041:
        r1 = fetchedAppSettingsCallbacks;	 Catch:{ all -> 0x0071 }
        r1 = r1.poll();	 Catch:{ all -> 0x0071 }
        r1 = (com.facebook.internal.FetchedAppSettingsManager.FetchedAppSettingsCallback) r1;	 Catch:{ all -> 0x0071 }
        r2 = new com.facebook.internal.FetchedAppSettingsManager$2;	 Catch:{ all -> 0x0071 }
        r2.<init>(r1);	 Catch:{ all -> 0x0071 }
        r3.post(r2);	 Catch:{ all -> 0x0071 }
        goto L_0x0039;
    L_0x0052:
        monitor-exit(r0);
        return;
    L_0x0054:
        r1 = fetchedAppSettingsCallbacks;	 Catch:{ all -> 0x0071 }
        r1 = r1.isEmpty();	 Catch:{ all -> 0x0071 }
        if (r1 != 0) goto L_0x006d;
    L_0x005c:
        r1 = fetchedAppSettingsCallbacks;	 Catch:{ all -> 0x0071 }
        r1 = r1.poll();	 Catch:{ all -> 0x0071 }
        r1 = (com.facebook.internal.FetchedAppSettingsManager.FetchedAppSettingsCallback) r1;	 Catch:{ all -> 0x0071 }
        r4 = new com.facebook.internal.FetchedAppSettingsManager$3;	 Catch:{ all -> 0x0071 }
        r4.<init>(r1, r2);	 Catch:{ all -> 0x0071 }
        r3.post(r4);	 Catch:{ all -> 0x0071 }
        goto L_0x0054;
    L_0x006d:
        monitor-exit(r0);
        return;
    L_0x006f:
        monitor-exit(r0);
        return;
    L_0x0071:
        r1 = move-exception;
        monitor-exit(r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.FetchedAppSettingsManager.pollCallbacks():void");
    }

    @Nullable
    public static FetchedAppSettings queryAppSettings(String str, boolean z) {
        if (!z && fetchedAppSettings.containsKey(str)) {
            return (FetchedAppSettings) fetchedAppSettings.get(str);
        }
        JSONObject appSettingsQueryResponse = getAppSettingsQueryResponse(str);
        if (appSettingsQueryResponse == null) {
            return null;
        }
        FetchedAppSettings parseAppSettingsFromJSON = parseAppSettingsFromJSON(str, appSettingsQueryResponse);
        if (str.equals(FacebookSdk.getApplicationId())) {
            loadingState.set(FetchAppSettingState.SUCCESS);
            pollCallbacks();
        }
        return parseAppSettingsFromJSON;
    }

    private static FetchedAppSettings parseAppSettingsFromJSON(String str, JSONObject jSONObject) {
        FacebookRequestErrorClassification defaultErrorClassification;
        JSONObject jSONObject2 = jSONObject;
        JSONArray optJSONArray = jSONObject2.optJSONArray(APP_SETTING_ANDROID_SDK_ERROR_CATEGORIES);
        if (optJSONArray == null) {
            defaultErrorClassification = FacebookRequestErrorClassification.getDefaultErrorClassification();
        } else {
            defaultErrorClassification = FacebookRequestErrorClassification.createFromJSON(optJSONArray);
        }
        FacebookRequestErrorClassification facebookRequestErrorClassification = defaultErrorClassification;
        int optInt = jSONObject2.optInt(APP_SETTING_APP_EVENTS_FEATURE_BITMASK, 0);
        boolean z = (optInt & 8) != 0;
        boolean z2 = (optInt & 16) != 0;
        boolean z3 = (optInt & 32) != 0;
        boolean z4 = (optInt & 256) != 0;
        JSONArray optJSONArray2 = jSONObject2.optJSONArray(APP_SETTING_APP_EVENTS_EVENT_BINDINGS);
        unityEventBindings = optJSONArray2;
        if (unityEventBindings != null && InternalSettings.isUnityApp()) {
            UnityReflection.sendEventMapping(optJSONArray2.toString());
        }
        FetchedAppSettings fetchedAppSettings = r2;
        FetchedAppSettings fetchedAppSettings2 = new FetchedAppSettings(jSONObject2.optBoolean(APP_SETTING_SUPPORTS_IMPLICIT_SDK_LOGGING, false), jSONObject2.optString(APP_SETTING_NUX_CONTENT, ""), jSONObject2.optBoolean(APP_SETTING_NUX_ENABLED, false), jSONObject2.optBoolean(APP_SETTING_CUSTOM_TABS_ENABLED, false), jSONObject2.optInt(APP_SETTING_APP_EVENTS_SESSION_TIMEOUT, Constants.getDefaultAppEventsSessionTimeoutInSeconds()), SmartLoginOption.parseOptions(jSONObject2.optLong(APP_SETTING_SMART_LOGIN_OPTIONS)), parseDialogConfigurations(jSONObject2.optJSONObject(APP_SETTING_DIALOG_CONFIGS)), z, facebookRequestErrorClassification, jSONObject2.optString(SMART_LOGIN_BOOKMARK_ICON_URL), jSONObject2.optString(SMART_LOGIN_MENU_ICON_URL), z2, z3, optJSONArray2, jSONObject2.optString(SDK_UPDATE_MESSAGE), z4);
        fetchedAppSettings2 = fetchedAppSettings;
        fetchedAppSettings.put(str, fetchedAppSettings2);
        return fetchedAppSettings2;
    }

    public static void setIsUnityInit(boolean z) {
        isUnityInit = z;
        if (unityEventBindings != null && isUnityInit) {
            UnityReflection.sendEventMapping(unityEventBindings.toString());
        }
    }

    private static JSONObject getAppSettingsQueryResponse(String str) {
        Bundle bundle = new Bundle();
        bundle.putString("fields", TextUtils.join(",", new ArrayList(Arrays.asList(APP_SETTING_FIELDS))));
        GraphRequest newGraphPathRequest = GraphRequest.newGraphPathRequest(null, str, null);
        newGraphPathRequest.setSkipClientToken(true);
        newGraphPathRequest.setParameters(bundle);
        return newGraphPathRequest.executeAndWait().getJSONObject();
    }

    private static Map<String, Map<String, DialogFeatureConfig>> parseDialogConfigurations(JSONObject jSONObject) {
        HashMap hashMap = new HashMap();
        if (jSONObject != null) {
            JSONArray optJSONArray = jSONObject.optJSONArray(ShareConstants.WEB_DIALOG_PARAM_DATA);
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    DialogFeatureConfig parseDialogConfig = DialogFeatureConfig.parseDialogConfig(optJSONArray.optJSONObject(i));
                    if (parseDialogConfig != null) {
                        String dialogName = parseDialogConfig.getDialogName();
                        Map map = (Map) hashMap.get(dialogName);
                        if (map == null) {
                            map = new HashMap();
                            hashMap.put(dialogName, map);
                        }
                        map.put(parseDialogConfig.getFeatureName(), parseDialogConfig);
                    }
                }
            }
        }
        return hashMap;
    }
}
