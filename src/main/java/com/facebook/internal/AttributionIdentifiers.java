package com.facebook.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AttributionIdentifiers {
    private static final String ANDROID_ID_COLUMN_NAME = "androidid";
    private static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
    private static final String ATTRIBUTION_ID_CONTENT_PROVIDER = "com.facebook.katana.provider.AttributionIdProvider";
    private static final String ATTRIBUTION_ID_CONTENT_PROVIDER_WAKIZASHI = "com.facebook.wakizashi.provider.AttributionIdProvider";
    private static final int CONNECTION_RESULT_SUCCESS = 0;
    private static final long IDENTIFIER_REFRESH_INTERVAL_MILLIS = 3600000;
    private static final String LIMIT_TRACKING_COLUMN_NAME = "limit_tracking";
    private static final String TAG = AttributionIdentifiers.class.getCanonicalName();
    private static AttributionIdentifiers recentlyFetchedIdentifiers;
    private String androidAdvertiserId;
    private String androidInstallerPackage;
    private String attributionId;
    private long fetchTime;
    private boolean limitTracking;

    private static final class GoogleAdInfo implements IInterface {
        private static final int FIRST_TRANSACTION_CODE = 1;
        private static final int SECOND_TRANSACTION_CODE = 2;
        private IBinder binder;

        GoogleAdInfo(IBinder iBinder) {
            this.binder = iBinder;
        }

        public IBinder asBinder() {
            return this.binder;
        }

        public String getAdvertiserId() throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                this.binder.transact(1, obtain, obtain2, 0);
                obtain2.readException();
                String readString = obtain2.readString();
                return readString;
            } finally {
                obtain2.recycle();
                obtain.recycle();
            }
        }

        public boolean isTrackingLimited() throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                boolean z = true;
                obtain.writeInt(1);
                this.binder.transact(2, obtain, obtain2, 0);
                obtain2.readException();
                if (obtain2.readInt() == 0) {
                    z = false;
                }
                obtain2.recycle();
                obtain.recycle();
                return z;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
            }
        }
    }

    private static final class GoogleAdServiceConnection implements ServiceConnection {
        private AtomicBoolean consumed;
        private final BlockingQueue<IBinder> queue;

        public void onServiceDisconnected(ComponentName componentName) {
        }

        private GoogleAdServiceConnection() {
            this.consumed = new AtomicBoolean(false);
            this.queue = new LinkedBlockingDeque();
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder != null) {
                try {
                    this.queue.put(iBinder);
                } catch (InterruptedException unused) {
                }
            }
        }

        public IBinder getBinder() throws InterruptedException {
            if (!this.consumed.compareAndSet(true, true)) {
                return (IBinder) this.queue.take();
            }
            throw new IllegalStateException("Binder already consumed");
        }
    }

    private static AttributionIdentifiers getAndroidId(Context context) {
        AttributionIdentifiers androidIdViaReflection = getAndroidIdViaReflection(context);
        if (androidIdViaReflection != null) {
            return androidIdViaReflection;
        }
        androidIdViaReflection = getAndroidIdViaService(context);
        return androidIdViaReflection == null ? new AttributionIdentifiers() : androidIdViaReflection;
    }

    private static AttributionIdentifiers getAndroidIdViaReflection(Context context) {
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Method methodQuietly = Utility.getMethodQuietly("com.google.android.gms.common.GooglePlayServicesUtil", "isGooglePlayServicesAvailable", Context.class);
                if (methodQuietly == null) {
                    return null;
                }
                Object invokeMethodQuietly = Utility.invokeMethodQuietly(null, methodQuietly, context);
                if (invokeMethodQuietly instanceof Integer) {
                    if (((Integer) invokeMethodQuietly).intValue() == 0) {
                        methodQuietly = Utility.getMethodQuietly("com.google.android.gms.ads.identifier.AdvertisingIdClient", "getAdvertisingIdInfo", Context.class);
                        if (methodQuietly == null) {
                            return null;
                        }
                        Object invokeMethodQuietly2 = Utility.invokeMethodQuietly(null, methodQuietly, context);
                        if (invokeMethodQuietly2 == null) {
                            return null;
                        }
                        methodQuietly = Utility.getMethodQuietly(invokeMethodQuietly2.getClass(), "getId", new Class[0]);
                        Method methodQuietly2 = Utility.getMethodQuietly(invokeMethodQuietly2.getClass(), "isLimitAdTrackingEnabled", new Class[0]);
                        if (methodQuietly != null) {
                            if (methodQuietly2 != null) {
                                AttributionIdentifiers attributionIdentifiers = new AttributionIdentifiers();
                                attributionIdentifiers.androidAdvertiserId = (String) Utility.invokeMethodQuietly(invokeMethodQuietly2, methodQuietly, new Object[0]);
                                attributionIdentifiers.limitTracking = ((Boolean) Utility.invokeMethodQuietly(invokeMethodQuietly2, methodQuietly2, new Object[0])).booleanValue();
                                return attributionIdentifiers;
                            }
                        }
                        return null;
                    }
                }
                return null;
            }
            throw new FacebookException("getAndroidId cannot be called on the main thread.");
        } catch (Exception e) {
            Utility.logd("android_id", e);
            return null;
        }
    }

    private static AttributionIdentifiers getAndroidIdViaService(Context context) {
        GoogleAdServiceConnection googleAdServiceConnection = new GoogleAdServiceConnection();
        Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        AttributionIdentifiers attributionIdentifiers = true;
        if (context.bindService(intent, googleAdServiceConnection, 1)) {
            try {
                GoogleAdInfo googleAdInfo = new GoogleAdInfo(googleAdServiceConnection.getBinder());
                attributionIdentifiers = new AttributionIdentifiers();
                attributionIdentifiers.androidAdvertiserId = googleAdInfo.getAdvertiserId();
                attributionIdentifiers.limitTracking = googleAdInfo.isTrackingLimited();
                return attributionIdentifiers;
            } catch (Exception e) {
                attributionIdentifiers = "android_id";
                Utility.logd((String) attributionIdentifiers, e);
            } finally {
                context.unbindService(googleAdServiceConnection);
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x006d A:{Catch:{ Exception -> 0x00db, all -> 0x00d9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0076 A:{Catch:{ Exception -> 0x00db, all -> 0x00d9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0071 A:{Catch:{ Exception -> 0x00db, all -> 0x00d9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0101  */
    public static com.facebook.internal.AttributionIdentifiers getAttributionIdentifiers(android.content.Context r10) {
        /*
        r0 = android.os.Looper.myLooper();
        r1 = android.os.Looper.getMainLooper();
        if (r0 != r1) goto L_0x0011;
    L_0x000a:
        r0 = TAG;
        r1 = "getAttributionIdentifiers should not be called from the main thread";
        android.util.Log.e(r0, r1);
    L_0x0011:
        r0 = recentlyFetchedIdentifiers;
        if (r0 == 0) goto L_0x0028;
    L_0x0015:
        r0 = java.lang.System.currentTimeMillis();
        r2 = recentlyFetchedIdentifiers;
        r2 = r2.fetchTime;
        r0 = r0 - r2;
        r2 = 3600000; // 0x36ee80 float:5.044674E-39 double:1.7786363E-317;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 >= 0) goto L_0x0028;
    L_0x0025:
        r10 = recentlyFetchedIdentifiers;
        return r10;
    L_0x0028:
        r0 = getAndroidId(r10);
        r1 = 3;
        r2 = 0;
        r5 = new java.lang.String[r1];	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r1 = "aid";
        r3 = 0;
        r5[r3] = r1;	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r1 = 1;
        r4 = "androidid";
        r5[r1] = r4;	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r1 = 2;
        r4 = "limit_tracking";
        r5[r1] = r4;	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r1 = r10.getPackageManager();	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r4 = "com.facebook.katana.provider.AttributionIdProvider";
        r1 = r1.resolveContentProvider(r4, r3);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        if (r1 == 0) goto L_0x0053;
    L_0x004b:
        r1 = "content://com.facebook.katana.provider.AttributionIdProvider";
        r1 = android.net.Uri.parse(r1);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
    L_0x0051:
        r4 = r1;
        goto L_0x0067;
    L_0x0053:
        r1 = r10.getPackageManager();	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r4 = "com.facebook.wakizashi.provider.AttributionIdProvider";
        r1 = r1.resolveContentProvider(r4, r3);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        if (r1 == 0) goto L_0x0066;
    L_0x005f:
        r1 = "content://com.facebook.wakizashi.provider.AttributionIdProvider";
        r1 = android.net.Uri.parse(r1);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        goto L_0x0051;
    L_0x0066:
        r4 = r2;
    L_0x0067:
        r1 = getInstallerPackageName(r10);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        if (r1 == 0) goto L_0x006f;
    L_0x006d:
        r0.androidInstallerPackage = r1;	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
    L_0x006f:
        if (r4 != 0) goto L_0x0076;
    L_0x0071:
        r10 = cacheAndReturnIdentifiers(r0);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        return r10;
    L_0x0076:
        r3 = r10.getContentResolver();	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r10 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x00db, all -> 0x00d9 }
        if (r10 == 0) goto L_0x00cf;
    L_0x0083:
        r1 = r10.moveToFirst();	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        if (r1 != 0) goto L_0x008a;
    L_0x0089:
        goto L_0x00cf;
    L_0x008a:
        r1 = "aid";
        r1 = r10.getColumnIndex(r1);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r3 = "androidid";
        r3 = r10.getColumnIndex(r3);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r4 = "limit_tracking";
        r4 = r10.getColumnIndex(r4);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r1 = r10.getString(r1);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r0.attributionId = r1;	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        if (r3 <= 0) goto L_0x00bc;
    L_0x00a4:
        if (r4 <= 0) goto L_0x00bc;
    L_0x00a6:
        r1 = r0.getAndroidAdvertiserId();	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        if (r1 != 0) goto L_0x00bc;
    L_0x00ac:
        r1 = r10.getString(r3);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r0.androidAdvertiserId = r1;	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r1 = r10.getString(r4);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r1 = java.lang.Boolean.parseBoolean(r1);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        r0.limitTracking = r1;	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
    L_0x00bc:
        if (r10 == 0) goto L_0x00c1;
    L_0x00be:
        r10.close();
    L_0x00c1:
        r10 = cacheAndReturnIdentifiers(r0);
        return r10;
    L_0x00c6:
        r0 = move-exception;
        r2 = r10;
        r10 = r0;
        goto L_0x00ff;
    L_0x00ca:
        r0 = move-exception;
        r9 = r0;
        r0 = r10;
        r10 = r9;
        goto L_0x00dd;
    L_0x00cf:
        r0 = cacheAndReturnIdentifiers(r0);	 Catch:{ Exception -> 0x00ca, all -> 0x00c6 }
        if (r10 == 0) goto L_0x00d8;
    L_0x00d5:
        r10.close();
    L_0x00d8:
        return r0;
    L_0x00d9:
        r10 = move-exception;
        goto L_0x00ff;
    L_0x00db:
        r10 = move-exception;
        r0 = r2;
    L_0x00dd:
        r1 = TAG;	 Catch:{ all -> 0x00fd }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00fd }
        r3.<init>();	 Catch:{ all -> 0x00fd }
        r4 = "Caught unexpected exception in getAttributionId(): ";
        r3.append(r4);	 Catch:{ all -> 0x00fd }
        r10 = r10.toString();	 Catch:{ all -> 0x00fd }
        r3.append(r10);	 Catch:{ all -> 0x00fd }
        r10 = r3.toString();	 Catch:{ all -> 0x00fd }
        com.facebook.internal.Utility.logd(r1, r10);	 Catch:{ all -> 0x00fd }
        if (r0 == 0) goto L_0x00fc;
    L_0x00f9:
        r0.close();
    L_0x00fc:
        return r2;
    L_0x00fd:
        r10 = move-exception;
        r2 = r0;
    L_0x00ff:
        if (r2 == 0) goto L_0x0104;
    L_0x0101:
        r2.close();
    L_0x0104:
        throw r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.AttributionIdentifiers.getAttributionIdentifiers(android.content.Context):com.facebook.internal.AttributionIdentifiers");
    }

    public static AttributionIdentifiers getCachedIdentifiers() {
        return recentlyFetchedIdentifiers;
    }

    private static AttributionIdentifiers cacheAndReturnIdentifiers(AttributionIdentifiers attributionIdentifiers) {
        attributionIdentifiers.fetchTime = System.currentTimeMillis();
        recentlyFetchedIdentifiers = attributionIdentifiers;
        return attributionIdentifiers;
    }

    public String getAttributionId() {
        return this.attributionId;
    }

    public String getAndroidAdvertiserId() {
        return (FacebookSdk.isInitialized() && FacebookSdk.getAdvertiserIDCollectionEnabled()) ? this.androidAdvertiserId : null;
    }

    public String getAndroidInstallerPackage() {
        return this.androidInstallerPackage;
    }

    public boolean isTrackingLimited() {
        return this.limitTracking;
    }

    @Nullable
    private static String getInstallerPackageName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager != null ? packageManager.getInstallerPackageName(context.getPackageName()) : null;
    }
}
