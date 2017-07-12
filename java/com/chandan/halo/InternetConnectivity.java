package com.chandan.halo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by Chandan on 19-12-2016.
 */

public class InternetConnectivity extends BroadcastReceiver {
    private boolean status=true;
    @Override
    public void onReceive(final Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        boolean isConn = networkInfo != null && networkInfo.isConnected();
        setInternetAvailable(isConn);

    }


    public void setInternetAvailable(boolean res){
        status=res;
    }
    public boolean isInternetAvailable(){
        return  status;
    }

}
