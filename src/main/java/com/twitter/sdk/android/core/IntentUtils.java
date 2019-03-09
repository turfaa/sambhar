package com.twitter.sdk.android.core;

import android.content.Context;
import android.content.Intent;

public class IntentUtils {
    public static boolean isActivityAvailable(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 0).isEmpty() ^ 1;
    }

    public static boolean safeStartActivity(Context context, Intent intent) {
        if (!isActivityAvailable(context, intent)) {
            return false;
        }
        context.startActivity(intent);
        return true;
    }
}
