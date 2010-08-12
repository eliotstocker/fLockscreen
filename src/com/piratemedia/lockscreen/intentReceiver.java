package com.piratemedia.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class intentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context aContext, Intent aIntent) {

        LockScreenApp.getInstance().startService(aContext, aIntent);
    }
}
