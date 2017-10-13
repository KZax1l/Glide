package com.bumptech.glide.manager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bumptech.glide.util.Synthetic;

/**
 * Uses {@link ConnectivityManager} to identify connectivity changes.
 * <p>
 * 使用一个{@link BroadcastReceiver}来监听网络连接状态
 */
class DefaultConnectivityMonitor implements ConnectivityMonitor {
    private final Context context;
    @Synthetic
    final ConnectivityListener listener;

    @Synthetic
    boolean isConnected;
    private boolean isRegistered;

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean wasConnected = isConnected;
            isConnected = isConnected(context);
            if (wasConnected != isConnected) {
                listener.onConnectivityChanged(isConnected);
            }
        }
    };

    public DefaultConnectivityMonitor(Context context, ConnectivityListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    private void register() {
        if (isRegistered) {
            return;
        }

        isConnected = isConnected(context);
        context.registerReceiver(connectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isRegistered = true;
    }

    private void unregister() {
        if (!isRegistered) {
            return;
        }

        context.unregisterReceiver(connectivityReceiver);
        isRegistered = false;
    }

    /**
     * 判断网络是否连接
     */
    @Synthetic
    // Permissions are checked in the factory instead.
    @SuppressLint("MissingPermission")
    boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onStart() {
        register();
    }

    @Override
    public void onStop() {
        unregister();
    }

    @Override
    public void onDestroy() {
        // Do nothing.
    }
}
