package com.piratemedia.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class intentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context aContext, Intent aIntent) {
    	if(!aIntent.equals(Intent.ACTION_BOOT_COMPLETED) && !utils.getCheckBoxPref(aContext, LockscreenSettings.ENABLE_KEY, true)) {
        LockScreenApp.getInstance().startService(aContext, aIntent);
    	}
    }
}
