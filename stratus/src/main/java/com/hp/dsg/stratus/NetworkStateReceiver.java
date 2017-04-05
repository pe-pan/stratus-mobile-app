package com.hp.dsg.stratus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by panuska on 2.4.2017.
 */
//copied from http://stackoverflow.com/questions/12157130/internet-listener-android-example
public class NetworkStateReceiver extends BroadcastReceiver {

    private static boolean online = true;  // we expect the app being online when starting

    public static final String TAG = NetworkStateReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Network connectivity change");
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if (ni == null || ni.getState() != NetworkInfo.State.CONNECTED) {
            Log.d(TAG,"There's no network connectivity");
            if (online) // don't show the message if already offline
                Toast.makeText(context, R.string.noInternet, Toast.LENGTH_SHORT).show();
            online = false;
        } else {
            Log.d(TAG,"Network "+ni.getTypeName()+" connected");
            if (!online)  // don't show the message if already online
                Toast.makeText(context, R.string.backOnline, Toast.LENGTH_SHORT).show();
            online = true;
        }
    }
}
