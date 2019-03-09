package com.facebook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Base64;
import android.util.Log;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.appevents.internal.ActivityLifecycleTracker;
import com.facebook.appevents.internal.AppEventsLoggerUtility;
import com.facebook.appevents.internal.AppEventsLoggerUtility.GraphAPIActivityType;
import com.facebook.internal.AttributionIdentifiers;
import com.facebook.internal.LockOnGetVariable;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.sambhar.sambharappreport.data.SambharConstant;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

public final class FacebookSdk {
    public static final String ADVERTISER_ID_COLLECTION_ENABLED_PROPERTY = "com.facebook.sdk.AdvertiserIDCollectionEnabled";
    public static final String APPLICATION_ID_PROPERTY = "com.facebook.sdk.ApplicationId";
    public static final String APPLICATION_NAME_PROPERTY = "com.facebook.sdk.ApplicationName";
    private static final String ATTRIBUTION_PREFERENCES = "com.facebook.sdk.attributionTracking";
    public static final String AUTO_LOG_APP_EVENTS_ENABLED_PROPERTY = "com.facebook.sdk.AutoLogAppEventsEnabled";
    static final String CALLBACK_OFFSET_CHANGED_AFTER_INIT = "The callback request code offset can't be updated once the SDK is initialized. Call FacebookSdk.setCallbackRequestCodeOffset inside your Application.onCreate method";
    static final String CALLBACK_OFFSET_NEGATIVE = "The callback request code offset can't be negative.";
    public static final String CALLBACK_OFFSET_PROPERTY = "com.facebook.sdk.CallbackOffset";
    public static final String CLIENT_TOKEN_PROPERTY = "com.facebook.sdk.ClientToken";
    public static final String CODELESS_DEBUG_LOG_ENABLED_PROPERTY = "com.facebook.sdk.CodelessDebugLogEnabled";
    private static final int DEFAULT_CALLBACK_REQUEST_CODE_OFFSET = 64206;
    private static final int DEFAULT_CORE_POOL_SIZE = 5;
    private static final int DEFAULT_KEEP_ALIVE = 1;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 128;
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        public Thread newThread(Runnable runnable) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("FacebookSdk #");
            stringBuilder.append(this.counter.incrementAndGet());
            return new Thread(runnable, stringBuilder.toString());
        }
    };
    private static final BlockingQueue<Runnable> DEFAULT_WORK_QUEUE = new LinkedBlockingQueue(10);
    private static final String FACEBOOK_COM = "facebook.com";
    private static final Object LOCK = new Object();
    private static final int MAX_REQUEST_CODE_RANGE = 100;
    private static final String PUBLISH_ACTIVITY_PATH = "%s/activities";
    private static final String TAG = FacebookSdk.class.getCanonicalName();
    public static final String WEB_DIALOG_THEME = "com.facebook.sdk.WebDialogTheme";
    private static volatile String appClientToken;
    private static Context applicationContext;
    private static volatile String applicationId;
    private static volatile String applicationName;
    private static LockOnGetVariable<File> cacheDir;
    private static int callbackRequestCodeOffset = DEFAULT_CALLBACK_REQUEST_CODE_OFFSET;
    private static volatile Boolean codelessDebugLogEnabled;
    private static Executor executor;
    private static volatile String facebookDomain = FACEBOOK_COM;
    private static String graphApiVersion = ServerProtocol.getDefaultAPIVersion();
    private static volatile boolean isDebugEnabled = false;
    private static boolean isLegacyTokenUpgradeSupported = false;
    private static final HashSet<LoggingBehavior> loggingBehaviors = new HashSet(Arrays.asList(new LoggingBehavior[]{LoggingBehavior.DEVELOPER_ERRORS}));
    private static AtomicLong onProgressThreshold = new AtomicLong(PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH);
    private static Boolean sdkInitialized = Boolean.valueOf(false);

    public interface InitializeCallback {
        void onInitialized();
    }

    public static String getSdkVersion() {
        return FacebookSdkVersion.BUILD;
    }

    @Deprecated
    public static synchronized void sdkInitialize(Context context, int i) {
        synchronized (FacebookSdk.class) {
            sdkInitialize(context, i, null);
        }
    }

    @Deprecated
    public static synchronized void sdkInitialize(Context context, int i, InitializeCallback initializeCallback) {
        synchronized (FacebookSdk.class) {
            if (sdkInitialized.booleanValue()) {
                if (i != callbackRequestCodeOffset) {
                    throw new FacebookException(CALLBACK_OFFSET_CHANGED_AFTER_INIT);
                }
            }
            if (i >= 0) {
                callbackRequestCodeOffset = i;
                sdkInitialize(context, initializeCallback);
            } else {
                throw new FacebookException(CALLBACK_OFFSET_NEGATIVE);
            }
        }
    }

    @Deprecated
    public static synchronized void sdkInitialize(Context context) {
        synchronized (FacebookSdk.class) {
            sdkInitialize(context, null);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0011, code skipped:
            return;
     */
    @java.lang.Deprecated
    public static synchronized void sdkInitialize(final android.content.Context r3, final com.facebook.FacebookSdk.InitializeCallback r4) {
        /*
        r0 = com.facebook.FacebookSdk.class;
        monitor-enter(r0);
        r1 = sdkInitialized;	 Catch:{ all -> 0x007f }
        r1 = r1.booleanValue();	 Catch:{ all -> 0x007f }
        if (r1 == 0) goto L_0x0012;
    L_0x000b:
        if (r4 == 0) goto L_0x0010;
    L_0x000d:
        r4.onInitialized();	 Catch:{ all -> 0x007f }
    L_0x0010:
        monitor-exit(r0);
        return;
    L_0x0012:
        r1 = "applicationContext";
        com.facebook.internal.Validate.notNull(r3, r1);	 Catch:{ all -> 0x007f }
        r1 = 0;
        com.facebook.internal.Validate.hasFacebookActivity(r3, r1);	 Catch:{ all -> 0x007f }
        com.facebook.internal.Validate.hasInternetPermissions(r3, r1);	 Catch:{ all -> 0x007f }
        r1 = r3.getApplicationContext();	 Catch:{ all -> 0x007f }
        applicationContext = r1;	 Catch:{ all -> 0x007f }
        r1 = applicationContext;	 Catch:{ all -> 0x007f }
        loadDefaultsFromMetadata(r1);	 Catch:{ all -> 0x007f }
        r1 = applicationId;	 Catch:{ all -> 0x007f }
        r1 = com.facebook.internal.Utility.isNullOrEmpty(r1);	 Catch:{ all -> 0x007f }
        if (r1 != 0) goto L_0x0077;
    L_0x0031:
        r1 = 1;
        r1 = java.lang.Boolean.valueOf(r1);	 Catch:{ all -> 0x007f }
        sdkInitialized = r1;	 Catch:{ all -> 0x007f }
        r1 = applicationContext;	 Catch:{ all -> 0x007f }
        r1 = r1 instanceof android.app.Application;	 Catch:{ all -> 0x007f }
        if (r1 == 0) goto L_0x004d;
    L_0x003e:
        r1 = com.facebook.UserSettingsManager.getAutoLogAppEventsEnabled();	 Catch:{ all -> 0x007f }
        if (r1 == 0) goto L_0x004d;
    L_0x0044:
        r1 = applicationContext;	 Catch:{ all -> 0x007f }
        r1 = (android.app.Application) r1;	 Catch:{ all -> 0x007f }
        r2 = applicationId;	 Catch:{ all -> 0x007f }
        com.facebook.appevents.internal.ActivityLifecycleTracker.startTracking(r1, r2);	 Catch:{ all -> 0x007f }
    L_0x004d:
        com.facebook.internal.FetchedAppSettingsManager.loadAppSettingsAsync();	 Catch:{ all -> 0x007f }
        com.facebook.internal.NativeProtocol.updateAllAvailableProtocolVersionsAsync();	 Catch:{ all -> 0x007f }
        r1 = applicationContext;	 Catch:{ all -> 0x007f }
        com.facebook.internal.BoltsMeasurementEventListener.getInstance(r1);	 Catch:{ all -> 0x007f }
        r1 = new com.facebook.internal.LockOnGetVariable;	 Catch:{ all -> 0x007f }
        r2 = new com.facebook.FacebookSdk$2;	 Catch:{ all -> 0x007f }
        r2.<init>();	 Catch:{ all -> 0x007f }
        r1.<init>(r2);	 Catch:{ all -> 0x007f }
        cacheDir = r1;	 Catch:{ all -> 0x007f }
        r1 = new java.util.concurrent.FutureTask;	 Catch:{ all -> 0x007f }
        r2 = new com.facebook.FacebookSdk$3;	 Catch:{ all -> 0x007f }
        r2.<init>(r4, r3);	 Catch:{ all -> 0x007f }
        r1.<init>(r2);	 Catch:{ all -> 0x007f }
        r3 = getExecutor();	 Catch:{ all -> 0x007f }
        r3.execute(r1);	 Catch:{ all -> 0x007f }
        monitor-exit(r0);
        return;
    L_0x0077:
        r3 = new com.facebook.FacebookException;	 Catch:{ all -> 0x007f }
        r4 = "A valid Facebook app id must be set in the AndroidManifest.xml or set by calling FacebookSdk.setApplicationId before initializing the sdk.";
        r3.<init>(r4);	 Catch:{ all -> 0x007f }
        throw r3;	 Catch:{ all -> 0x007f }
    L_0x007f:
        r3 = move-exception;
        monitor-exit(r0);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.FacebookSdk.sdkInitialize(android.content.Context, com.facebook.FacebookSdk$InitializeCallback):void");
    }

    public static synchronized boolean isInitialized() {
        boolean booleanValue;
        synchronized (FacebookSdk.class) {
            booleanValue = sdkInitialized.booleanValue();
        }
        return booleanValue;
    }

    public static Set<LoggingBehavior> getLoggingBehaviors() {
        Set unmodifiableSet;
        synchronized (loggingBehaviors) {
            unmodifiableSet = Collections.unmodifiableSet(new HashSet(loggingBehaviors));
        }
        return unmodifiableSet;
    }

    public static void addLoggingBehavior(LoggingBehavior loggingBehavior) {
        synchronized (loggingBehaviors) {
            loggingBehaviors.add(loggingBehavior);
            updateGraphDebugBehavior();
        }
    }

    public static void removeLoggingBehavior(LoggingBehavior loggingBehavior) {
        synchronized (loggingBehaviors) {
            loggingBehaviors.remove(loggingBehavior);
        }
    }

    public static void clearLoggingBehaviors() {
        synchronized (loggingBehaviors) {
            loggingBehaviors.clear();
        }
    }

    public static boolean isLoggingBehaviorEnabled(LoggingBehavior loggingBehavior) {
        boolean z;
        synchronized (loggingBehaviors) {
            z = isDebugEnabled() && loggingBehaviors.contains(loggingBehavior);
        }
        return z;
    }

    public static boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public static void setIsDebugEnabled(boolean z) {
        isDebugEnabled = z;
    }

    public static boolean isLegacyTokenUpgradeSupported() {
        return isLegacyTokenUpgradeSupported;
    }

    private static void updateGraphDebugBehavior() {
        if (loggingBehaviors.contains(LoggingBehavior.GRAPH_API_DEBUG_INFO) && !loggingBehaviors.contains(LoggingBehavior.GRAPH_API_DEBUG_WARNING)) {
            loggingBehaviors.add(LoggingBehavior.GRAPH_API_DEBUG_WARNING);
        }
    }

    public static void setLegacyTokenUpgradeSupported(boolean z) {
        isLegacyTokenUpgradeSupported = z;
    }

    public static Executor getExecutor() {
        synchronized (LOCK) {
            if (executor == null) {
                executor = AsyncTask.THREAD_POOL_EXECUTOR;
            }
        }
        return executor;
    }

    public static void setExecutor(Executor executor) {
        Validate.notNull(executor, "executor");
        synchronized (LOCK) {
            executor = executor;
        }
    }

    public static String getFacebookDomain() {
        return facebookDomain;
    }

    public static void setFacebookDomain(String str) {
        Log.w(TAG, "WARNING: Calling setFacebookDomain from non-DEBUG code.");
        facebookDomain = str;
    }

    public static Context getApplicationContext() {
        Validate.sdkInitialized();
        return applicationContext;
    }

    public static void setGraphApiVersion(String str) {
        Log.w(TAG, "WARNING: Calling setGraphApiVersion from non-DEBUG code.");
        if (!Utility.isNullOrEmpty(str) && !graphApiVersion.equals(str)) {
            graphApiVersion = str;
        }
    }

    public static String getGraphApiVersion() {
        Utility.logd(TAG, String.format("getGraphApiVersion: %s", new Object[]{graphApiVersion}));
        return graphApiVersion;
    }

    public static void publishInstallAsync(Context context, final String str) {
        context = context.getApplicationContext();
        getExecutor().execute(new Runnable() {
            public void run() {
                FacebookSdk.publishInstallAndWaitForResponse(context, str);
            }
        });
    }

    static void publishInstallAndWaitForResponse(Context context, String str) {
        if (context == null || str == null) {
            throw new IllegalArgumentException("Both context and applicationId must be non-null");
        }
        try {
            AttributionIdentifiers attributionIdentifiers = AttributionIdentifiers.getAttributionIdentifiers(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences(ATTRIBUTION_PREFERENCES, 0);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append("ping");
            String stringBuilder2 = stringBuilder.toString();
            long j = sharedPreferences.getLong(stringBuilder2, 0);
            JSONObject jSONObjectForGraphAPICall = AppEventsLoggerUtility.getJSONObjectForGraphAPICall(GraphAPIActivityType.MOBILE_INSTALL_EVENT, attributionIdentifiers, AppEventsLogger.getAnonymousAppDeviceGUID(context), getLimitEventAndDataUsage(context), context);
            GraphRequest newPostRequest = GraphRequest.newPostRequest(null, String.format(PUBLISH_ACTIVITY_PATH, new Object[]{str}), jSONObjectForGraphAPICall, null);
            if (j == 0) {
                newPostRequest.executeAndWait();
                Editor edit = sharedPreferences.edit();
                edit.putLong(stringBuilder2, System.currentTimeMillis());
                edit.apply();
            }
        } catch (JSONException e) {
            throw new FacebookException("An error occurred while publishing install.", e);
        } catch (Exception e2) {
            Utility.logd("Facebook-publish", e2);
        }
    }

    public static boolean getLimitEventAndDataUsage(Context context) {
        Validate.sdkInitialized();
        return context.getSharedPreferences(AppEventsLogger.APP_EVENT_PREFERENCES, 0).getBoolean("limitEventUsage", false);
    }

    public static void setLimitEventAndDataUsage(Context context, boolean z) {
        context.getSharedPreferences(AppEventsLogger.APP_EVENT_PREFERENCES, 0).edit().putBoolean("limitEventUsage", z).apply();
    }

    public static long getOnProgressThreshold() {
        Validate.sdkInitialized();
        return onProgressThreshold.get();
    }

    public static void setOnProgressThreshold(long j) {
        onProgressThreshold.set(j);
    }

    static void loadDefaultsFromMetadata(Context context) {
        if (context != null) {
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
                if (applicationInfo != null && applicationInfo.metaData != null) {
                    if (applicationId == null) {
                        Object obj = applicationInfo.metaData.get(APPLICATION_ID_PROPERTY);
                        if (obj instanceof String) {
                            String str = (String) obj;
                            if (str.toLowerCase(Locale.ROOT).startsWith(SambharConstant.SOURCE_FB)) {
                                applicationId = str.substring(2);
                            } else {
                                applicationId = str;
                            }
                        } else if (obj instanceof Integer) {
                            throw new FacebookException("App Ids cannot be directly placed in the manifest.They must be prefixed by 'fb' or be placed in the string resource file.");
                        }
                    }
                    if (applicationName == null) {
                        applicationName = applicationInfo.metaData.getString(APPLICATION_NAME_PROPERTY);
                    }
                    if (appClientToken == null) {
                        appClientToken = applicationInfo.metaData.getString(CLIENT_TOKEN_PROPERTY);
                    }
                    if (callbackRequestCodeOffset == DEFAULT_CALLBACK_REQUEST_CODE_OFFSET) {
                        callbackRequestCodeOffset = applicationInfo.metaData.getInt(CALLBACK_OFFSET_PROPERTY, DEFAULT_CALLBACK_REQUEST_CODE_OFFSET);
                    }
                    if (codelessDebugLogEnabled == null) {
                        codelessDebugLogEnabled = Boolean.valueOf(applicationInfo.metaData.getBoolean(CODELESS_DEBUG_LOG_ENABLED_PROPERTY, false));
                    }
                }
            } catch (NameNotFoundException unused) {
            }
        }
    }

    public static String getApplicationSignature(Context context) {
        Validate.sdkInitialized();
        if (context == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return null;
        }
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 64);
            Signature[] signatureArr = packageInfo.signatures;
            if (signatureArr == null || signatureArr.length == 0) {
                return null;
            }
            try {
                MessageDigest instance = MessageDigest.getInstance("SHA-1");
                instance.update(packageInfo.signatures[0].toByteArray());
                return Base64.encodeToString(instance.digest(), 9);
            } catch (NoSuchAlgorithmException unused) {
                return null;
            }
        } catch (NameNotFoundException unused2) {
            return null;
        }
    }

    public static String getApplicationId() {
        Validate.sdkInitialized();
        return applicationId;
    }

    public static void setApplicationId(String str) {
        applicationId = str;
    }

    public static String getApplicationName() {
        Validate.sdkInitialized();
        return applicationName;
    }

    public static void setApplicationName(String str) {
        applicationName = str;
    }

    public static String getClientToken() {
        Validate.sdkInitialized();
        return appClientToken;
    }

    public static void setClientToken(String str) {
        appClientToken = str;
    }

    public static boolean getAutoLogAppEventsEnabled() {
        return UserSettingsManager.getAutoLogAppEventsEnabled();
    }

    public static void setAutoLogAppEventsEnabled(boolean z) {
        UserSettingsManager.setAutoLogAppEventsEnabled(z);
        if (z) {
            ActivityLifecycleTracker.startTracking((Application) applicationContext, applicationId);
        }
    }

    public static boolean getCodelessDebugLogEnabled() {
        Validate.sdkInitialized();
        return codelessDebugLogEnabled.booleanValue();
    }

    public static boolean getCodelessSetupEnabled() {
        return UserSettingsManager.getCodelessSetupEnabled();
    }

    public static void setAdvertiserIDCollectionEnabled(boolean z) {
        UserSettingsManager.setAdvertiserIDCollectionEnabled(z);
    }

    public static boolean getAdvertiserIDCollectionEnabled() {
        return UserSettingsManager.getAdvertiserIDCollectionEnabled();
    }

    public static void setCodelessDebugLogEnabled(boolean z) {
        codelessDebugLogEnabled = Boolean.valueOf(z);
    }

    public static File getCacheDir() {
        Validate.sdkInitialized();
        return (File) cacheDir.getValue();
    }

    public static void setCacheDir(File file) {
        cacheDir = new LockOnGetVariable((Object) file);
    }

    public static int getCallbackRequestCodeOffset() {
        Validate.sdkInitialized();
        return callbackRequestCodeOffset;
    }

    public static boolean isFacebookRequestCode(int i) {
        return i >= callbackRequestCodeOffset && i < callbackRequestCodeOffset + 100;
    }
}
