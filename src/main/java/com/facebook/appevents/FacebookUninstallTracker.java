package com.facebook.appevents;

public class FacebookUninstallTracker {
    @Deprecated
    public static void updateDeviceToken(String str) {
        AppEventsLogger.setPushNotificationsRegistrationId(str);
    }
}
