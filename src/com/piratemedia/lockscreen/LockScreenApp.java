package com.piratemedia.lockscreen;

import com.android.music.IMediaPlaybackService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class LockScreenApp extends Application {
	
	public IMediaPlaybackService mService = null;
	public static LockScreenApp sInstance = null;
	
	public static LockScreenApp getInstance() {
        
        if (sInstance != null) {
            return sInstance;
        } else {
            return new LockScreenApp();
        }
    }
	
	@Override
	public void onCreate() {
	
	sInstance = this;
	
}
	
	public void startService(Context aContext, Intent aIntent) {

		Intent serviceIntent = new Intent(this, updateService.class);
		serviceIntent.setAction(aIntent.getAction());
		serviceIntent.putExtras(aIntent);
		aContext.startService(serviceIntent);

	}
}
