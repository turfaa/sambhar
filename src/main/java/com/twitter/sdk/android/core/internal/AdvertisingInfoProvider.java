package com.twitter.sdk.android.core.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStore;

class AdvertisingInfoProvider {
    private static final String PREFKEY_ADVERTISING_ID = "advertising_id";
    private static final String PREFKEY_LIMIT_AD_TRACKING = "limit_ad_tracking_enabled";
    private final Context context;
    private final PreferenceStore preferenceStore;

    AdvertisingInfoProvider(Context context, PreferenceStore preferenceStore) {
        this.context = context.getApplicationContext();
        this.preferenceStore = preferenceStore;
    }

    /* Access modifiers changed, original: 0000 */
    public AdvertisingInfo getAdvertisingInfo() {
        AdvertisingInfo infoFromPreferences = getInfoFromPreferences();
        if (isInfoValid(infoFromPreferences)) {
            Twitter.getLogger().d("Twitter", "Using AdvertisingInfo from Preference Store");
            refreshInfoIfNeededAsync(infoFromPreferences);
            return infoFromPreferences;
        }
        infoFromPreferences = getAdvertisingInfoFromStrategies();
        storeInfoToPreferences(infoFromPreferences);
        return infoFromPreferences;
    }

    private void refreshInfoIfNeededAsync(final AdvertisingInfo advertisingInfo) {
        new Thread(new Runnable() {
            public void run() {
                AdvertisingInfo access$000 = AdvertisingInfoProvider.this.getAdvertisingInfoFromStrategies();
                if (!advertisingInfo.equals(access$000)) {
                    Twitter.getLogger().d("Twitter", "Asychronously getting Advertising Info and storing it to preferences");
                    AdvertisingInfoProvider.this.storeInfoToPreferences(access$000);
                }
            }
        }).start();
    }

    @SuppressLint({"CommitPrefEdits"})
    private void storeInfoToPreferences(AdvertisingInfo advertisingInfo) {
        if (isInfoValid(advertisingInfo)) {
            this.preferenceStore.save(this.preferenceStore.edit().putString(PREFKEY_ADVERTISING_ID, advertisingInfo.advertisingId).putBoolean(PREFKEY_LIMIT_AD_TRACKING, advertisingInfo.limitAdTrackingEnabled));
        } else {
            this.preferenceStore.save(this.preferenceStore.edit().remove(PREFKEY_ADVERTISING_ID).remove(PREFKEY_LIMIT_AD_TRACKING));
        }
    }

    private AdvertisingInfo getInfoFromPreferences() {
        return new AdvertisingInfo(this.preferenceStore.get().getString(PREFKEY_ADVERTISING_ID, ""), this.preferenceStore.get().getBoolean(PREFKEY_LIMIT_AD_TRACKING, false));
    }

    private AdvertisingInfoStrategy getReflectionStrategy() {
        return new AdvertisingInfoReflectionStrategy(this.context);
    }

    private AdvertisingInfoStrategy getServiceStrategy() {
        return new AdvertisingInfoServiceStrategy(this.context);
    }

    private boolean isInfoValid(AdvertisingInfo advertisingInfo) {
        return (advertisingInfo == null || TextUtils.isEmpty(advertisingInfo.advertisingId)) ? false : true;
    }

    private AdvertisingInfo getAdvertisingInfoFromStrategies() {
        AdvertisingInfo advertisingInfo = getReflectionStrategy().getAdvertisingInfo();
        if (isInfoValid(advertisingInfo)) {
            Twitter.getLogger().d("Twitter", "Using AdvertisingInfo from Reflection Provider");
        } else {
            advertisingInfo = getServiceStrategy().getAdvertisingInfo();
            if (isInfoValid(advertisingInfo)) {
                Twitter.getLogger().d("Twitter", "Using AdvertisingInfo from Service Provider");
            } else {
                Twitter.getLogger().d("Twitter", "AdvertisingInfo not present");
            }
        }
        return advertisingInfo;
    }
}
