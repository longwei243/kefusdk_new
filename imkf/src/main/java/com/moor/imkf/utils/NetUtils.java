package com.moor.imkf.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 *
 */
public class NetUtils {
    private static final String TAG = "net";
    private static final int LOW_SPEED_UPLOAD_BUF_SIZE = 1024;
    private static final int HIGH_SPEED_UPLOAD_BUF_SIZE = 10240;
    private static final int MAX_SPEED_UPLOAD_BUF_SIZE = 102400;
    private static final int LOW_SPEED_DOWNLOAD_BUF_SIZE = 2024;
    private static final int HIGH_SPEED_DOWNLOAD_BUF_SIZE = 30720;
    private static final int MAX_SPEED_DOWNLOAD_BUF_SIZE = 102400;

    public NetUtils() {
    }

    public static boolean hasNetwork(Context var0) {
        if(var0 != null) {
            ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo var2 = var1.getActiveNetworkInfo();
            return var2 != null?var2.isAvailable():false;
        } else {
            return false;
        }
    }

    public static boolean hasDataConnection(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getNetworkInfo(1);
        if(var2 != null && var2.isAvailable()) {
            return true;
        } else {
            var2 = var1.getNetworkInfo(0);
            if(var2 != null && var2.isConnectedOrConnecting()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean isWifiConnection(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getNetworkInfo(1);
        if(var2 != null && var2.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isConnectionFast(int var0, int var1) {
        if(var0 == 1) {
            return true;
        } else {
            if(var0 == 0) {
                switch(var1) {
                    case 1:
                        return false;
                    case 2:
                        return false;
                    case 3:
                        return true;
                    case 4:
                        return false;
                    case 5:
                        return true;
                    case 6:
                        return true;
                    case 7:
                        return false;
                    case 8:
                        return true;
                    case 9:
                        return true;
                    case 10:
                        return true;
                    default:
                        if(Build.VERSION.SDK_INT >= 11 && (var1 == 14 || var1 == 13)) {
                            return true;
                        }

                        if(Build.VERSION.SDK_INT >= 9 && var1 == 12) {
                            return true;
                        }

                        if(Build.VERSION.SDK_INT >= 8 && var1 == 11) {
                            return false;
                        }
                }
            }

            return false;
        }
    }
}
