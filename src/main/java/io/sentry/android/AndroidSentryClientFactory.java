package io.sentry.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import io.sentry.DefaultSentryClientFactory;
import io.sentry.SentryClient;
import io.sentry.android.event.helper.AndroidEventBuilderHelper;
import io.sentry.buffer.Buffer;
import io.sentry.buffer.DiskBuffer;
import io.sentry.config.Lookup;
import io.sentry.context.ContextManager;
import io.sentry.context.SingletonContextManager;
import io.sentry.dsn.Dsn;
import io.sentry.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class AndroidSentryClientFactory extends DefaultSentryClientFactory {
    private static final String DEFAULT_BUFFER_DIR = "sentry-buffered-events";
    public static final String TAG = "io.sentry.android.AndroidSentryClientFactory";
    private Context ctx;

    public AndroidSentryClientFactory(Context context) {
        Log.d(TAG, "Construction of Android Sentry.");
        this.ctx = context.getApplicationContext();
    }

    public SentryClient createSentryClient(Dsn dsn) {
        if (!checkPermission("android.permission.INTERNET")) {
            Log.e(TAG, "android.permission.INTERNET is required to connect to the Sentry server, please add it to your AndroidManifest.xml");
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sentry init with ctx='");
        stringBuilder.append(this.ctx.toString());
        stringBuilder.append("' and dsn='");
        stringBuilder.append(dsn);
        stringBuilder.append("'");
        Log.d(str, stringBuilder.toString());
        str = dsn.getProtocol();
        if (str.equalsIgnoreCase("noop")) {
            Log.w(TAG, "*** Couldn't find a suitable DSN, Sentry operations will do nothing! See documentation: https://docs.sentry.io/clients/java/modules/android/ ***");
        } else if (!(str.equalsIgnoreCase("http") || str.equalsIgnoreCase("https"))) {
            String lookup = Lookup.lookup(DefaultSentryClientFactory.ASYNC_OPTION, dsn);
            if (lookup == null || !lookup.equalsIgnoreCase("false")) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Only 'http' or 'https' connections are supported in Sentry Android, but received: ");
                stringBuilder.append(str);
                throw new IllegalArgumentException(stringBuilder.toString());
            }
            throw new IllegalArgumentException("Sentry Android cannot use synchronous connections, remove 'async=false' from your options.");
        }
        SentryClient createSentryClient = super.createSentryClient(dsn);
        createSentryClient.addBuilderHelper(new AndroidEventBuilderHelper(this.ctx));
        return createSentryClient;
    }

    /* Access modifiers changed, original: protected */
    public Collection<String> getInAppFrames(Dsn dsn) {
        Collection inAppFrames = super.getInAppFrames(dsn);
        if (inAppFrames.isEmpty()) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = this.ctx.getPackageManager().getPackageInfo(this.ctx.getPackageName(), 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Error getting package information.", e);
            }
            if (!(packageInfo == null || Util.isNullOrEmpty(packageInfo.packageName))) {
                ArrayList arrayList = new ArrayList(1);
                arrayList.add(packageInfo.packageName);
                return arrayList;
            }
        }
        return inAppFrames;
    }

    /* Access modifiers changed, original: protected */
    public Buffer getBuffer(Dsn dsn) {
        File file;
        String lookup = Lookup.lookup(DefaultSentryClientFactory.BUFFER_DIR_OPTION, dsn);
        if (lookup != null) {
            file = new File(lookup);
        } else {
            file = new File(this.ctx.getCacheDir().getAbsolutePath(), DEFAULT_BUFFER_DIR);
        }
        lookup = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Using buffer dir: ");
        stringBuilder.append(file.getAbsolutePath());
        Log.d(lookup, stringBuilder.toString());
        return new DiskBuffer(file, getBufferSize(dsn));
    }

    /* Access modifiers changed, original: protected */
    public ContextManager getContextManager(Dsn dsn) {
        return new SingletonContextManager();
    }

    private boolean checkPermission(String str) {
        return this.ctx.checkCallingOrSelfPermission(str) == 0;
    }
}
