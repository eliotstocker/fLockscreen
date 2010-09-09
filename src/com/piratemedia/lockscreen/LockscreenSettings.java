package com.piratemedia.lockscreen;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class LockscreenSettings extends PreferenceActivity {

	static final String KEY_MUSIC_PLAYER = "music_player_select";

	static final String KEY_FULLSCREEN = "fullscreen";
	
	static final String KEY_SHOW_ART = "albumart";
	
	static final String KEY_SHOW_CUSTOM_BG = "custom_bg";
	
	static final String KEY_PICK_BG = "bg_picker";
	
	static final String KEY_LANDSCAPE = "landscape";

	static final String KEY_HOME_APP_PACKAGE = "user_home_app_package";
	
	static final String KEY_HOME_APP_ACTIVITY = "user_home_app_activity";
	
	static final String SMS_COUNT_KEY = "sms_count";
	
	static final String MISSED_CALL_KEY = "missed_calls";
	
	static final String GMAIL_COUNT_KEY = "gmail_count";
	
	static final String MUTE_TOGGLE_KEY = "mute_toggle";
	
	static final String USB_MS_KEY = "usb_ms";
	
	static final String WIFI_MODE_KEY = "wifi_mode";
	
	static final String COUNT_KEY = "countDown";
	
	static final String LEFT_ACTION_KEY = "leftAction";
	
	static final String RIGHT_ACTION_KEY = "rightAction";
	
	static final String BLUETOOTH_MODE_KEY = "bluetooth_mode";
	
	static final String MUTE_MODE_KEY = "muteMode";
	
	static final String GMAIL_VIEW_KEY = "gmail_view";
	
	static final String GMAIL_ACCOUNT_KEY = "gmail_labels";
	
	static final String GMAIL_MERGE_KEY = "gmail_merge";
	
	static final String SERVICE_FOREGROUND = "service_foreground";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);
        
      //make sure service is running
        Intent ServiceStrt = new Intent("com.piratemedia.lockscreen.startservice");
        Intent serviceIntent = new Intent(this, updateService.class);
		serviceIntent.setAction(ServiceStrt.getAction());
		serviceIntent.putExtras(ServiceStrt);
		getBaseContext().startService(serviceIntent);
		
		DefaultMusicApp();
        
        PreferenceScreen screen = this.getPreferenceScreen();
        Preference pick = (Preference) screen.findPreference(KEY_PICK_BG);
        Preference landscape = (Preference) screen.findPreference(KEY_LANDSCAPE);
        Preference service_foreground = (Preference) screen.findPreference(SERVICE_FOREGROUND);
        Preference laction = (Preference) screen.findPreference(LEFT_ACTION_KEY);
        Preference raction = (Preference) screen.findPreference(RIGHT_ACTION_KEY);
        
        laction.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	actionLeft(newValue);
            	return true;
        	}
            });
        
        raction.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	actionRight(newValue);
            	return true;
            }
            });
        
        pick.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	pickImage();
            	return true;
        	}
        });
        
        landscape.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	String warning = getString(R.string.landscape_image_warning);
				Toast.makeText(getBaseContext(), warning, 1700).show();
            	return true;
        	}
        });
        
        service_foreground.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference arg0) {
				StartStopForground();
				return true;
			}
        });
        
        //ADW: Home app preference
        Preference homeApp=findPreference("user_home_app");
        homeApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent=new Intent(LockscreenSettings.this,HomeChooserActivity.class);
				intent.putExtra("loadOnClick", false);
				startActivity(intent);
				return true;
			}
		});
        
	}
	
	private void actionLeft(Object newVal) {
		int LeftInt;
		int RightInt;
		String LeftString = newVal.toString();
		String RightString = utils.getStringPref(getBaseContext() , LockscreenSettings.RIGHT_ACTION_KEY, "2");
		LeftInt = Integer.parseInt(LeftString);
		RightInt = Integer.parseInt(RightString);
		if (LeftInt != 1 && RightInt != 1) {
			Toast.makeText(getBaseContext(), "one of the actions must be unlock, setting right to unlock", Toast.LENGTH_SHORT).show();
			utils.setStringPref(getBaseContext(), RIGHT_ACTION_KEY, "1");
			startActivity(new Intent(getBaseContext(),
			LockscreenSettings.class));
			finish();
        }
	}
	
	private void actionRight(Object newVal) {
		int RightInt;
		int LeftInt;
		String RightString = newVal.toString();
		String LeftString = utils.getStringPref(getBaseContext() , LockscreenSettings.LEFT_ACTION_KEY, "1");
		LeftInt = Integer.parseInt(LeftString);
		RightInt = Integer.parseInt(RightString);
		if (LeftInt != 1 && RightInt != 1) {
			Toast.makeText(getBaseContext(), "one of the actions must be unlock, setting left to unlock", Toast.LENGTH_SHORT).show();
			utils.setStringPref(getBaseContext(), LEFT_ACTION_KEY, "1");
			startActivity(new Intent(getBaseContext(),
			LockscreenSettings.class));
			finish();
        }
	}

	private void pickImage() {
		int width;
		int height;
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setType("image/*");
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
    	if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_LANDSCAPE, false)) {
    		//for some reson these dont work unless the are halved, ie 800x480 is too big
    		//TODO: we need to fix this :)
    		width = display.getHeight()/2;
    		height = display.getWidth()/2;
    	} else {
    		width = display.getWidth()/2;
    		height = display.getHeight()/2;
    	}
        intent.putExtra("crop", "true");
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", height);
		intent.putExtra("aspectX", width);
		intent.putExtra("aspectY", height);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        
		startActivityForResult(intent, 4);
	}
	
	protected void  onActivityResult  (int requestCode, int resultCode, Intent data){
		if (requestCode==4) {
			if (resultCode == RESULT_OK) {
				try {
					
					Bitmap BG_Image = (Bitmap) data.getParcelableExtra("data");

				
					final String FileName = "bg_pic";
					FileOutputStream fileOutputStream = null;
					int quality = 80;

					fileOutputStream = openFileOutput(FileName + ".jpg", getBaseContext().MODE_PRIVATE);
					BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
					BG_Image.compress(CompressFormat.JPEG, quality, bos);
					bos.flush();
					bos.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} if (resultCode == RESULT_CANCELED) {
				String no_image = getString(R.string.custombg_none_selected);
				Toast.makeText(getBaseContext(), no_image, 700).show();
			}
		}
	}
	
	// check if android music exists, will use this to set default music player
	
	private void StartStopForground() {
		notifyChange(updateService.START_STOP_FORGROUND);
	}
	
	private void notifyChange(String what) {
        Intent i = new Intent(what);
        sendBroadcast(i);
    }
	
    private void DefaultMusicApp() {
    	
    	PreferenceScreen screen = this.getPreferenceScreen();
    	Preference MusicSel = (Preference) screen.findPreference(KEY_MUSIC_PLAYER);
    	
    	final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    
	     		String StockMusic = "com.android.music";
	     		String HTCMusic = "com.htc.music";
	
		for (int i = 0; i < services.size(); i++) {
			if (StockMusic.equals(services.get(i).service.getPackageName())) {
				MusicSel.setDefaultValue("1");
			} else if (HTCMusic.equals(services.get(i).service.getPackageName())) {
				MusicSel.setDefaultValue("2");
			} else {
				MusicSel.setDefaultValue("3");
			}
		}
    }
}