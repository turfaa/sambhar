package com.bumptech.glide.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.bumptech.glide.manager.ConnectivityMonitor.ConnectivityListener;

public class DefaultConnectivityMonitorFactory implements ConnectivityMonitorFactory {
    private static final String NETWORK_PERMISSION = "android.permission.ACCESS_NETWORK_STATE";
    private static final String TAG = "ConnectivityMonitor";

    @NonNull
    public ConnectivityMonitor build(@NonNull Context context, @NonNull ConnectivityListener connectivityListener) {
        Object obj = ContextCompat.checkSelfPermission(context, NETWORK_PERMISSION) == 0 ? 1 : null;
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, obj != null ? "ACCESS_NETWORK_STATE permission granted, registering connectivity monitor" : "ACCESS_NETWORK_STATE permission missing, cannot register connectivity monitor");
        }
        return obj != null ? new DefaultConnectivityMonitor(context, connectivityListener) : new NullConnectivityMonitor();
    }
}
