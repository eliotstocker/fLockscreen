package com.piratemedia.lockscreen;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
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

	private void pickImage() {
		int width;
		int height;
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setType("image/*");
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
    	if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_LANDSCAPE, false)) {
    		width = display.getHeight();
    		height = display.getWidth();
    	} else {
    		width = display.getWidth();
    		height = display.getHeight();
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