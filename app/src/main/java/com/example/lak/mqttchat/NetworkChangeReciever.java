package com.example.lak.mqttchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReciever extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("msgS","Inside NetworkChange");
        if(checkInternet(context))
        {
            Log.d("msgS","Internet Available");
            Log.d("msgS","Calling connectt method!");
            MsgReceiver2.connectt();
        }

    }

    boolean checkInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }



}