package com.example.compling_app.GlobalClasses;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class Toodles {

    public static boolean isNetworkAvailable(Application application) {

        ConnectivityManager connManager =
                (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            NetworkCapabilities capabilities =
                    connManager.getNetworkCapabilities(connManager.getActiveNetwork());
            if (capabilities != null){
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                    return true;
                }
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                    return true;
                }
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    return true;
                }
            }
        }
        else {
            try {
                NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
