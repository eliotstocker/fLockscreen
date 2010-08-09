package com.piratemedia.lockscreen;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import android.provider.MediaStore.Images.Media;
import android.widget.Toast;

public class LockscreenSettings extends PreferenceActivity {

	static final String KEY_MUSIC_PLAYER = "music_player_select";

	static final String KEY_FULLSCREEN = "fullscreen";
	
	static final String KEY_SHOW_ART = "albumart";
	
	static final String KEY_SHOW_CUSTOM_BG = "custom_bg";
	
	static final String KEY_PICK_BG = "bg_picker";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);
        
        PreferenceScreen screen = this.getPreferenceScreen();
        Preference pick = (Preference) screen.findPreference(KEY_PICK_BG);
        
        pick.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	pickImage();
            	return true;
        	}
            });
        
	}

	private void pickImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 4);
	}
	
	protected void  onActivityResult  (int requestCode, int resultCode, Intent data){
		
		if (requestCode==4) {
			if (resultCode == RESULT_OK) {
				Bitmap bitmap;
				try {
					bitmap = Media.getBitmap(getContentResolver(), data.getData());
				
				final String FileName = "bg_pic";
				FileOutputStream fileOutputStream = null;
				int quality = 50;

				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 5;
				fileOutputStream = openFileOutput(FileName + ".jpg", getBaseContext().MODE_PRIVATE);
				BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
				bitmap.compress(CompressFormat.JPEG, quality, bos);
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
				Toast.makeText(getBaseContext(), "No Image Selected", 700).show();
			}
		}
	}
}