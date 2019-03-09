package com.twitter.sdk.android.core.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.twitter.sdk.android.core.Twitter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class AdvertisingInfoServiceStrategy implements AdvertisingInfoStrategy {
    private static final String GOOGLE_PLAY_SERVICES_INTENT = "com.google.android.gms.ads.identifier.service.START";
    private static final String GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME = "com.google.android.gms";
    private static final String GOOGLE_PLAY_SERVICE_PACKAGE_NAME = "com.android.vending";
    private final Context context;

    private static final class AdvertisingConnection implements ServiceConnection {
        private static final int QUEUE_TIMEOUT_IN_MS = 200;
        private final LinkedBlockingQueue<IBinder> queue;
        private boolean retrieved;

        private AdvertisingConnection() {
            this.queue = new LinkedBlockingQueue(1);
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                this.queue.put(iBinder);
            } catch (InterruptedException unused) {
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            this.queue.clear();
        }

        /* Access modifiers changed, original: 0000 */
        public IBinder getBinder() {
            if (this.retrieved) {
                Twitter.getLogger().e("Twitter", "getBinder already called");
            }
            this.retrieved = true;
            try {
                return (IBinder) this.queue.poll(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException unused) {
                return null;
            }
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private static final String ADVERTISING_ID_SERVICE_INTERFACE_TOKEN = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
        private static final int AD_TRANSACTION_CODE_ID = 1;
        private static final int AD_TRANSACTION_CODE_LIMIT_AD_TRACKING = 2;
        private static final int FLAGS_NONE = 0;
        private final IBinder binder;

        private AdvertisingInterface(IBinder iBinder) {
            this.binder = iBinder;
        }

        public IBinder asBinder() {
            return this.binder;
        }

        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x0024 */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Can't wrap try/catch for region: R(5:5|6|11|10|8) */
        /* JADX WARNING: Missing block: B:6:?, code skipped:
            com.twitter.sdk.android.core.Twitter.getLogger().d("Twitter", "Could not get parcel from Google Play Service to capture AdvertisingId");
     */
        /* JADX WARNING: Missing block: B:7:0x0037, code skipped:
            r1.recycle();
            r0.recycle();
     */
        /* JADX WARNING: Missing block: B:8:?, code skipped:
            return null;
     */
        public java.lang.String getId() throws android.os.RemoteException {
            /*
            r5 = this;
            r0 = android.os.Parcel.obtain();
            r1 = android.os.Parcel.obtain();
            r2 = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
            r0.writeInterfaceToken(r2);	 Catch:{ Exception -> 0x0024 }
            r2 = r5.binder;	 Catch:{ Exception -> 0x0024 }
            r3 = 1;
            r4 = 0;
            r2.transact(r3, r0, r1, r4);	 Catch:{ Exception -> 0x0024 }
            r1.readException();	 Catch:{ Exception -> 0x0024 }
            r2 = r1.readString();	 Catch:{ Exception -> 0x0024 }
            r1.recycle();
            r0.recycle();
            goto L_0x0036;
        L_0x0022:
            r2 = move-exception;
            goto L_0x0037;
        L_0x0024:
            r2 = com.twitter.sdk.android.core.Twitter.getLogger();	 Catch:{ all -> 0x0022 }
            r3 = "Twitter";
            r4 = "Could not get parcel from Google Play Service to capture AdvertisingId";
            r2.d(r3, r4);	 Catch:{ all -> 0x0022 }
            r1.recycle();
            r0.recycle();
            r2 = 0;
        L_0x0036:
            return r2;
        L_0x0037:
            r1.recycle();
            r0.recycle();
            throw r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.internal.AdvertisingInfoServiceStrategy$AdvertisingInterface.getId():java.lang.String");
        }

        /* JADX WARNING: Missing exception handler attribute for start block: B:6:0x0025 */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Can't wrap try/catch for region: R(2:6|7) */
        /* JADX WARNING: Missing block: B:7:?, code skipped:
            com.twitter.sdk.android.core.Twitter.getLogger().d("Twitter", "Could not get parcel from Google Play Service to capture Advertising limitAdTracking");
     */
        /* JADX WARNING: Missing block: B:10:0x0037, code skipped:
            r1.recycle();
            r0.recycle();
     */
        private boolean isLimitAdTrackingEnabled() throws android.os.RemoteException {
            /*
            r6 = this;
            r0 = android.os.Parcel.obtain();
            r1 = android.os.Parcel.obtain();
            r2 = 0;
            r3 = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
            r0.writeInterfaceToken(r3);	 Catch:{ Exception -> 0x0025 }
            r3 = 1;
            r0.writeInt(r3);	 Catch:{ Exception -> 0x0025 }
            r4 = r6.binder;	 Catch:{ Exception -> 0x0025 }
            r5 = 2;
            r4.transact(r5, r0, r1, r2);	 Catch:{ Exception -> 0x0025 }
            r1.readException();	 Catch:{ Exception -> 0x0025 }
            r4 = r1.readInt();	 Catch:{ Exception -> 0x0025 }
            if (r4 == 0) goto L_0x0030;
        L_0x0021:
            r2 = 1;
            goto L_0x0030;
        L_0x0023:
            r2 = move-exception;
            goto L_0x0037;
        L_0x0025:
            r3 = com.twitter.sdk.android.core.Twitter.getLogger();	 Catch:{ all -> 0x0023 }
            r4 = "Twitter";
            r5 = "Could not get parcel from Google Play Service to capture Advertising limitAdTracking";
            r3.d(r4, r5);	 Catch:{ all -> 0x0023 }
        L_0x0030:
            r1.recycle();
            r0.recycle();
            return r2;
        L_0x0037:
            r1.recycle();
            r0.recycle();
            throw r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.core.internal.AdvertisingInfoServiceStrategy$AdvertisingInterface.isLimitAdTrackingEnabled():boolean");
        }
    }

    AdvertisingInfoServiceStrategy(Context context) {
        this.context = context.getApplicationContext();
    }

    public AdvertisingInfo getAdvertisingInfo() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Twitter.getLogger().d("Twitter", "AdvertisingInfoServiceStrategy cannot be called on the main thread");
            return null;
        }
        try {
            this.context.getPackageManager().getPackageInfo(GOOGLE_PLAY_SERVICE_PACKAGE_NAME, 0);
            AdvertisingConnection advertisingConnection = new AdvertisingConnection();
            Intent intent = new Intent(GOOGLE_PLAY_SERVICES_INTENT);
            intent.setPackage(GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME);
            try {
                if (this.context.bindService(intent, advertisingConnection, 1)) {
                    try {
                        AdvertisingInterface advertisingInterface = new AdvertisingInterface(advertisingConnection.getBinder());
                        AdvertisingInfo advertisingInfo = new AdvertisingInfo(advertisingInterface.getId(), advertisingInterface.isLimitAdTrackingEnabled());
                        this.context.unbindService(advertisingConnection);
                        return advertisingInfo;
                    } catch (Exception e) {
                        Twitter.getLogger().w("Twitter", "Exception in binding to Google Play Service to capture AdvertisingId", e);
                        this.context.unbindService(advertisingConnection);
                    }
                } else {
                    Twitter.getLogger().d("Twitter", "Could not bind to Google Play Service to capture AdvertisingId");
                    return null;
                }
            } catch (Throwable th) {
                Twitter.getLogger().d("Twitter", "Could not bind to Google Play Service to capture AdvertisingId", th);
            }
        } catch (NameNotFoundException unused) {
            Twitter.getLogger().d("Twitter", "Unable to find Google Play Services package name");
            return null;
        } catch (Exception e2) {
            Twitter.getLogger().d("Twitter", "Unable to determine if Google Play Services is available", e2);
            return null;
        }
    }
}
