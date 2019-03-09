package com.facebook;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import com.facebook.internal.CustomTab;
import com.sambhar.sambharappreport.data.SambharConstant;

public class CustomTabMainActivity extends Activity {
    public static final String EXTRA_CHROME_PACKAGE;
    public static final String EXTRA_PARAMS;
    public static final String EXTRA_URL;
    private static final String OAUTH_DIALOG = "oauth";
    public static final String REFRESH_ACTION;
    private BroadcastReceiver redirectReceiver;
    private boolean shouldCloseCustomTab = true;

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabMainActivity.class.getSimpleName());
        stringBuilder.append(".extra_params");
        EXTRA_PARAMS = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabMainActivity.class.getSimpleName());
        stringBuilder.append(".extra_chromePackage");
        EXTRA_CHROME_PACKAGE = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabMainActivity.class.getSimpleName());
        stringBuilder.append(".extra_url");
        EXTRA_URL = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(CustomTabMainActivity.class.getSimpleName());
        stringBuilder.append(".action_refresh");
        REFRESH_ACTION = stringBuilder.toString();
    }

    public static final String getRedirectUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SambharConstant.SOURCE_FB);
        stringBuilder.append(FacebookSdk.getApplicationId());
        stringBuilder.append("://authorize");
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (CustomTabActivity.CUSTOM_TAB_REDIRECT_ACTION.equals(getIntent().getAction())) {
            setResult(0);
            finish();
            return;
        }
        if (bundle == null) {
            bundle = getIntent().getBundleExtra(EXTRA_PARAMS);
            new CustomTab(OAUTH_DIALOG, bundle).openCustomTab(this, getIntent().getStringExtra(EXTRA_CHROME_PACKAGE));
            this.shouldCloseCustomTab = false;
            this.redirectReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Intent intent2 = new Intent(CustomTabMainActivity.this, CustomTabMainActivity.class);
                    intent2.setAction(CustomTabMainActivity.REFRESH_ACTION);
                    intent2.putExtra(CustomTabMainActivity.EXTRA_URL, intent.getStringExtra(CustomTabMainActivity.EXTRA_URL));
                    intent2.addFlags(603979776);
                    CustomTabMainActivity.this.startActivity(intent2);
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(this.redirectReceiver, new IntentFilter(CustomTabActivity.CUSTOM_TAB_REDIRECT_ACTION));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (REFRESH_ACTION.equals(intent.getAction())) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CustomTabActivity.DESTROY_ACTION));
            sendResult(-1, intent);
        } else if (CustomTabActivity.CUSTOM_TAB_REDIRECT_ACTION.equals(intent.getAction())) {
            sendResult(-1, intent);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (this.shouldCloseCustomTab) {
            sendResult(0, null);
        }
        this.shouldCloseCustomTab = true;
    }

    private void sendResult(int i, Intent intent) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.redirectReceiver);
        if (intent != null) {
            setResult(i, intent);
        } else {
            setResult(i);
        }
        finish();
    }
}
