package com.twitter.sdk.android.core.internal;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStore;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStoreImpl;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class IdManager {
    static final String ADVERTISING_PREFERENCES = "com.twitter.sdk.android.AdvertisingPreferences";
    static final String COLLECT_IDENTIFIERS_ENABLED = "com.twitter.sdk.android.COLLECT_IDENTIFIERS_ENABLED";
    private static final String FORWARD_SLASH_REGEX = Pattern.quote("/");
    private static final Pattern ID_PATTERN = Pattern.compile("[^\\p{Alnum}]");
    static final String PREFKEY_INSTALLATION_UUID = "installation_uuid";
    AdvertisingInfo advertisingInfo;
    AdvertisingInfoProvider advertisingInfoProvider;
    private final String appIdentifier;
    private final boolean collectHardwareIds;
    boolean fetchedAdvertisingInfo;
    private final ReentrantLock installationIdLock;
    private final PreferenceStore preferenceStore;

    public IdManager(Context context) {
        this(context, new PreferenceStoreImpl(context, ADVERTISING_PREFERENCES));
    }

    IdManager(Context context, PreferenceStore preferenceStore) {
        this(context, preferenceStore, new AdvertisingInfoProvider(context, preferenceStore));
    }

    IdManager(Context context, PreferenceStore preferenceStore, AdvertisingInfoProvider advertisingInfoProvider) {
        this.installationIdLock = new ReentrantLock();
        if (context != null) {
            this.appIdentifier = context.getPackageName();
            this.advertisingInfoProvider = advertisingInfoProvider;
            this.preferenceStore = preferenceStore;
            this.collectHardwareIds = CommonUtils.getBooleanResourceValue(context, COLLECT_IDENTIFIERS_ENABLED, true);
            if (!this.collectHardwareIds) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Device ID collection disabled for ");
                stringBuilder.append(context.getPackageName());
                Twitter.getLogger().d("Twitter", stringBuilder.toString());
                return;
            }
            return;
        }
        throw new IllegalArgumentException("appContext must not be null");
    }

    private String formatId(String str) {
        return str == null ? null : ID_PATTERN.matcher(str).replaceAll("").toLowerCase(Locale.US);
    }

    public String getAppIdentifier() {
        return this.appIdentifier;
    }

    public String getOsVersionString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getOsDisplayVersionString());
        stringBuilder.append("/");
        stringBuilder.append(getOsBuildVersionString());
        return stringBuilder.toString();
    }

    public String getOsDisplayVersionString() {
        return removeForwardSlashesIn(VERSION.RELEASE);
    }

    public String getOsBuildVersionString() {
        return removeForwardSlashesIn(VERSION.INCREMENTAL);
    }

    public String getModelName() {
        return String.format(Locale.US, "%s/%s", new Object[]{removeForwardSlashesIn(Build.MANUFACTURER), removeForwardSlashesIn(Build.MODEL)});
    }

    private String removeForwardSlashesIn(String str) {
        return str.replaceAll(FORWARD_SLASH_REGEX, "");
    }

    public String getDeviceUUID() {
        String str = "";
        if (!this.collectHardwareIds) {
            return str;
        }
        str = this.preferenceStore.get().getString(PREFKEY_INSTALLATION_UUID, null);
        return str == null ? createInstallationUUID() : str;
    }

    private String createInstallationUUID() {
        this.installationIdLock.lock();
        try {
            String string = this.preferenceStore.get().getString(PREFKEY_INSTALLATION_UUID, null);
            if (string == null) {
                string = formatId(UUID.randomUUID().toString());
                this.preferenceStore.save(this.preferenceStore.edit().putString(PREFKEY_INSTALLATION_UUID, string));
            }
            this.installationIdLock.unlock();
            return string;
        } catch (Throwable th) {
            this.installationIdLock.unlock();
        }
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized AdvertisingInfo getAdvertisingInfo() {
        if (!this.fetchedAdvertisingInfo) {
            this.advertisingInfo = this.advertisingInfoProvider.getAdvertisingInfo();
            this.fetchedAdvertisingInfo = true;
        }
        return this.advertisingInfo;
    }

    public Boolean isLimitAdTrackingEnabled() {
        if (this.collectHardwareIds) {
            AdvertisingInfo advertisingInfo = getAdvertisingInfo();
            if (advertisingInfo != null) {
                return Boolean.valueOf(advertisingInfo.limitAdTrackingEnabled);
            }
        }
        return null;
    }

    public String getAdvertisingId() {
        if (this.collectHardwareIds) {
            AdvertisingInfo advertisingInfo = getAdvertisingInfo();
            if (advertisingInfo != null) {
                return advertisingInfo.advertisingId;
            }
        }
        return null;
    }
}
