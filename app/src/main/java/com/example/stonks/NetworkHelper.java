package com.example.stonks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    private Context context;
    private NetworkStatusListener listener;

    public NetworkHelper(Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void setNetworkStatusListener(NetworkStatusListener listener) {
        this.listener = listener;
    }

    public void startNetworkCallback() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    public void stopNetworkCallback() {
        context.unregisterReceiver(networkReceiver);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null) {
                listener.onNetworkChanged(isNetworkAvailable());
            }
        }
    };

    public interface NetworkStatusListener {
        void onNetworkChanged(boolean isConnected);
    }
}
