package com.piratemedia.lockscreen;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);
        
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
					int quality = 50;

					BitmapFactory.Options options=new BitmapFactory.Options();
					options.inSampleSize = 5;
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
}