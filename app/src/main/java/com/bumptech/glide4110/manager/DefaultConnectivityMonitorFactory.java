package com.bumptech.glide4110.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * A factory class that produces a functional {@link ConnectivityMonitor}
 * if the application has the {@code android.permission.ACCESS_NETWORK_STATE} permission and a no-op
 * non functional {@link ConnectivityMonitor} if the app does not have
 * the required permission.
 */
public class DefaultConnectivityMonitorFactory implements ConnectivityMonitorFactory {
  private static final String TAG = "ConnectivityMonitor";
  private static final String NETWORK_PERMISSION = "android.permission.ACCESS_NETWORK_STATE";

  @NonNull
  @Override
  public ConnectivityMonitor build(
      @NonNull Context context, @NonNull ConnectivityMonitor.ConnectivityListener listener) {
    int permissionResult = ContextCompat.checkSelfPermission(context, NETWORK_PERMISSION);
    boolean hasPermission = permissionResult == PackageManager.PERMISSION_GRANTED;
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(
          TAG,
          hasPermission
              ? "ACCESS_NETWORK_STATE permission granted, registering connectivity monitor"
              : "ACCESS_NETWORK_STATE permission missing, cannot register connectivity monitor");
    }
    //1. 有网络权限 添加网络监听
    return hasPermission
        ? new DefaultConnectivityMonitor(context, listener)
        : new NullConnectivityMonitor();
  }
}
