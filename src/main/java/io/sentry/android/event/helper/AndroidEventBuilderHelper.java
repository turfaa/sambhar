package io.sentry.android.event.helper;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.facebook.share.internal.MessengerShareContentUtility;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import io.sentry.event.EventBuilder;
import io.sentry.event.helper.EventBuilderHelper;
import io.sentry.event.interfaces.DebugMetaInterface;
import io.sentry.event.interfaces.DebugMetaInterface.DebugImage;
import io.sentry.event.interfaces.UserInterface;
import io.sentry.marshaller.json.JsonMarshaller;
import io.sentry.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class AndroidEventBuilderHelper implements EventBuilderHelper {
    private static final Boolean IS_EMULATOR = isEmulator();
    private static final String KERNEL_VERSION = getKernelVersion();
    public static final String TAG = "io.sentry.android.event.helper.AndroidEventBuilderHelper";
    private static String[] cachedProGuardUuids = null;
    private Context ctx;

    public AndroidEventBuilderHelper(Context context) {
        this.ctx = context;
    }

    public void helpBuildingEvent(EventBuilder eventBuilder) {
        eventBuilder.withSdkIntegration("android");
        PackageInfo packageInfo = getPackageInfo(this.ctx);
        if (packageInfo != null) {
            if (eventBuilder.getEvent().getRelease() == null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(packageInfo.packageName);
                stringBuilder.append("-");
                stringBuilder.append(packageInfo.versionName);
                eventBuilder.withRelease(stringBuilder.toString());
            }
            if (eventBuilder.getEvent().getDist() == null) {
                eventBuilder.withDist(Integer.toString(packageInfo.versionCode));
            }
        }
        String string = Secure.getString(this.ctx.getContentResolver(), "android_id");
        int i = 0;
        if (!(string == null || string.trim().equals(""))) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("android:");
            stringBuilder2.append(string);
            eventBuilder.withSentryInterface(new UserInterface(stringBuilder2.toString(), null, null, null), false);
        }
        String[] proGuardUuids = getProGuardUuids(this.ctx);
        if (proGuardUuids != null && proGuardUuids.length > 0) {
            DebugMetaInterface debugMetaInterface = new DebugMetaInterface();
            int length = proGuardUuids.length;
            while (i < length) {
                debugMetaInterface.addDebugImage(new DebugImage(proGuardUuids[i]));
                i++;
            }
            eventBuilder.withSentryInterface(debugMetaInterface);
        }
        eventBuilder.withContexts(getContexts());
    }

    private Map<String, Map<String, Object>> getContexts() {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        HashMap hashMap3 = new HashMap();
        HashMap hashMap4 = new HashMap();
        hashMap.put("os", hashMap3);
        hashMap.put("device", hashMap2);
        hashMap.put("app", hashMap4);
        hashMap2.put("manufacturer", Build.MANUFACTURER);
        hashMap2.put("brand", Build.BRAND);
        hashMap2.put("model", Build.MODEL);
        hashMap2.put("family", getFamily());
        hashMap2.put("model_id", Build.ID);
        hashMap2.put("battery_level", getBatteryLevel(this.ctx));
        hashMap2.put("orientation", getOrientation(this.ctx));
        hashMap2.put("simulator", IS_EMULATOR);
        hashMap2.put("arch", Build.CPU_ABI);
        hashMap2.put("storage_size", getTotalInternalStorage());
        hashMap2.put("free_storage", getUnusedInternalStorage());
        hashMap2.put("external_storage_size", getTotalExternalStorage());
        hashMap2.put("external_free_storage", getUnusedExternalStorage());
        hashMap2.put("charging", isCharging(this.ctx));
        hashMap2.put("online", Boolean.valueOf(isConnected(this.ctx)));
        DisplayMetrics displayMetrics = getDisplayMetrics(this.ctx);
        if (displayMetrics != null) {
            int max = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Integer.toString(max));
            stringBuilder.append("x");
            stringBuilder.append(Integer.toString(min));
            hashMap2.put("screen_resolution", stringBuilder.toString());
            hashMap2.put("screen_density", Float.valueOf(displayMetrics.density));
            hashMap2.put("screen_dpi", Integer.valueOf(displayMetrics.densityDpi));
        }
        MemoryInfo memInfo = getMemInfo(this.ctx);
        if (memInfo != null) {
            hashMap2.put("free_memory", Long.valueOf(memInfo.availMem));
            if (VERSION.SDK_INT >= 16) {
                hashMap2.put("memory_size", Long.valueOf(memInfo.totalMem));
            }
            hashMap2.put("low_memory", Boolean.valueOf(memInfo.lowMemory));
        }
        hashMap3.put("name", "Android");
        hashMap3.put(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION, VERSION.RELEASE);
        hashMap3.put("build", Build.DISPLAY);
        hashMap3.put("kernel_version", KERNEL_VERSION);
        hashMap3.put("rooted", isRooted());
        PackageInfo packageInfo = getPackageInfo(this.ctx);
        if (packageInfo != null) {
            hashMap4.put("app_version", packageInfo.versionName);
            hashMap4.put("app_build", Integer.valueOf(packageInfo.versionCode));
            hashMap4.put("app_identifier", packageInfo.packageName);
        }
        hashMap4.put(NativeProtocol.BRIDGE_ARG_APP_NAME_STRING, getApplicationName(this.ctx));
        hashMap4.put("app_start_time", stringifyDate(new Date()));
        return hashMap;
    }

    private static String[] getProGuardUuids(Context context) {
        if (cachedProGuardUuids != null) {
            return cachedProGuardUuids;
        }
        String[] strArr = new String[0];
        try {
            InputStream open = context.getAssets().open("sentry-debug-meta.properties");
            Properties properties = new Properties();
            properties.load(open);
            open.close();
            String property = properties.getProperty("io.sentry.ProguardUuids");
            if (!Util.isNullOrEmpty(property)) {
                strArr = property.split("\\|");
            }
        } catch (FileNotFoundException unused) {
            Log.d(TAG, "Proguard UUIDs file not found.");
        } catch (Exception e) {
            Log.e(TAG, "Error getting Proguard UUIDs.", e);
        }
        cachedProGuardUuids = strArr;
        return strArr;
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error getting package info.", e);
            return null;
        }
    }

    private static String getFamily() {
        try {
            return Build.MODEL.split(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR)[0];
        } catch (Exception e) {
            Log.e(TAG, "Error getting device family.", e);
            return null;
        }
    }

    private static Boolean isEmulator() {
        try {
            boolean z;
            if (!(Build.FINGERPRINT.startsWith(MessengerShareContentUtility.TEMPLATE_GENERIC_TYPE) || Build.FINGERPRINT.startsWith("unknown") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") || (Build.BRAND.startsWith(MessengerShareContentUtility.TEMPLATE_GENERIC_TYPE) && Build.DEVICE.startsWith(MessengerShareContentUtility.TEMPLATE_GENERIC_TYPE)))) {
                if (!"google_sdk".equals(Build.PRODUCT)) {
                    z = false;
                    return Boolean.valueOf(z);
                }
            }
            z = true;
            return Boolean.valueOf(z);
        } catch (Exception e) {
            Log.e(TAG, "Error checking whether application is running in an emulator.", e);
            return null;
        }
    }

    private static MemoryInfo getMemInfo(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            MemoryInfo memoryInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo;
        } catch (Exception e) {
            Log.e(TAG, "Error getting MemoryInfo.", e);
            return null;
        }
    }

    private static String getOrientation(Context context) {
        try {
            String str;
            switch (context.getResources().getConfiguration().orientation) {
                case 1:
                    str = "portrait";
                    break;
                case 2:
                    str = "landscape";
                    break;
                default:
                    str = null;
                    break;
            }
            return str;
        } catch (Exception e) {
            Log.e(TAG, "Error getting device orientation.", e);
            return null;
        }
    }

    private static Float getBatteryLevel(Context context) {
        try {
            Intent registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (registerReceiver == null) {
                return null;
            }
            int intExtra = registerReceiver.getIntExtra(JsonMarshaller.LEVEL, -1);
            int intExtra2 = registerReceiver.getIntExtra("scale", -1);
            if (intExtra != -1) {
                if (intExtra2 != -1) {
                    return Float.valueOf((((float) intExtra) / ((float) intExtra2)) * 100.0f);
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting device battery level.", e);
            return null;
        }
    }

    private static Boolean isCharging(Context context) {
        try {
            Intent registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (registerReceiver == null) {
                return null;
            }
            int intExtra = registerReceiver.getIntExtra("plugged", -1);
            boolean z = true;
            if (intExtra != 1) {
                if (intExtra != 2) {
                    z = false;
                }
            }
            return Boolean.valueOf(z);
        } catch (Exception e) {
            Log.e(TAG, "Error getting device charging state.", e);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e A:{SYNTHETIC, Splitter:B:28:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0041 A:{SYNTHETIC, Splitter:B:22:0x0041} */
    private static java.lang.String getKernelVersion() {
        /*
        r0 = "Exception while attempting to read kernel information";
        r1 = "os.version";
        r1 = java.lang.System.getProperty(r1);
        r2 = 0;
        r3 = new java.io.File;	 Catch:{ Exception -> 0x0039 }
        r4 = "/proc/version";
        r3.<init>(r4);	 Catch:{ Exception -> 0x0039 }
        r4 = r3.canRead();	 Catch:{ Exception -> 0x0039 }
        if (r4 != 0) goto L_0x0017;
    L_0x0016:
        return r1;
    L_0x0017:
        r4 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x0039 }
        r5 = new java.io.FileReader;	 Catch:{ Exception -> 0x0039 }
        r5.<init>(r3);	 Catch:{ Exception -> 0x0039 }
        r4.<init>(r5);	 Catch:{ Exception -> 0x0039 }
        r2 = r4.readLine();	 Catch:{ Exception -> 0x0033, all -> 0x0030 }
        r4.close();	 Catch:{ IOException -> 0x0029 }
        goto L_0x002f;
    L_0x0029:
        r1 = move-exception;
        r3 = TAG;
        android.util.Log.e(r3, r0, r1);
    L_0x002f:
        return r2;
    L_0x0030:
        r1 = move-exception;
        r2 = r4;
        goto L_0x004c;
    L_0x0033:
        r2 = move-exception;
        r3 = r2;
        r2 = r4;
        goto L_0x003a;
    L_0x0037:
        r1 = move-exception;
        goto L_0x004c;
    L_0x0039:
        r3 = move-exception;
    L_0x003a:
        r4 = TAG;	 Catch:{ all -> 0x0037 }
        android.util.Log.e(r4, r0, r3);	 Catch:{ all -> 0x0037 }
        if (r2 == 0) goto L_0x004b;
    L_0x0041:
        r2.close();	 Catch:{ IOException -> 0x0045 }
        goto L_0x004b;
    L_0x0045:
        r2 = move-exception;
        r3 = TAG;
        android.util.Log.e(r3, r0, r2);
    L_0x004b:
        return r1;
    L_0x004c:
        if (r2 == 0) goto L_0x0058;
    L_0x004e:
        r2.close();	 Catch:{ IOException -> 0x0052 }
        goto L_0x0058;
    L_0x0052:
        r2 = move-exception;
        r3 = TAG;
        android.util.Log.e(r3, r0, r2);
    L_0x0058:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.android.event.helper.AndroidEventBuilderHelper.getKernelVersion():java.lang.String");
    }

    private static Boolean isRooted() {
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return Boolean.valueOf(true);
        }
        String[] strArr = new String[]{"/data/local/bin/su", "/data/local/su", "/data/local/xbin/su", "/sbin/su", "/su/bin", "/su/bin/su", "/system/app/SuperSU", "/system/app/SuperSU.apk", "/system/app/Superuser", "/system/app/Superuser.apk", "/system/bin/failsafe/su", "/system/bin/su", "/system/sd/xbin/su", "/system/xbin/daemonsu", "/system/xbin/su"};
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            try {
                if (new File(strArr[i]).exists()) {
                    return Boolean.valueOf(true);
                }
                i++;
            } catch (Exception e) {
                Log.e(TAG, "Exception while attempting to detect whether the device is rooted", e);
            }
        }
        return Boolean.valueOf(false);
    }

    private static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals("mounted") && !Environment.isExternalStorageEmulated();
    }

    private static Long getUnusedInternalStorage() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            return Long.valueOf(((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize()));
        } catch (Exception e) {
            Log.e(TAG, "Error getting unused internal storage amount.", e);
            return null;
        }
    }

    private static Long getTotalInternalStorage() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            return Long.valueOf(((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize()));
        } catch (Exception e) {
            Log.e(TAG, "Error getting total internal storage amount.", e);
            return null;
        }
    }

    private static Long getUnusedExternalStorage() {
        try {
            if (isExternalStorageMounted()) {
                StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
                return Long.valueOf(((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unused external storage amount.", e);
        }
        return null;
    }

    private static Long getTotalExternalStorage() {
        try {
            if (isExternalStorageMounted()) {
                StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
                return Long.valueOf(((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total external storage amount.", e);
        }
        return null;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        try {
            return context.getResources().getDisplayMetrics();
        } catch (Exception e) {
            Log.e(TAG, "Error getting DisplayMetrics.", e);
            return null;
        }
    }

    private static String stringifyDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).format(date);
    }

    private static String getApplicationName(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int i = applicationInfo.labelRes;
            if (i != 0) {
                return context.getString(i);
            }
            if (applicationInfo.nonLocalizedLabel != null) {
                return applicationInfo.nonLocalizedLabel.toString();
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting application name.", e);
        }
    }

    private static boolean isConnected(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
