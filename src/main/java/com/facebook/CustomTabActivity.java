package com.facebook;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class CustomTabActivity extends Activity {
    public static final String CUSTOM_TAB_REDIRECT_ACTION;
    private static final int CUSTOM_TAB_REDIRECT_REQUEST_CODE = 2;
    public static final String DESTROY_ACTION;
    private BroadcastReceiver closeReceiver;

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabActivity.class.getSimpleName());
        stringBuilder.append(".action_customTabRedirect");
        CUSTOM_TAB_REDIRECT_ACTION = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabActivity.class.getSimpleName());
        stringBuilder.append(".action_destroy");
        DESTROY_ACTION = stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = new Intent(this, CustomTabMainActivity.class);
        intent.setAction(CUSTOM_TAB_REDIRECT_ACTION);
        intent.putExtra(CustomTabMainActivity.EXTRA_URL, getIntent().getDataString());
        intent.addFlags(603979776);
        startActivityForResult(intent, 2);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == 0) {
            Intent intent2 = new Intent(CUSTOM_TAB_REDIRECT_ACTION);
            intent2.putExtra(CustomTabMainActivity.EXTRA_URL, getIntent().getDataString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
            this.closeReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    CustomTabActivity.this.finish();
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(this.closeReceiver, new IntentFilter(DESTROY_ACTION));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.closeReceiver);
        super.onDestroy();
    }
}
