package com.facebook.appevents.internal;

import android.content.Context;
import android.os.Bundle;
import com.facebook.AccessToken;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.Utility;
import java.math.BigDecimal;
import java.util.Currency;

class InternalAppEventsLogger extends AppEventsLogger {
    InternalAppEventsLogger(Context context) {
        this(Utility.getActivityName(context), null, null);
    }

    InternalAppEventsLogger(String str, String str2, AccessToken accessToken) {
        super(str, str2, accessToken);
    }

    /* Access modifiers changed, original: protected */
    public void logPurchaseImplicitlyInternal(BigDecimal bigDecimal, Currency currency, Bundle bundle) {
        super.logPurchaseImplicitlyInternal(bigDecimal, currency, bundle);
    }

    /* Access modifiers changed, original: protected */
    public void logEventImplicitly(String str, BigDecimal bigDecimal, Currency currency, Bundle bundle) {
        super.logEventImplicitly(str, bigDecimal, currency, bundle);
    }
}
